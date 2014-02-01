package net.kolls.railworld.play;

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

import java.awt.geom.Point2D;

import net.kolls.railworld.Train;

/**
 * Describes an accident and the train(s) involed.
 * In the future, this may be replaced with an abstract class
 * or interface, and the various kinds of accidents
 * be subclasses.  This would allow scripts to extend the game
 * and add their own accident types (e.g. failing to blow horn before crossing)
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class RailAccident extends RuntimeException {
	
	/**
	 * The trains involved in the accident.  t1 must not be null.  
	 */
	public Train t1;
	
	/**
	 * The other train.  t2 may be null if there is
	 * only one train involved.
	 */
	public Train t2;
	
	
	/**
	 * The title of this accident type
	 * 
	 * @return Title based on kind of accident
	 */
	public abstract String title();
	
	/**
	 * The midbody of the report is the section that comes before the second train, if any,
	 * is described but after the first train is described.
	 * It is a sentence fragment like "collided head-on with a"
	 * (which becomes "A southbound 40-ton train collided head-on with a 30-ton train...")
	 * Do not end the phrase with a period or start with a capital.
	 * If you want the direction for the second train, you can insert {2d} into the string
	 * and that will be replaced by the second train direction.
	 * 
	 * 
	 * @return midbody phrase
	 */
	public abstract String midbody();
	
	
	/**
	 * The location of the accident.
	 */
	public Point2D pos;
	
	
	/**
	 * Create a rail accident.
	 * 
	 * @param first The first train.  Must not be null.
	 * @param second The second train.  May be null if only one train involved.
	 * @param p The point on the map where the accident occured.
	 */
	public RailAccident(Train first, Train second, Point2D p) {
		super();
		
		t1 = first;
		t2 = second;
		pos = p;
		
	}
}
