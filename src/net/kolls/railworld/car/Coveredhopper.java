package net.kolls.railworld.car;

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


import java.awt.Color;

import net.kolls.railworld.Distance;

/**
 * Covered hopper based on CSXT 225021
 * 
 * @author Steve Kollmansberger
 *
 */
public class Coveredhopper extends AbstractCar {
	// from CSXT 225021
	private static final Distance d = new Distance(41, Distance.Measure.FEET);
	
	public Color color() {
		return Color.green;
	}

	
	public Distance length() {
		return d;
	}

	
	public String show() {
		return "Covered hopper";
	}

	
	public int weight() {
		if (loaded()) return 131; else return 26;
	}

}
