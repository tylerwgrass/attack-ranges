package com.attackranges;

import static com.attackranges.AttackRangesUtils.isOuterTile;
import static com.attackranges.AttackRangesUtils.shouldRender;

import com.google.inject.Inject;
import java.awt.Shape;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
class AttackRangesOverlay extends Overlay
{
	private final Client client;
	private final AttackRangesConfig config;
	private final AttackRangesPlugin plugin;
	private WorldPoint cachedPlayerPosition;
	private WorldPoint[][] cachedPoints;
	private WorldView cachedWorldView;
	private int[] cachedCameraState;
	private Path2D cachedBorderPath;
	private Path2D cachedTileBorderPath;
	private Shape cachedFillShape;
	private Color cachedFillColor;
	private Color cachedBorderColor;
	private int cachedBorderSize;
	private AttackRangesConfig.DisplayMode cachedDisplayMode;

	@Inject
	private AttackRangesOverlay(Client client, AttackRangesConfig config, AttackRangesPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{

		if (shouldRender(plugin, client, config, config.playerEnableState()))
		{
			renderPlayer(graphics);
		}

		if (shouldRender(plugin, client, config, config.npcHighlightEnableState()))
		{
			renderTargetableNpcs(graphics);
		}
		return null;
	}

	private void renderTargetableNpcs(Graphics2D graphics)
	{
		for (NPC npc : AttackRangesUtils.getTargetableNpcs())
		{
			Shape npcClickbox = npc.getConvexHull();
			renderPoly(
				graphics,
				config.npcHighlightOutlineColor(),
				config.npcOutlineSize(),
				config.npcHighlightFillColor(),
				npcClickbox);
		}
	}

	private void renderPlayer(Graphics2D graphics)
	{
		drawAttackableSquares(graphics, client.getLocalPlayer(), plugin.playerVisiblePoints, config.rangeBorderColor());
	}

	private void drawAttackableSquares(Graphics2D graphics, Actor actor, WorldPoint[][] points, Color color)
	{
		if (points == null)
		{
			return;
		}

		WorldView wv = actor.getWorldView();
		WorldPoint playerPosition = actor.getWorldLocation();
		int[] cameraState = getCameraState();
		Color fillColor = config.rangeFillColor();
		int borderSize = config.borderSize();
		AttackRangesConfig.DisplayMode displayMode = config.displayMode();
		if (!Objects.equals(playerPosition, cachedPlayerPosition)
			|| !Arrays.deepEquals(points, cachedPoints)
			|| wv != cachedWorldView
			|| !Arrays.equals(cameraState, cachedCameraState)
			|| !Objects.equals(fillColor, cachedFillColor)
			|| !Objects.equals(color, cachedBorderColor)
			|| borderSize != cachedBorderSize
			|| displayMode != cachedDisplayMode)
		{
			cachedBorderPath = new Path2D.Double();
			cachedTileBorderPath = new Path2D.Double();
			Path2D fillPath = new Path2D.Double();
			for (int i = 0; i < points.length; i++)
			{
				for (int j = 0; j < points[i].length; j++)
				{
					WorldPoint wp = points[i][j];
					if (wp == null)
					{
						continue;
					}

					LocalPoint lp = LocalPoint.fromWorld(wv, wp);
					if (lp == null)
					{
						continue;
					}

					Polygon tile = Perspective.getCanvasTilePoly(client, lp);
					if (tile != null)
					{
						fillPath.append(tile, false);
					}
					switch (displayMode)
					{
						case TILES:
							if (tile != null)
							{
								cachedTileBorderPath.append(tile, false);
							}
							break;
						case EDGE_TILES:
							if (isOuterTile(points, i, j) && tile != null)
							{
								cachedTileBorderPath.append(tile, false);
							}
							break;
						case BORDER:
							appendBorders(cachedBorderPath, lp, wv, wp.getPlane(), points, i, j);
							break;
					}
				}
			}

			cachedFillShape = new Area(fillPath);
			cachedPlayerPosition = playerPosition;
			cachedPoints = copyPoints(points);
			cachedWorldView = wv;
			cachedCameraState = cameraState;
			cachedFillColor = fillColor;
			cachedBorderColor = color;
			cachedBorderSize = borderSize;
			cachedDisplayMode = displayMode;
		}

		graphics.setColor(fillColor);
		graphics.fill(cachedFillShape);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(borderSize));
		graphics.draw(cachedBorderPath);
		graphics.draw(cachedTileBorderPath);
	}

	private int[] getCameraState()
	{
		return new int[]{
			client.getCameraX(),
			client.getCameraY(),
			client.getCameraZ(),
			client.getCameraPitch(),
			client.getCameraYaw(),
			client.getScale(),
			client.getViewportXOffset(),
			client.getViewportYOffset(),
			client.getViewportWidth(),
			client.getViewportHeight()
		};
	}

	private WorldPoint[][] copyPoints(WorldPoint[][] points)
	{
		WorldPoint[][] copy = new WorldPoint[points.length][];
		for (int i = 0; i < points.length; i++)
		{
			copy[i] = points[i].clone();
		}
		return copy;
	}

	private void appendBorders(
		Path2D path, LocalPoint lp, WorldView wv, int plane, WorldPoint[][] points, int i, int j)
	{
		int halfTile = Perspective.LOCAL_HALF_TILE_SIZE;
		if (j == points[i].length - 1 || points[i][j + 1] == null)
		{
			appendBorder(path, lp, wv, plane, halfTile, halfTile - 1, -halfTile + 1, halfTile);
		}
		if (j == 0 || points[i][j - 1] == null)
		{
			appendBorder(path, lp, wv, plane, halfTile - 1, -halfTile, -halfTile + 1, -halfTile);
		}
		if (i == points.length - 1 || points[i + 1][j] == null)
		{
			appendBorder(path, lp, wv, plane, halfTile, -halfTile + 1, halfTile, halfTile - 1);
		}
		if (i == 0 || points[i - 1][j] == null)
		{
			appendBorder(path, lp, wv, plane, -halfTile, -halfTile + 1, -halfTile, halfTile - 1);
		}
	}

	private void appendBorder(
		Path2D path, LocalPoint lp, WorldView wv, int plane, int startOffsetX, int startOffsetY,
		int endOffsetX, int endOffsetY)
	{
		Point start = Perspective.localToCanvas(client,
			new LocalPoint(lp.getX() + startOffsetX, lp.getY() + startOffsetY, wv), plane);
		Point end = Perspective.localToCanvas(client,
			new LocalPoint(lp.getX() + endOffsetX, lp.getY() + endOffsetY, wv), plane);
		if (start != null && end != null)
		{
			path.moveTo(start.getX(), start.getY());
			path.lineTo(end.getX(), end.getY());
		}
	}

	private void renderPoly(Graphics2D graphics, Color borderColor, float borderWidth, Color fillColor, Shape polygon)
	{
		if (polygon != null)
		{
			graphics.setColor(borderColor);
			graphics.setStroke(new BasicStroke(borderWidth));
			graphics.draw(polygon);
			graphics.setColor(fillColor);
			graphics.fill(polygon);
		}
	}
}

