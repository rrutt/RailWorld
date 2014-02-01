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
 * Finds the empty point of a train.  Used, for example, for reversing a train.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrainEndPointFinder extends TrainUIController {

	/**
	 * The endpoint, once found.
	 */
	public CLoc p;
	
	@Override
	public void segment(Car c, Line2D l) { }
	@Override
	public void car(Car c, CLoc b, CLoc pos) { p = pos; }



}
