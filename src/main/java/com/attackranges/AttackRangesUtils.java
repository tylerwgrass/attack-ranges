package com.attackranges;

import static com.attackranges.Regions.SUPPORTED_REGIONS;
import static com.attackranges.Regions.getPlayerRegionId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.WildcardMatcher;

public class AttackRangesUtils
{
	@Getter
	private static final Set<NPC> targetableNpcs = new HashSet<>();
	private static final Map<WorldPoint, Set<NPC>> npcPointMap = new HashMap<>();

	@Getter
	@Setter
	private static boolean isHotkeyRenderEnabled = false;

	public static Map<WorldPoint, Set<NPC>> mapNpcsToPoints(Client client)
	{
		Map<WorldPoint, Set<NPC>> npcPointMap = new HashMap<>();

		client.getLocalPlayer().getWorldView().npcs().stream()
			.filter(npc -> npc.getCombatLevel() > 0)
			.forEach(npc -> {
				WorldPoint npcPoint = npc.getWorldLocation();
				Set<NPC> npcSet = npcPointMap.computeIfAbsent(npcPoint, k -> new HashSet<>());
				npcSet.add(npc);
		});

		return npcPointMap;
	}

	public static boolean isAllowlistedWeapon(String weaponName, List<String> allowListedWeapons)
	{
		if (allowListedWeapons.isEmpty())
		{
			return true;
		}

		for (String pattern : allowListedWeapons)
		{
			if (WildcardMatcher.matches(pattern, weaponName))
			{
				return true;
			}
		}
		return false;
	}

	public static WorldPoint[][] getVisiblePoints(Actor actor, int dist, Client client)
	{
		targetableNpcs.clear();
		npcPointMap.clear();

		if (dist < 1)
		{
			return new WorldPoint[0][0];
		}
		npcPointMap.putAll(mapNpcsToPoints(client));

		final WorldArea wa = actor.getWorldArea();
		final WorldView wv = actor.getWorldView();
		final int areaWidth = wa.getWidth() + (dist * 2);
		final int areaHeight = wa.getHeight() + (dist * 2);
		final WorldPoint[][] points = new WorldPoint[areaWidth][areaHeight];

		int startX = wa.getX() - dist;
		int startY = wa.getY() - dist;
		int maxX = wa.getX() + wa.getWidth() + dist - 1;
		int maxY = wa.getY() + wa.getHeight() + dist - 1;

		for (int x = startX, i = 0; x < maxX + 1; x++, i++)
		{
			for (int y = startY, j = 0; y < maxY + 1; y++, j++)
			{
				WorldPoint currentPoint = new WorldPoint(x, y, wa.getPlane());
				if (wa.hasLineOfSightTo(wv, currentPoint))
				{
					points[i][j] = currentPoint;
					if (npcPointMap.containsKey(currentPoint))
					{
						targetableNpcs.addAll(npcPointMap.get(currentPoint));
					}
				}
			}
		}
		return points;
	}

	public static boolean shouldRender(
		AttackRangesPlugin plugin,
		Client client,
		AttackRangesConfig.EnableState enableState)
	{
		if (plugin.playerAttackRange < 1)
		{
			return false;
		}

		switch (enableState)
		{
			case ON:
				return true;
			case HOTKEY_MODE:
				return isHotkeyRenderEnabled;
			case INSTANCES_ONLY:
				Integer regionId = getPlayerRegionId(client);
				return regionId != null && SUPPORTED_REGIONS.contains(regionId);
			case OFF:
			default:
				return false;
		}
	}

	public static boolean isOuterTile(WorldPoint[][] points, int i, int j)
	{
		boolean isTopEdge = j == 0 || points[i][j - 1] == null;
		boolean isBottomEdge = j == points.length - 1 || points[i][j + 1] == null;
		boolean isLeftEdge = i == 0 || points[i - 1][j] == null;
		boolean isRightEdge = i == points[0].length - 1 || points[i + 1][j] == null;

		return isTopEdge || isBottomEdge || isLeftEdge || isRightEdge;
	}
	public static void handleDragProtection(MenuEntry[] menuEntries, Client client)
	{
		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		NPC target = topEntry.getNpc();

		if (target == null || !topEntry.getOption().equals("Attack"))
		{
			return;
		}

		int examineEntryIndex = -1;
		for (int i = 0; i < menuEntries.length; i++)
		{
			if (menuEntries[i].getOption().equals("Examine"))
			{
				examineEntryIndex = i;
				break;
			}

		}

		if (examineEntryIndex == -1 || targetableNpcs.contains(target))
		{
			return;
		}

		swapOptions(menuEntries, menuEntries.length - 1, examineEntryIndex);
		client.getMenu().setMenuEntries(menuEntries);
	}

	private static void swapOptions(MenuEntry[] entries, int i, int j)
	{
		MenuEntry temp = entries[i];
		entries[i] = entries[j];
		entries[j] = temp;
	}
}