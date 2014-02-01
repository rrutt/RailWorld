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


/**
 * The Distance class maintains the current scale and zoom information globally, as well as managing
 * individual distance conversion (feet to pixels).  Distances should be created as feet
 * and then rendered as pixels; the pixel value will change according to the scale
 * and the zoom.  As the user may alter the zoom at any time, the pixel values
 * should not be cached.
 * <p>
 * To help with the display issues,
 * we provide static point and line scaling routines.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Distance {
	private double distFeet;
	
	
	
	/**
	 * The numeric value of each distance may be stored as either pixels
	 * or feet.  The difference between these depends on the map scale.
	 */
	public enum Measure {
		/**
		 * A value is in feet
		 */
		FEET, 
		/**
		 * A value is in pixels
		 */
		PIXELS };
	
	
	/**
	 * This value must be changed prior to constructing any segments, as the segments
	 * convert pixels into feet, depending on a correct scale.
	 */
	public static double feetPerPixels = 0.666; 
	
	/**
	 * The default zoom for new maps or partial displays not subject to user zooming.
	 * 
	 * @return A zoom factor based on the current {@link #feetPerPixels}.
	 */
	public static double getDefaultZoom() {
		return feetPerPixels / 3.0;
	}
	
	/**
	 * Create a new distance measurement based on either feet or pixels.  Creating a value
	 * based on pixels will be converted internally to feet immediately based on the current scaling.
	 * Thus, if you are creating a value when the scaling is not yet known, you must absolutely
	 * use feet.
	 * 
	 * @param val The length.
	 * @param measure The unit of the length, as given in {@link Measure}.
	 */
	public Distance(double val, Measure measure) {
		if (measure == Measure.FEET) distFeet = val; else distFeet = toFeet(val); 
	}
	
	/**
	 * Compute the pixel length.
	 * 
	 * @return A <code>double</code> indicating the length in pixels.  For integer values, do not round this one, as it may come to zero.  
	 * Use {@link #iPixels() iPixels} instead.
	 */
	public double pixels() {
		return (distFeet / feetPerPixels) /* * zoom */;
	}
	
	/**
	 * The feet length of this distance.
	 * 
	 * @return A <code>double</code> indicating the length in feet.
	 */
	public double feet() {
		return distFeet;
	}
	
	/**
	 * The length, in pixels.  For any length greater than 0 feet, this value is guaranteed to be at least 1,
	 * regardless of scale.
	 * 
	 * @return An <code>int</code>, containing the pixel length. 
	 */
	public int iPixels() {
		int a = (int)(Math.round(pixels()));
		if (a > 0 || distFeet == 0) return a; else return 1; // no zero distances
	}
	
	/**
	 * Convert a given length in feet into pixels.
	 * 
	 * @param feet The length in feet
	 * @return The length in pixels based on the current {@link #feetPerPixels}
	 */
	public static double toPixels(double feet) {
		return (feet / feetPerPixels) /* * zoom */ ;
	}
	
	/**
	 * Convert a given length in pixels into feet/
	 * 
	 * @param pixels The length in pixels
	 * @return The length in feet based on the current {@link #feetPerPixels}
	 */
	public static double toFeet(double pixels) {
		return pixels*feetPerPixels;
	}
	
}
