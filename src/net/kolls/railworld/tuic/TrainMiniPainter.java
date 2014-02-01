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

import java.awt.*;
import java.awt.geom.*;

/**
 * Paints a mini version of the train on the mini viewer.  
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrainMiniPainter extends TrainUIController {

	private Stroke ms;
	
	
	private Graphics2D g;
	
	
	
	private double xscale, yscale;
	
	/**
	 * 
	 * @param mg The graphics context to draw on
	 * @param xs The X scale of the miniviewer
	 * @param ys The Y scale of the miniviewer
	 */
	public TrainMiniPainter(Graphics2D mg, double xs, double ys) { 
		// paint all cars
		ms = new BasicStroke(2.0f);
		
		
		
		g = mg;
		
		

		xscale = xs;
		yscale = ys;
	}
	@Override
	public void segment(Car c, Line2D l) {
		

		g.setPaint(c.color());

		// scale line l
		Line2D nl = new Line2D.Double(l.getX1() * xscale, l.getY1() * yscale, l.getX2() * xscale, l.getY2() * yscale);

		g.setStroke(ms);
		g.draw(nl);

		
	}
	@Override
	public void car(Car c, CLoc b, CLoc pos) { }

}
