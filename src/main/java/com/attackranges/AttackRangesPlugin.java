package com.attackranges;

import static com.attackranges.AttackRangesUtils.getVisiblePoints;
import static com.attackranges.AttackRangesUtils.handleDragProtection;
import static com.attackranges.Regions.isInRegion;
import com.attackranges.weapons.ManualCastable;
import com.attackranges.weapons.Weapon;
import com.attackranges.weapons.WeaponsGenerator;
import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NpcID;
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Attack Ranges"
)
public class AttackRangesPlugin extends Plugin
{
	private final Map<Integer, Weapon> weaponsMap = new HashMap<>();
	@Inject
	private net.runelite.api.Client client;
	@Inject
	private AttackRangesConfig config;
	@Inject
	private ConfigManager configManager;
	@Inject
	private AttackRangesOverlay overlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ClientThread clientThread;
	@Inject
	private KeyManager keyManager;
	@Inject
	private UpdateManager updateManager;

	private final Splitter allowListSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
	@Getter
	private Item equippedWeapon;
	@Getter
	private List<String> allowListedWeapons = new ArrayList<>();
	public WorldPoint[][] playerVisiblePoints;
	public Integer playerAttackRange = -1;
	public Integer externalRangeModifier = 0;

	private final String OVERLAY_RENDER_ENABLED_KEY = "player-overlay-render-enabled";

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		weaponsMap.putAll(WeaponsGenerator.generate());
		keyManager.registerKeyListener(playerOverlayEnabledHotkeyListener);

		allowListedWeapons = allowListSplitter.splitToList(config.getAllowListedWeapons());

		String savedRenderState = configManager.getConfiguration(
			AttackRangesConfig.ATTACK_RANGES_GROUP,
			OVERLAY_RENDER_ENABLED_KEY);

		if (savedRenderState != null)
		{
			AttackRangesUtils.setHotkeyRenderEnabled(Boolean.parseBoolean(savedRenderState));
		}
	}

	@Override
	protected void shutDown()
	{
		reset();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!AttackRangesConfig.ATTACK_RANGES_GROUP.equals(event.getGroup()))
		{
			return;
		}

		switch (event.getKey())
		{
			case "npcHighlightEnableState":
			case "playerEnableState":
				if (Objects.equals(event.getNewValue(), AttackRangesConfig.EnableState.HOTKEY_MODE.toString()))
				{
					if (config.displayHotkeyMode() == AttackRangesConfig.DisplayHotkeyMode.HOLD)
					{
						AttackRangesUtils.setHotkeyRenderEnabled(false);
					}
				}
				return;
			case "displayHotkeyMode":
				if (Objects.equals(event.getNewValue(), AttackRangesConfig.DisplayHotkeyMode.HOLD.name()))
				{
					AttackRangesUtils.setHotkeyRenderEnabled(false);
				}
				return;
			case "allowListedWeapons":
				allowListedWeapons = allowListSplitter.splitToList(event.getNewValue());
			case "showManualCasting":
				clientThread.invoke(this::updatePlayerAttackRange);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		final GameState state = event.getGameState();

		if (state == GameState.LOGGED_IN && !updateManager.hasLatestVersion())
		{
			updateManager.sendUpdateMessage();
		}
	}

	@Subscribe
	protected void onGameTick(GameTick event)
	{
		playerVisiblePoints = getVisiblePoints(client.getLocalPlayer(), playerAttackRange, client);

		if (!isInRegion(client, Regions.FORTIS_COLOSSEUM))
		{
			externalRangeModifier = 0;
		}
	}

	@Subscribe(priority = -1)
	public void onPostMenuSort(PostMenuSort event)
	{
		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries.length == 0 || !config.getDragProtection())
		{
			return;
		}

		handleDragProtection(menuEntries, client);
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (!ColosseumHandler.isSelectModifierScript(event.getScriptId()))
		{
			return;
		}

		try
		{
			ColosseumHandler.setNextWaveModifierOptions(event.getScriptEvent().getArguments());
		}
		catch (Exception e)
		{
			log.warn("Failed to parse modifiers", e);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() != NpcID.MINIMUS_12808)
		{
			return;
		}

		externalRangeModifier = ColosseumHandler.getMyopiaRangeDeduction(client) * -1;
		updatePlayerAttackRange();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayer.ATTACK_STYLE || event.getVarbitId() == Varbits.EQUIPPED_WEAPON_TYPE)
		{
			updatePlayerAttackRange();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			equippedWeapon = null;
			return;
		}

		equippedWeapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		updatePlayerAttackRange();
	}

	private final HotkeyListener playerOverlayEnabledHotkeyListener = new HotkeyListener(() -> config.displayHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			if (config.playerEnableState() != AttackRangesConfig.EnableState.HOTKEY_MODE &&
				config.npcHighlightEnableState() != AttackRangesConfig.EnableState.HOTKEY_MODE)
			{
				return;
			}

			if (config.displayHotkeyMode() == AttackRangesConfig.DisplayHotkeyMode.HOLD)
			{
				AttackRangesUtils.setHotkeyRenderEnabled(true);
			}
			else
			{
				AttackRangesUtils.setHotkeyRenderEnabled(!AttackRangesUtils.isHotkeyRenderEnabled());
			}
		}

		@Override
		public void hotkeyReleased()
		{
			if (config.playerEnableState() != AttackRangesConfig.EnableState.HOTKEY_MODE &&
				config.npcHighlightEnableState() != AttackRangesConfig.EnableState.HOTKEY_MODE)
			{
				return;
			}
			if (config.displayHotkeyMode() == AttackRangesConfig.DisplayHotkeyMode.HOLD)
			{
				AttackRangesUtils.setHotkeyRenderEnabled(false);
			}
		}
	};

	@Provides
	AttackRangesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AttackRangesConfig.class);
	}

	private void updatePlayerAttackRange()
	{
		final int attackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		final int weaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		if (equippedWeapon == null)
		{
			playerAttackRange = 1;
			return;
		}

		if (!weaponsMap.containsKey(equippedWeapon.getId()))
		{
			playerAttackRange = 1;
			return;
		}

		Weapon weapon = weaponsMap.get(equippedWeapon.getId());
		String attackStyle = getWeaponAttackStyle(attackStyleVarbit, weaponTypeVarbit);
		int unmodifiedRange = (weapon instanceof ManualCastable)
			? ((ManualCastable) weapon).getRange(attackStyle, config.getManualCastingMode())
			: weapon.getRange(attackStyle);
		playerAttackRange = Math.max(unmodifiedRange + externalRangeModifier, 0);
	}

	private String getWeaponAttackStyle(Integer attackStyleVarbit, Integer weaponTypeVarbit)
	{
		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(weaponTypeVarbit);
		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		StructComposition attackStylesStruct = client.getStructComposition(weaponStyleStructs[attackStyleVarbit]);
		return attackStylesStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);
	}

	private void reset()
	{
		overlayManager.remove(overlay);
		configManager.setConfiguration(AttackRangesConfig.ATTACK_RANGES_GROUP, OVERLAY_RENDER_ENABLED_KEY, AttackRangesUtils.isHotkeyRenderEnabled());
		keyManager.unregisterKeyListener(playerOverlayEnabledHotkeyListener);
		AttackRangesUtils.getTargetableNpcs().clear();
		AttackRangesUtils.getNpcPointMap().clear();
		weaponsMap.clear();
	}
}

