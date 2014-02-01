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

/**
 * Given a position on the map, determine if it is within a train.  
 * If so, indicate which car it falls within.
 * 
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrainClickTest extends TrainUIController {
	private int x, y;
	
	/**
	 * If the position falls within a train, this will be set to the Car it hits.
	 * Otherwise null.
	 */
	public Car cc;
	
	/**
	 * Creates a new TrainClickTest for a given position.
	 * 
	 * @param vx The X coordinate on the map
	 * @param vy The Y coordinate on the map
	 */
	public TrainClickTest(int vx, int vy) {
		x = vx;
		y = vy;
		cc = null;
		pd = Math.pow(clickDist.pixels(), 2.0);
	}
	private double pd;
	
	// give us some leeway (1 ft)
	private static final Distance clickDist = new Distance(Car.CAR_WIDTH.feet()+1, Distance.Measure.FEET);
	
	@Override
	public void segment(Car c, Line2D l) {
		// to test if we click on the line

		
		if (l.ptSegDistSq(x, y) <= pd) cc = c;

	}
	@Override
	public void car(Car c, CLoc b, CLoc e) { }


}
