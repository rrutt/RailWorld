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
 * A DLoc represents a location, direction, and rendering information.
 * The rendering information is stored as a series of lines which may be
 * stroked to display a Car or other item.
 * 
 * @author Steve Kollmansberger
 *
 */
public class DLoc {
	
	/**
	 * Construct a DLoc.
	 * 
	 * @param li The rendering lines.  May be <code>null</code>.
	 * @param nl A {@link CLoc}, indicating location and position.
	 */
	public DLoc(Line2D[] li, CLoc nl) { lines = li; newLoc = nl; }
	
	/**
	 * An array of lines to be drawn for this item.  Note that
	 * the lines may not be adjacent (for example, if part of the car
	 * is hidden).
	 */
	public Line2D[] lines;
	
	/**
	 * The head position associated with this item.
	 */
	public CLoc newLoc;
}
