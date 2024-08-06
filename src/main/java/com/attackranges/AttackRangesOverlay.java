package com.attackranges;

import static com.attackranges.AttackRangesUtils.isOuterTile;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

@Slf4j
class AttackRangesOverlay extends Overlay
{
	private final Client client;
	private final AttackRangesConfig config;
	private final AttackRangesPlugin plugin;

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
		renderPlayer(graphics);
		return null;
	}

	private void renderPlayer(Graphics2D graphics)
	{
		if (!AttackRangesUtils.shouldRenderForPlayer(plugin, config, client))
		{
			return;
		}

		drawAttackableSquares(graphics, client.getLocalPlayer(), plugin.playerVisiblePoints, config.rangeBorderColor());
	}

	private void drawAttackableSquares(Graphics2D graphics, Actor actor, WorldPoint[][] points, Color color)
	{
		WorldView wv = actor.getWorldView();
		for (int i = 0; i < points.length; i++)
		{
			for (int j = 0; j < points[i].length; j++)
			{
				WorldPoint wp = points[i][j];
				if (wp == null)
				{
					continue;
				}
				switch (config.displayMode())
				{
					case TILES:
						drawTile(graphics, wp, wv, color);
						continue;
					case EDGE_TILES:
						drawTile(graphics, wp, wv, points, i, j, color);
						continue;
					case BORDER:
						drawBorders(graphics, wp, wv, points, i, j, color);
				}
			}
		}
	}

	private void drawTile(Graphics2D graphics, WorldPoint wp, WorldView wv, Color borderColor)
	{
		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null)
		{
			Polygon p = Perspective.getCanvasTilePoly(client, lp);
			if (p != null)
			{
				OverlayUtil.renderPolygon(
					graphics,
					p,
					borderColor,
					config.rangeFillColor(),
					new BasicStroke(config.borderSize()));
			}
		}
	}
	private void drawTile(
		Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j, Color borderColor)
	{
		if (!isOuterTile(points, i, j))
		{
			return;
		}

		drawTile(graphics, wp, wv, borderColor);
	}


	private void drawBorders(
		Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j, Color borderColor)
	{
		graphics.setColor(borderColor);
		graphics.setStroke(new BasicStroke(config.borderSize()));
		drawTopBorder(graphics, wp, wv, points, i, j);
		drawBottomBorder(graphics, wp, wv, points, i, j);
		drawLeftBorder(graphics, wp, wv, points, i, j);
		drawRightBorder(graphics, wp, wv, points, i, j);

		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null)
		{
			graphics.setColor(config.rangeFillColor());
			Polygon p = Perspective.getCanvasTilePoly(client, lp);
			if (p != null)
			{
				graphics.fill(p);
			}
		}
	}

	private void drawTopBorder(Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j)
	{
		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null && (j == points.length - 1 || points[i][j + 1] == null))
		{
			Point start = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() + Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() + Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			Point end = Perspective.localToCanvas(client,
				new LocalPoint(
					lp.getX() - Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() + Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			drawLine(graphics, start, end);
		}
	}

	private void drawBottomBorder(Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j)
	{
		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null && (j == 0 || points[i][j - 1] == null))
		{
			Point start = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() + Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() - Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			Point end = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() - Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() - Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			drawLine(graphics, start, end);
		}
	}

	private void drawLeftBorder(Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j)
	{
		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null && (i == points.length - 1 || points[i + 1][j] == null))
		{
			Point start = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() + Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() - Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			Point end = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() + Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() + Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			drawLine(graphics, start, end);
		}
	}

	private void drawRightBorder(Graphics2D graphics, WorldPoint wp, WorldView wv, WorldPoint[][] points, int i, int j)
	{
		LocalPoint lp = LocalPoint.fromWorld(wv, wp);
		if (lp != null && (i == 0 || points[i - 1][j] == null))
		{
			Point start = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() - Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() - Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			Point end = Perspective.localToCanvas(client, new LocalPoint(
					lp.getX() - Perspective.LOCAL_HALF_TILE_SIZE,
					lp.getY() + Perspective.LOCAL_HALF_TILE_SIZE,
					wv),
				wp.getPlane());
			drawLine(graphics, start, end);
		}
	}

	private void drawLine(Graphics2D graphics, Point start, Point end)
	{
		if (start == null || end == null)
		{
			return;
		}
		graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
	}
}

