package com.attackranges;

import java.util.Set;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

public class Regions
{
	public static final int FIGHT_CAVES = 9551;
	public static final int INFERNO = 9043;
	public static final int GAUNTLET = 7512;
	public static final int CORRUPTED_GAUNTLET = 7768;
	public static final int FORTIS_COLOSSEUM = 7216;

	public static final Set<Integer> SUPPORTED_REGIONS = Set.of(
		FIGHT_CAVES,
		INFERNO,
		GAUNTLET,
		CORRUPTED_GAUNTLET,
		FORTIS_COLOSSEUM
	);

	public static boolean isInRegion(Client client, int regionId)
	{
		Integer playerRegionId = getPlayerRegionId(client);
		return playerRegionId != null && playerRegionId == regionId;
	}

	public static Integer getPlayerRegionId(Client client)
	{
		return getActorRegionId(client, client.getLocalPlayer());
	}

	public static Integer getActorRegionId(Client client, Actor actor)
	{
		WorldPoint wp = actor.getWorldLocation();
		LocalPoint lp = LocalPoint.fromWorld(client.getLocalPlayer().getWorldView(), wp);

		if (lp == null)
		{
			return null;
		}

		return WorldPoint.fromLocalInstance(client, lp).getRegionID();
	}
}
