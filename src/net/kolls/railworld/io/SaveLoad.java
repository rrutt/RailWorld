package net.kolls.railworld.io;

import java.util.Map;

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
 * Allows a class to specify data to save and restore data on load.  This is essentially
 * serialization; the reason for using a custom class rather than serializable
 * is so that scripts instances can be saved as well using the same technique
 * as regular classes.
 * 
 * Script classes are can't use newInstance, so we have an alternative here.
 * 
 * @author Steve Kollmansberger
 */
public interface SaveLoad {

	/**
	 * Store all persistent data into a map and return for saving.
	 * May return null if there is no data to save.  This will be treated
	 * the same as the empty map.
	 * 
	 * @return A list of key-value pairs to be saved.
	 */
	Map<String, String> save();
	
	/**
	 * Load from saved.
	 * 
	 * @param data Key value pairs saved by an instance of this class
	 * 
	 */
	void load(Map<String, String> data);
	
	
	/**
	 * like getClass().newInstance().  Allows script objects to also be copied.
	 * 
	 * @return A new instance of this class
	 */
	Object newInstance();
	
	/**
	 * 
	 * @return Give the name of this type so it can be reconstructed by the factory.
	 */
	String toString();
	
	
}
