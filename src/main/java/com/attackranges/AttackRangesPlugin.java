package com.attackranges;

import static com.attackranges.Regions.isInRegion;
import com.attackranges.weapons.Weapon;
import com.attackranges.weapons.WeaponsGenerator;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NpcID;
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.Map;
import java.util.HashMap;

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
	private AttackRangesOverlay overlay;
	@Inject
	private OverlayManager overlayManager;
	private Item equippedWeapon;
	public WorldPoint[][] playerVisiblePoints;
	public Integer playerAttackRange = -1;
	public Integer externalRangeModifier = 0;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		weaponsMap.putAll(WeaponsGenerator.generate());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	protected void onGameTick(GameTick event)
	{
		playerVisiblePoints = AttackRangesUtils.getVisiblePoints(client.getLocalPlayer(), playerAttackRange);

		if (!isInRegion(client, Regions.FORTIS_COLOSSEUM))
		{
			externalRangeModifier = 0;
		}
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
			playerAttackRange = -1;
			return;
		}

		if (!weaponsMap.containsKey(equippedWeapon.getId()))
		{
			log.warn("Unsupported equipment: {}", equippedWeapon);
			playerAttackRange = -1;
			return;
		}

		Weapon weapon = weaponsMap.get(equippedWeapon.getId());
		int unmodifiedRange = weapon.getRange(getWeaponAttackStyle(attackStyleVarbit, weaponTypeVarbit));
		playerAttackRange = Math.max(unmodifiedRange + externalRangeModifier, 0);
	}

	private String getWeaponAttackStyle(Integer attackStyleVarbit, Integer weaponTypeVarbit)
	{
		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(weaponTypeVarbit);
		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		StructComposition attackStylesStruct = client.getStructComposition(weaponStyleStructs[attackStyleVarbit]);
		return attackStylesStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);
	}
}

