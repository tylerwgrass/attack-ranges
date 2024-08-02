package com.attackranges;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

@ConfigGroup("attackRanges")
public interface AttackRangesConfig extends Config
{
	@ConfigSection(
		name = "Styles",
		description = "Visual styles",
		position = 2
	)
	String styles = "styles";

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
		BORDER
	}
}
