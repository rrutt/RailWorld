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


import java.awt.geom.*;

/**
 * Deals with issues regarding a train as it appears on the map.  Once instantiated, the programmer
 * calls the act method, which in turns calls segment and car methods repeatadly.
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class TrainUIController {
	/**
	 * For each visible segment (there may be multiple segments per car, or even 0 if the car is hidden).
	 * 
	 * @param c  The {@link Car} involved.
	 * @param l The current segment.  There may be multiple calls to segment per Car.
	 */
	public abstract void segment(Car c, Line2D l);
	
	/**
	 * For each Car, including a beginning and ending position.  These are given regardless of whether or not
	 * the Car is all or partially hidden.  
	 * 
	 * @param c The {@link Car} involved.
	 * @param begin Starting position.
	 * @param end Final position.
	 */
	public abstract void car(Car c, CLoc begin, CLoc end);

	
	
	
	
	/**
	 * "Walk" the train and the segments it is on and call {@link #segment(Car, Line2D) segment}
	 * and {@link #car(Car, CLoc, CLoc) car} appropriately.
	 * 
	 * @param t  Train to act on.
	 */
	public final void act (Train t)  {
		if (t.pos.r == null) return;
		CLoc pos = t.pos.reverse(); 
		DLoc d;
		Car[] train = t.array();
		

		Line2D[] lines;
		int i, j;
		
		
			for ( i = 0; i < train.length; i++) {
				if (pos.r == null) break;
				
				train[i].segs().clear();
				d = pos.segFwd(train[i].length(), train[i], t);
				
				lines = d.lines;
				for (j = 0; j < lines.length; j++) {
					// do behavior
					segment(train[i], lines[j]);
					
				}

				
				//if (d.newLoc.r == null) break;
				car(train[i], pos.reverse(), d.newLoc);

				// between cars, move forward empty space			
				d = d.newLoc.segFwd(Car.DIST_BETWEEN_CARS, null, t);
				if (d.newLoc.r == null) break;
				pos = d.newLoc;
			}
			

	}

}
