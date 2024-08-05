package com.attackranges;

import com.attackranges.AttackRangesConfig.EnableState;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

@Slf4j
public class AttackRangesUtils
{
	public static WorldPoint[][] getVisiblePoints(Actor actor, int dist)
	{
		if (dist < 1)
		{
			return new WorldPoint[0][0];
		}

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
				}
			}
		}
		return points;
	}

	private static final int FIGHT_CAVES_REGION_ID = 9551;
	private static final int INFERNO_REGION_ID = 9043;
	private static final int GAUNTLET_REGION_ID = 7512;
	private static final int CORRUPTED_GAUNTLET_REGION_ID = 7768;
	private static final int FORTIS_COLOSSEUM_REGION_ID = 7216;

	public static final Set<Integer> SUPPORTED_REGIONS = Set.of(
		FIGHT_CAVES_REGION_ID,
		INFERNO_REGION_ID,
		GAUNTLET_REGION_ID,
		CORRUPTED_GAUNTLET_REGION_ID,
		FORTIS_COLOSSEUM_REGION_ID
	);

	public static boolean shouldRenderForPlayer(AttackRangesPlugin plugin, AttackRangesConfig config, Client client)
	{
		if (plugin.playerAttackRange < 1)
		{
			return false;
		}

		WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		LocalPoint lp = LocalPoint.fromWorld(client.getLocalPlayer().getWorldView(), wp);

		if (lp == null)
		{
			return false;
		}

		int regionId = WorldPoint.fromLocalInstance(client, lp).getRegionID();
		return config.playerEnableState() == EnableState.ON
			|| (config.playerEnableState() == EnableState.INSTANCES_ONLY && SUPPORTED_REGIONS.contains(regionId));
	}
}