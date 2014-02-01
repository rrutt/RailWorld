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

import java.awt.*;
import java.util.*;

import net.kolls.railworld.Car;
import net.kolls.railworld.RailSegment;

/*
 * Color tables:
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
 * 
 */

/**
 * AbstractCar contains some reasonable defaults to avoid repetition in creating standard cargo car types.
 * 
 * @author Steve Kollmansberger
 */
public abstract class AbstractCar implements Car {
	
	/**
	 * Indicates if the Car is currently loaded (with cargo) or not.
	 * Should be false if the Car is not loadable.
	 */
	protected boolean isLoaded;
	
	
	public boolean loaded() { return isLoaded; }
	
	
	public void load() { isLoaded = true; }
	
	
	public void unload() { isLoaded = false; }
	
	
	public boolean isLoadable() { return true; }

	private Set<RailSegment> segs;
	
	/**
	 * Constructs a new Car.  Parameters of a Car are set using inheritance.
	 *
	 */
	public AbstractCar() { segs = new HashSet<RailSegment>(); }
	
	
	public final Set<RailSegment> segs() { return segs; }

	
	@Override
	public final boolean equals(Object o) {
		Car c;
		if (o instanceof Car) c = (Car)o; else return false;
		return (c.show().equals(show()) && c.loaded() == loaded());
	}
	
	
	
	/**
	 * By default,  if the car is not loaded, or not loadable, show a solid car.
	 * Otherwise, show a white mid line.
	 */
	public Color midColor() {
		if (isLoadable() && loaded() == false) return Color.white;
		return color();
	}
	
	
	
	@Override
	public String toString() { return show(); }
	
	
	
	/**
	 * Default implementation saves only whether or not the car is loaded.
	 */
	public Map<String, String> save() {
		Hashtable<String, String> h = new Hashtable<String, String>();
		if (isLoadable()) h.put("Loaded", loaded() ? "Yes" : "No");
		return h;
		
	}
	
	/**
	 * Default implementation only loads whether or not the car is loaded.
	 */
	public void load(Map<String, String> m) {
		if (isLoadable()) {
			if (m.get("Loaded").equals("Yes")) load(); else unload();
		}
	}
	
	public boolean canUserCreate() { return true; }
	
	public Object newInstance() { 
		try {
			return getClass().newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean isEngine() {
		return false;
	}
	
}
