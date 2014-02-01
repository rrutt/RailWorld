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


import net.kolls.railworld.Distance;

import java.awt.*;

// based on GE Dash 7
/**
 * Locomotive based on GE Dash 7
 * 
 * @author Steve Kollmansberger
 */
public class Engine extends AbstractCar {
	private static final Distance d = new Distance(67, Distance.Measure.FEET);
	public Distance length() { return d; } // 67 ft
	@Override
	public boolean loaded() { return true; }
	public int weight() { return 210;} // 420 Klbs
	public Color color() { 
		return Color.darkGray;
	}
	public String show() { return "Engine"; }
	@Override
	public void load() { }
	@Override
	public void unload() { }
	@Override
	public boolean isLoadable() { return false; }
	@Override
	public boolean isEngine() {
		return true;
	}
}
