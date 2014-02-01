package net.kolls.railworld.tuic;

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


import net.kolls.railworld.*;

import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Paints a train on the map.  This follows each segment, and paints the car or partial car onto it.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrainPainter extends TrainUIController {

	private Stroke ms, ls;
	
	private RailCanvas myRC;
	private Graphics2D g;
	private boolean followMe;
	private Car selected;
	
	/**
	 * Did we submit new coords for this train yet?
	 */
	public boolean didFollow;
	
	/**
	 * Create a new TrainPainter.
	 * 
	 * @param rc  The {@link RailCanvas} for submitting follow-me coordinates.
	 * @param mg The graphics context to draw on
	 * @param f Should we follow (move display to) the given car sel?
	 * @param sel The selected car.  A green selected arrow will be drawn on it.
	 */
	public TrainPainter(RailCanvas rc, Graphics2D mg, boolean f, Car sel) { 
		// paint all cars
		ms = new BasicStroke(Car.CAR_WIDTH.iPixels(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		ls = new BasicStroke(Math.round(Car.CAR_WIDTH.pixels() / 3.0), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
		selected = sel;
		myRC = rc;
		g = mg;
		followMe = f;
		didFollow = false;
	}
	@Override
	public void segment(Car c, Line2D l) {
		

		g.setPaint(c.color());
		g.setStroke(ms);
		g.draw(l);

		if (c.color() != c.midColor()) {
			g.setPaint(c.midColor());
			g.setStroke(ls);
			g.draw(l);
		}
		

	}
	
	@Override
	public void car(Car c, CLoc begin, CLoc end) { 
		if (myRC == null) return;

		Point2D p1 = begin.getPoint();
		Point2D p2 = end.getPoint();
		Line2D l = new Line2D.Double(p1, p2);

		if (selected == c) { 
			if (followMe && p1.getX() >= 0 && p1.getY() >= 0) {
				// check if we need to adjust the view coordinates
				// if following
				didFollow = true;
				
				myRC.submitCenterCoords((int)p1.getX(), (int)p1.getY());
			}
			g.setPaint(Color.green);
			
			Distance d = new Distance(Car.CAR_WIDTH.feet() * 1.5, Distance.Measure.FEET);
			g.setStroke(ls);
			g.draw(RailCanvas.angle(l, p1, Math.PI / 4.0, d));
			g.draw(RailCanvas.angle(l, p1, -1.0*Math.PI / 4.0, d));
				
			g.setPaint(Color.white);
			

		}

	}
	
	
	/**
	 * Generates an image representing the Car.  This may be used for train builders or informational panes.
	 * This is not used for actually rendering a Car, because Cars may be at various angles or or partially visible.
	 * The rendering size depends on the current zoom level.
	 *
	 * @param c Car to generate image for
	 * @param mp A component used to derive the {@link GraphicsConfiguration}
	 * @return {@link BufferedImage} containing the Car.
	 */
	public static final BufferedImage image(Car c, Component mp) {
		
		
		GraphicsConfiguration gfxc = mp.getGraphicsConfiguration();
		
		int width = Car.CAR_WIDTH.iPixels();
		int w2 = width/2;
		//if (width % 2 == 1) w2++;
		
		int len = c.length().iPixels();
		
		
		
		BufferedImage bi = gfxc.createCompatibleImage((int)(len * Distance.getDefaultZoom()),
				(int)(width * Distance.getDefaultZoom()));
		
		Graphics2D ofg = bi.createGraphics();
		ofg.clearRect(0, 0, len, width);
		ofg.scale(Distance.getDefaultZoom(), Distance.getDefaultZoom());
		
		TrainPainter tp = new TrainPainter(null, ofg, false, null);
		tp.segment(c, new Line2D.Double(0,w2,len,w2));
		ofg.dispose();
		
		return bi;
		
	}

}
