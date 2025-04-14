package com.attackranges;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;
import net.runelite.client.config.Keybind;

@ConfigGroup("attackRanges")
public interface AttackRangesConfig extends Config
{
	String ATTACK_RANGES_GROUP = "attackRanges";

	@ConfigSection(
		name = "Options",
		description = "Attack range options",
		position = 1
	)
	String options = "options";

	@ConfigSection(
		name = "Styles",
		description = "Visual styles",
		position = 2
	)
	String styles = "styles";

	@ConfigItem(
		keyName = "playerEnableState",
		name = "Display Overlay",
		description = "When the overlay will be shown",
		section = options,
		position = 1
	)
	default EnableState playerEnableState()
	{
		return EnableState.ON;
	}

	@ConfigItem(
		keyName = "npcHighlightEnableState",
		name = "Highlight NPCs",
		description = "When to highlight NPCs that can be targeted",
		section = options,
		position = 2
	)
	default EnableState npcHighlightEnableState()
	{
		return EnableState.OFF;
	}

	@ConfigItem(
		keyName = "displayHotkey",
		name = "Display Hotkey",
		description = "Hotkey to press to display the overlay",
		section = options,
		position = 3
	)
	default Keybind displayHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "displayHotkeyMode",
		name = "Hotkey Mode",
		description = "Whether the hotkey is toggle or hold to display the overlay",
		section = options,
		position = 4
	)
	default DisplayHotkeyMode displayHotkeyMode()
	{
		return DisplayHotkeyMode.TOGGLE;
	}

	@ConfigItem(
		keyName = "allowListedWeapons",
		name = "Rendered weapons",
		description = "List of items you want displayed. Supports wildcards. Example: Trident*, rune crossbow",
		section = options,
		position = 5
	)
	default String getAllowListedWeapons()
	{
		return "";
	}

	@ConfigItem(
		keyName = "showManualCasting",
		name = "Display manual casting",
		description = "Display cast range for weapons when not auto casting",
		section = options,
		position = 6
	)
	default boolean getManualCastingMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showMeleeWeapons",
		name = "Display melee",
		description = "Display the range with melee weapons",
		section = options,
		position = 7
	)
	default boolean getShowDisplayMelee()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showSpecialAttack",
		name = "Display special attack",
		description = "Display ranges for special attacks, such as Dinh's Bulwark",
		section = options,
		position = 8
	)
	default boolean getShowSpecialAttack()
	{
		return true;
	}

	@ConfigItem(
		keyName = "dragProtection",
		name = "Enable drag protection",
		description = "Hides the Attack option on NPCs not in attack range",
		section = options,
		position = 9
	)
	default boolean getDragProtection()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayMode",
		name = "Display Mode",
		description = "How the attack range is displayed",
		section = styles
	)
	default DisplayMode displayMode()
	{
		return DisplayMode.BORDER;
	}

	@Alpha
	@ConfigItem(
		keyName = "rangeBorderColor",
		name = "Border Color",
		description = "The color of the your attack range border",
		position = 1,
		section = styles
	)
	default Color rangeBorderColor()
	{
		return new Color(86, 234, 103, 102);
	}

	@Alpha
	@ConfigItem(
		keyName = "rangeFillColor",
		name = "Fill Color",
		description = "The inner color of the your attack range section",
		position = 2,
		section = styles
	)
	default Color rangeFillColor()
	{
		return new Color(0, 0, 0, 50);
	}

	@ConfigItem(
		keyName = "borderSize",
		name = "Border Size",
		description = "Thickness of your attack range border",
		position = 3,
		section = styles
	)
	default int borderSize()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(
		keyName = "npcHighlightOutlineColor",
		name = "NPC Outline Color",
		description = "The outline color of targetable NPCs",
		position = 4,
		section = styles
	)
	default Color npcHighlightOutlineColor()
	{
		return new Color(0x9000FFFF, true);
	}

	@Alpha
	@ConfigItem(
		keyName = "npcHighlightFillColor",
		name = "NPC Fill Color",
		description = "The inner color of highlighted NPCs",
		position = 5,
		section = styles
	)
	default Color npcHighlightFillColor()
	{
		return new Color(0x4400FFFF, true);
	}

	@ConfigItem(
		keyName = "npcHighlightOutlineThickness",
		name = "NPC Outline Thickness",
		description = "Thickness of the highlighted NPCs outline",
		position = 6,
		section = styles
	)
	default int npcOutlineSize()
	{
		return 3;
	}


	enum DisplayMode
	{
		TILES,
		BORDER,
		EDGE_TILES,
	}

	enum EnableState
	{
		ON,
		OFF,
		INSTANCES_ONLY,
		HOTKEY_MODE
	}

	enum DisplayHotkeyMode
	{
		TOGGLE,
		HOLD
	}
}
