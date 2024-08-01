package com.attackranges;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.Set;

@Slf4j
public class AttackRangesUtils {
    public static WorldPoint[][] getVisiblePoints(Actor actor, int dist) {
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
}