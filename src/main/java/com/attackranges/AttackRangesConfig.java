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
	String ATTACK_RANGES_GROUP = "attackranges";

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
		keyName = "displayHotkey",
		name = "Display Hotkey",
		description = "Hotkey to press to display the overlay",
		section = options,
		position = 2
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
		position = 3
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
		position = 4
	)
	default String getAllowListedWeapons() { return ""; }

	@ConfigItem(
		keyName = "showManualCasting",
		name = "Display manual casting",
		description = "Display cast range for weapons when not auto casting",
		section = options,
		position = 5
	)
	default boolean getManualCastingMode() { return false; }

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
		description = "The color of the target's attack range border",
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
		description = "The inner color of the target's attack range section",
		section = styles
	)
	default Color rangeFillColor()
	{
		return new Color(0, 0, 0, 50);
	}

	@ConfigItem(
		keyName = "borderSize",
		name = "Border Size",
		description = "Thickness of the attack range border",
		section = styles
	)
	default int borderSize()
	{
		return 1;
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
