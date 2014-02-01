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


import java.awt.Color;
import java.util.Set;

import net.kolls.railworld.io.SaveLoad;
import net.kolls.railworld.tuic.TrainPainter;


/**
 * Car represents a basic railcar, which may either be a loadable (cargo type) or not.
 * The Car class contains a registry for tracking a Car's location, as well as information
 * on its appearance and characteristics.
 * 
 * @author Steve Kollmansberger
 */
public interface Car extends SaveLoad {

	
	
	/**
	 * Indicates if the Car is currently loaded.
	 * 
	 * @return <code>true</code> if the car is loadable and is currently loaded.
	 */
	boolean loaded();
	
	/**
	 * Loads the current Car.  Do not call on a Car you have specified as not loadable.
	 *
	 */
	void load();
	
	/**
	 * Unloads the current Car.  Do not call on a Car you have specified as not loadable.
	 *
	 */
	void unload();
	
	/**
	 * Determine if the current Car can be loaded and unloaded; that is, does it carry cargo.
	 * 
	 * @return <code>true</code> if the Car may be loaded and unloaded.  Defaults to <code>true</code>.
	 */
	boolean isLoadable();

	
	
	/**
	 * For the current Car, returns the segments that the Car occupies, if any.  
	 * This should be considered read-only.
	 * 
	 * @return {@link Set} of {@link RailSegment}s.
	 */
	Set<RailSegment> segs();

	
	
	/**
	 * Return the Car's color.  The following color assignments exist:
	 * black: open hopper car
	 * blue: tank car
	 * cyan: intermodal
	 * dark gray: engine
	 * gray: <reserved, ballast color>
	 * green: covered hopper
	 * light gray: passenger
	 * magenta: auto rack
	 * orange: centerbeam flat car
	 * pink: boxcar
	 * red: caboose
	 * white: <reserved, l/u line color>
	 * yellow: stock car
	 * 
	 * @return The {@link Color} of the current Car.
	 */
	Color color();
	
	
	/**
	 * Indicates what the middle line (loaded) color should be.  Used by {@link TrainPainter}.
	 * 
	 * @return The {@link Color} of the middle line in the car
	 */
	Color midColor();
	
	/**
	 * Gives a human-readable word for the Car.  Should indicate only the type of the Car,
	 * not whether it is loaded or unloaded.
	 * 
	 * @return {@link String} indicating type of Car.
	 */
	String show();
	

	
	/**
	 * Returns the weight of this Car (taking load/unload into account, if necessary) in US Tons.
	 * 1 US Ton = 2000 US Pounds
	 * 
	 * @return <code>int</code> representing weight in tons.
	 */
	int weight();
	
	/**
	 * Returns the length of this Car.
	 * 
	 * @return {@link Distance} indicating the length of the Car.
	 */
	Distance length();
	
	
	/**
	 * Indicates if this car should appear in any user creation selection window.
	 * 
	 * @return true if the car is user creatable
	 */
	boolean canUserCreate();
	
	/**
	 * Indicates if this car provides power.
	 * 
	 * @return true if the car provides power (is a working engine)
	 */
	boolean isEngine();
	
	/**
	 * The distance between cars (shown as empty space).
	 */
	public final static Distance DIST_BETWEEN_CARS = new Distance(5, Distance.Measure.FEET);
	
	/**
	 * The width of a car.
	 */
	public final static Distance CAR_WIDTH = new Distance(9, Distance.Measure.FEET);
}
