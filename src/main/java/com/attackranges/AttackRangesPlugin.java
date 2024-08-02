package com.attackranges;

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
import net.runelite.api.ParamID;
import net.runelite.api.StructComposition;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.ItemContainerChanged;
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
	public Integer playerAttackRange = -1;
	@Inject
	private net.runelite.api.Client client;
	@Inject
	private AttackRangesConfig config;
	@Inject
	private AttackRangesOverlay overlay;
	@Inject
	private OverlayManager overlayManager;
	private Item equippedWeapon;

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
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayer.ATTACK_STYLE || event.getVarbitId() == Varbits.EQUIPPED_WEAPON_TYPE)
		{
			final int attackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
			final int equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
			updatePlayerAttackRange(attackStyleVarbit, equippedWeaponTypeVarbit);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return;
		}

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null)
		{
			return;
		}

		equippedWeapon = weapon;
		final int attackStyleVarbit = client.getVarpValue(VarPlayer.ATTACK_STYLE);
		final int equippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		updatePlayerAttackRange(attackStyleVarbit, equippedWeaponTypeVarbit);
	}

	@Provides
	AttackRangesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AttackRangesConfig.class);
	}

	private void updatePlayerAttackRange(Integer attackStyleVarbit, Integer weaponTypeVarbit)
	{
		if (equippedWeapon == null)
		{
			return;
		}

		if (!weaponsMap.containsKey(equippedWeapon.getId()))
		{
			log.warn("Unsupported equipment: {}", equippedWeapon);
			playerAttackRange = -1;
			return;
		}

		Weapon weapon = weaponsMap.get(equippedWeapon.getId());
		playerAttackRange = weapon.getRange(getWeaponAttackStyle(attackStyleVarbit, weaponTypeVarbit));
	}

	private String getWeaponAttackStyle(Integer attackStyleVarbit, Integer weaponTypeVarbit)
	{
		int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(weaponTypeVarbit);
		int[] weaponStyleStructs = client.getEnum(weaponStyleEnum).getIntVals();
		StructComposition attackStylesStruct = client.getStructComposition(weaponStyleStructs[attackStyleVarbit]);
		return attackStylesStruct.getStringValue(ParamID.ATTACK_STYLE_NAME);
	}
}

