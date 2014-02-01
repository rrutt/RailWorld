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


import java.awt.geom.Line2D;

import net.kolls.railworld.CLoc;
import net.kolls.railworld.Car;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.TrainUIController;

/**
 * Determine the direction of a train in both radians and words.
 * Only the lead car is considered; to indicate that a new train is being tested,
 * call the {@link #reset()} method between trains.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrainDirectionFinder extends TrainUIController {

	/**
	 * When the direction is found, this will contain the angle of the lead car in radians.
	 */
	public double angle;
	
	/**
	 * When the direction is found, this will describe the direction of the lead car.
	 * For example, "south" or "northwest"
	 */
	public String adesc;
	
	/**
	 * Prepare for a new train.
	 *
	 */
	public void reset() {
		angle = -1;
	}
	
	/**
	 * Create a train direction finder
	 */
	public TrainDirectionFinder() {
		super();
		angle = -1;
	}
	
	@Override
	public void car(Car c, CLoc begin, CLoc end) {	}

	@Override
	public void segment(Car c, Line2D l) { 
		if (angle == -1) {
			angle = RailCanvas.lineAngle(l);
			
			
			// take the line and place it in a box
			// with the minimum as 0,0
			// then find the point 1
			// which quadrant is it in?
			
			double minx = Math.min(l.getX1(), l.getX2());
			double miny = Math.min(l.getY1(), l.getY2());
			
			double dx = Math.abs(l.getX1() - l.getX2());
			double dy = Math.abs(l.getY1() - l.getY2());
			
			double width, height;
			width = height = Math.max(dx, dy);
			
			
			// here we need to center the point if the sides are not of equal size
			double px = l.getX1() - minx + ((width - dx) / 2);
			double py = l.getY1() - miny + ((height - dy) / 2);
			
			int x = 0, y = 0;
			
			if (px < width / 3) x = -1;
			else if (px < 2*width / 3) x = 0;
			else x = 1;
			
			if (py < height / 3) y = -1;
			else if (py < 2*height / 3) y = 0;
			else y = 1;
			
			
			
			String s = "";
			
			if (y == -1) s = "north";
			if (y == 1) s = "south";
			
			if (x == -1) s += "west";
			if (x == 1) s += "east";
			
			adesc = s;
		}
			
	}

}
