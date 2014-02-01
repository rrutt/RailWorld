package net.kolls.railworld;

/*
 * Copyright (C) 2010 Steve Kollmansberger
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;



import javax.swing.SwingUtilities;

import net.kolls.railworld.opening.Options;

/**
 * The RailCanvas is one of the workhorses of the Rail World game.  
 * A RailCanvas represents the main drawing window where the image is
 * drawn and overlaid with rail segments and trains.  The rail canvas
 * is also where the user clicks to select trains and operate segment devices,
 * such as switches.
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class RailCanvas extends Canvas implements MouseListener,
		MouseMotionListener {

	
	/**
	 * The original, unmodified source image
	 */
	public BufferedImage orig_src;
	
	
	/**
	 * The miniviewer image
	 */
	protected BufferedImage minisource;

	
	/**
	 * The rail segments.  This should be considered read-only
	 */
	public RailSegment [] la;
	

	/**
	 * The left edge of the current viewing location on the image
	 */
	protected int vx;
	
	/**
	 * The upper edge of the current viewing location on the image
	 */
	protected int vy;
	
	/**
	 * The mouse location during a drag (x) 
	 */
	protected int mx;
	
	/**
	 * The mouse location during a drag (y)
	 */
	protected int my;
	
	/**
	 * The mini-viewer for this canvas
	 */
	protected MiniViewer miniv;
	
	
		
	/**
	 * Construct a new rail canvas
	 * 
	 * @param s The source image (@link{#orig_src}) to use.
	 * @param lines The rail segments (@link{#la}) to use.
	 * @param mini The mini viewer (@link{#miniv}) to use.
	 */
	public RailCanvas(BufferedImage s, RailSegment[] lines, MiniViewer mini) {
		orig_src = s;
		la = lines;
		vx = vy = 50;
		mx = my = 0;
		
		miniv = mini;
		miniv.rc = this;
		
		
			
		addMouseMotionListener(this);
		addMouseListener(this);
		
		setIgnoreRepaint(true);
		
		antialias = Options.getAntialias() ? RenderingHints.VALUE_ANTIALIAS_ON : 
			RenderingHints.VALUE_ANTIALIAS_OFF;
		
		
		strategy = null;
		
	}
	
	private BufferStrategy strategy;
	
	
	/**
	 * 
	 * @return The current positional offset
	 */
	public Point2D getVXY() {
		return new Point2D.Double(vx, vy);
	}
	
	/**
	 * Calculates the total length of all rail segments in feet.
	 * 
	 * @return Sum of all rail segment lengths, in feet.
	 */
	public double trackLength() {
		double len = 0;
		for (int i = 0; i < la.length; i++)
			len += la[i].length().feet();
		return len;
	}
	
	
	
	/**
	 * Deallocate images for garbage collection.
	 *
	 */
	public void stop() {
		
		Thread.yield();
		
		// allow images to be garbage collected
		orig_src = null;
	}
	
	
		
	/**
	 * Recompute all segment details
	 *
	 */
	public void recomp() {
		
		
		for (int i = 0; i < la.length; i++)
			la[i].recomp();
			
		
	}
	
	
	
	/**
	 * Returns the center of the currently displayed area.
	 * 
	 * @return A {@link Point2D} representing the center point of display on the image.
	 */
	public Point2D getCenterPoint() {
		//return new Point2D.Double(vx+(getWidth()/2.0)/Distance.zoom,vy+(getHeight()/2.0)/Distance.zoom);
		return new Point2D.Double(vx+getWidth()/RailCanvas.zoom/2.0,vy+getHeight()/RailCanvas.zoom/2.0);
		
	}
	
	/**
	 * Move the display to attempt to accomodate the given image coordinates as a center point.
	 * 
	 * @param x  X position on static image
	 * @param y  Y position on static image
	 */
	public void submitCenterCoords(int x, int y) {
		submitCoords((int)(x - getWidth()/RailCanvas.zoom/2.0), (int)(y - getHeight()/RailCanvas.zoom/2.0));
		
	}
	
	/**
	 * Move the display to attempt to accomodate the given upper-left coordinates on the static image.
	 * 
	 * @param x The x coordinate on the image
	 * @param y The y coordinate on the image
	 * 
	 */
	public void submitCoords(int x, int y) {
		int sh = (int)(orig_src.getHeight() * RailCanvas.zoom), sw = (int)(orig_src.getWidth()  * RailCanvas.zoom);
		int h = getHeight() , w = getWidth();
		
		// in order for this to work, we convert everything into pixels as they would
		// appear on the display
		vx = (int)(x * RailCanvas.zoom);
		vy = (int)(y * RailCanvas.zoom);
		
		if (vx > sw - w) vx = sw - w;
		if (vy > sh - h) vy = sh - h;
		if (vx < 0) vx = 0;
		if (vy < 0) vy = 0;
		
		// special case: viewport wider than image
		if (sw < w) vx = (sw-w)/2;
		if (sh < h) vy = (sh-h)/2;
		
		miniv.setPos(vx, vy);
		
		// then convert back to source pixels
		vx /= RailCanvas.zoom;
		vy /= RailCanvas.zoom;
		
		
		
		

	}
	
	
	/**
	 * Calculate the angle of a line, in radians.
	 * 
	 * @param bl A {@link Line2D} to find the angle of.
	 * @return A <code>double</code> value, representing angle in radians.
	 */
	public static double lineAngle(Line2D bl) {
		double dx, dy, da;
		dx = bl.getX2() - bl.getX1();
		dy = bl.getY2() - bl.getY1();
		
		da = Math.atan2(dy, dx);
		

		return da;
	}
	
	/**
	 * Find a position relative to an existing line.  This is used, for example, to draw
	 * the accepting cars or the switch selector.  The angle is relative to the given line;
	 * for example, a 90 degree angle would be perpendicular to the line no matter
	 * which way the line faced.  The point is a fixed starting point, and the len is the distance
	 * to travel at the given angle.
	 * 
	 * @param bl  A {@link Line2D} to start from.
	 * @param myPoint A {@link Point2D} as a fixed starting point.
	 * @param angle An angle, in radians, relative to the line given.
	 * @param len A {@link Distance} at the given relative angle to move.
	 * @return A {@link Line2D} from the given starting point to the end along the relative angle.
	 */
	public static Line2D angle(Line2D bl, Point2D myPoint, double angle, Distance len) {

		double dx, dy, da;
		double lenp = len.pixels();
		
		da = lineAngle(bl);
			
		da += angle; 
		
		dy = Math.sin(da) * lenp;
		dx = Math.cos(da) * lenp;
		

		dx += myPoint.getX();
		dy += myPoint.getY();
		
		return new Line2D.Float(myPoint, new Point2D.Double(dx,dy));
	}
	
	/**
	 * Draws a shadowed text on a graphics device.
	 * 
	 * @param ofg A {@link Graphics2D} to draw on.
	 * @param x Position to draw the text.
	 * @param y
	 * @param text The actual text to draw.
	 * @param size The size, in pixels, of the text.
	 * @param c The {@link Color} of the text.
	 * @param angle The angle, in radians, to draw the text.
	 * @param centered The position given represents the center of the text?
	 */
	public static void drawOutlineFont(Graphics2D ofg, int x, int y, String text, int size, Color c, double angle, boolean centered) {
		
		
		
		
		
		
		Font font = new Font("SansSerif", Font.PLAIN, size);
		FontMetrics m = ofg.getFontMetrics(font);
		
		int lblw = m.stringWidth(text);
		
		int lblh = m.getHeight(); 
		int cw = centered ? lblw/2 : 0;
		int ch = centered ? lblh/2 : 0;
		
		
		Font f = ofg.getFont();
		AffineTransform r = new AffineTransform();
		
		r.rotate(angle, cw, ch);
		
		ofg.setFont(font.deriveFont(r));
		ofg.setColor(new Color(0, 0, 0, c.getAlpha()));
		ofg.drawString(text, x-cw, y+ch);
		ofg.setColor(c);
		ofg.drawString(text, x-cw+1, y+ch+1);
		ofg.setFont(f);
		
		
		
	
	}
	
	private void drawScale(Graphics2D ofg, int x, int y) {
		// find scale approx 50 pixels wide
		int ft = 25;
		
		
		while (Distance.toPixels(ft)*RailCanvas.zoom < 50) {
			ft += 25;
		}
		
		int dist = (int)(Distance.toPixels(ft) * RailCanvas.zoom);
		
		Line2D s,f,c;
		
		s = new Line2D.Double(x,y,x,y+10);
		f = new Line2D.Double(x+dist,y,x+dist,y+10);
		c = new Line2D.Double(x,y+5,x+dist,y+5);
		
		
		
		
		
		
		ofg.setStroke(new BasicStroke(3));
		ofg.setColor(Color.white);
		ofg.draw(s);
		ofg.draw(c);
		ofg.draw(f);
		
		ofg.setStroke(new BasicStroke(1));
		ofg.setColor(Color.black);
		ofg.draw(s);
		ofg.draw(c);
		ofg.draw(f);
		
		String lbl = Integer.toString(ft)+ " ft";
		
		drawOutlineFont(ofg, x+(dist/2),y+15, lbl, 12, Color.white, 0, true);
		
		
	}
	
	
	
	/**
	 * Draws a portion of the display
	 * 
	 * @param center  A {@link Point2D} representing the center position of the image.
	 * @param g The {@link Graphics2D} to draw into.  Drawing is always performed at (0,0) in the target graphics.
	 * @param width The width of the draw.
	 * @param height The height of the draw.
	 * @param useZoom The zoom factor to use for this draw.  Normally {@link RailCanvas#zoom}
	 * @param detailed Should high expense items like scripts be drawn as well?
	 */
	public void draw(Graphics2D g, Point2D center, int width, int height, double useZoom, boolean detailed) {
		
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		g.clip(new Rectangle2D.Double(0, 0, width, height));
		
		
		int hvx = (int)(center.getX() - width/2);
		int hvy = (int)(center.getY() - height/2);
		
		
		
		
		
		AffineTransform orgat = g.getTransform();
		
		
		g.scale(useZoom, useZoom);
		
		
		g.translate(-hvx,-hvy);
		g.drawImage(orig_src, 0, 0, this);
		
		for (int z = 1; z < 3;z++) {
			for (int i = 0; i < la.length; i++) {
				la[i].draw(z,g);
			}
		}
		
		
		doPaint(g, hvx, hvy, detailed);
		
		g.setTransform(orgat);
		

		
		
	}
	
	/**
	 * The rendering hints for antialias (either VALUE_ANTIALIAS_ON or VALUE_ANTIALIAS_OFF)
	 */
	private Object antialias;


	//1089.yrd = 3 ft/px
	// 1.jpg = 75ft/50px = 0.666 ft/px
	/**
	 * The zoom value may be changed at any time, but appropriate redraws may be needed.
	 */
	public static double zoom = 0.5;
	
	/**
	 * Draw the rail canvas and its miniviewer, if appropriate.
	 */
	public void drawCanvas() {
		
		
		if (strategy == null) {
			createBufferStrategy(2);
			strategy = getBufferStrategy();
		}
		
		Graphics2D ofg = (Graphics2D)strategy.getDrawGraphics();
	    
		RenderingHints orh = ofg.getRenderingHints();
		
		orh.put(RenderingHints.KEY_ANTIALIASING, antialias);
		
		ofg.setRenderingHints(orh);
		
		
		int h = getHeight(), w = getWidth();

		if (w < 1 || h < 1) return;

		
		draw(ofg, new Point2D.Double(vx+(w/2), vy+(h/2)), w, h, RailCanvas.zoom, true);

		
		drawScale(ofg, 10, h-50);
	    strategy.show();

	    if (orig_src != null) {
	    	miniv.config(getWidth(), getHeight(), (int)(RailCanvas.zoom * orig_src.getWidth()), (int)(RailCanvas.zoom*orig_src.getHeight()));
	    	miniv.drawCanvas();
	    }

	    //painted = true;
	}
	
	

	/**
	 * Finds the area of the entire playing map at current zoom level.
	 * 
	 * @return Dimension indicating area.
	 */
	public Dimension areaSize() {
		return new Dimension((int)(orig_src.getWidth() * RailCanvas.zoom), (int)(orig_src.getHeight() * RailCanvas.zoom));
	}
	
	/**
	 * Given the graphics context of the mini image, paint it.
	 * 
	 * @param ofg A {@link Graphics2D} context for the mini image.
	 */
	public abstract void doMiniPaint(Graphics2D ofg);
	
	/**
	 * Paint the main image.
	 * 
	 * @param ofg A {@link Graphics2D} context for the main image.
	 * @param hvy The vx coordinate to use
	 * @param hvx The vy coordinate to use
	 * @param detailed Should expensive items also be drawn?
	 */
	protected abstract void doPaint(Graphics2D ofg, int hvx, int hvy, boolean detailed);
	
	
	/**
	 * Transforms a point on the canvas to a point on the image.
	 * 
	 * @param e The mouse event which includes the point.
	 * @return A point on the static image
	 */
	protected final Point2D transform(MouseEvent e) {
		return new Point2D.Double((e.getX() / RailCanvas.zoom)+vx, (e.getY() / RailCanvas.zoom)+vy);
	}
	
	/**
	 * Transforms a point on the canvas to a point on the image.
	 * 
	 * @param p The point on the canvas to transform
	 * @return A point on the static image
	 */
	protected final Point2D transform(Point2D p) {
		
		return new Point2D.Double((p.getX() / RailCanvas.zoom)+vx, (p.getY() / RailCanvas.zoom)+vy);
		
		
	}
	
	/**
	 * Called if the left mouse button is held and dragged.
	 * 
	 * @param e The mouse event generated
	 */
	public void leftDrag(MouseEvent e) { }
	
	/**
	 * Called if the mouse is moved without the right mouse button down
	 * 
	 * @param e The mouse event generated
	 */
	public void leftMove(MouseEvent e) {	}
	
	
	/**
	 * Called if the left mouse button is released
	 * 
	 * @param e The mouse event generated
	 */
	public void leftRelease(MouseEvent e) { }
	
	
	/**
	 * Called if the left mouse button is pressed
	 * 
	 * @param e The mouse event generated
	 */
	public void leftPress(MouseEvent e) { }
	
	
	
	public void mouseClicked(MouseEvent e) { }
	
	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e) {	}

	public final void mousePressed(MouseEvent e) {
		
		boolean b = SwingUtilities.isLeftMouseButton(e);
		
		if (b) leftPress(e);
	}

	public final void mouseReleased(MouseEvent e) {
		

		boolean b = SwingUtilities.isLeftMouseButton(e);
		
		if (b) leftRelease(e);
		
		
	}

	/**
	 * If the right mouse button is dragged, moves the displayed location.
	 * Otherwise delegates to {@link #leftDrag(MouseEvent)}.
	 */
	public final void mouseDragged(MouseEvent e) {
		
		boolean b = SwingUtilities.isRightMouseButton(e);
		
		if (b) {
		
		
			int mxp, myp;
		
			mxp = e.getX();
			myp = e.getY();
		
			int dx = (int)((mxp - mx) / RailCanvas.zoom);
			int dy = (int)((myp - my) / RailCanvas.zoom);

			if (Math.abs(dx) < 2 && Math.abs(dy) < 2) return;
			
			
			
			if (mx > -1) {
				submitCoords( (vx - dx), (vy - dy));
			}
			mx = mxp; my = myp;
		} else leftDrag(e);

	}
	
	public final void mouseMoved(MouseEvent e) { 
		boolean b = SwingUtilities.isRightMouseButton(e);
		
		mx = my = -1;
		
		if (b == false) leftMove(e);
	}
		
	
	
	
	
}

