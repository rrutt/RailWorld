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


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import net.kolls.railworld.io.SaveLoad;



/**
 * A factory that creates instances by name and is extensible.
 * New types can be added (e.g. by plugins)  and then instantiated by other classes.
 * Works with the save/load system to allow saving and restoring of various classes.
 * Type parameter is supertype of all the items for that factory.
 * 
 * @param <T> The type of object to create in the factory.  Must implement {@link SaveLoad}
 * 
 * 
 * @see net.kolls.railworld.io.SaveLoad
 * @author Steve Kollmansberger
 * 
 * 
 */
public class ExtensibleFactory<T extends SaveLoad> {
	
	private Map<String, T> mytypes;

	/**
	 * Create a new extensible factory.
	 */
	public ExtensibleFactory() { mytypes = new Hashtable<String, T>(); }

	/**
	 * Removes all items.
	 *
	 */
	public void clear() {
		mytypes.clear();
	}
	
	/**
	 * Adds a new subclass of the master item type to the factory.  The instance's
	 * toString method will be queried immediately and that will be the key by which the item is found.
	 * If an item already exists with that key, it will be replaced.
	 * 
	 * @param item An instance of the class to add
	 */
	public void addType(T item) {
		mytypes.put(item.toString(), item);
	}

	/**
	 * Returns an instance of each item class known.  Note that a new instance for each item class is expected
	 * each time this method is called.
	 *  
	 *  @return <code>T[]</code>
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> allTypes() {
		Iterator<T> it = mytypes.values().iterator();
		//Object[] tar = new Object[mytypes.size()];
		ArrayList<T> vsl = new ArrayList<T>();
		
		
		while (it.hasNext()) {
			T t = it.next();
			try {
				//tar[tidx] = t.getClass().newInstance();
				//tidx++;
				vsl.add( (T)t.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//T[] ts = (T[])tar;
		
		return vsl;
		
	}
	
	/**
	 * Given the {@link #toString()} value, creates a new instance.  
	 *  
	 * @param type The name of the item class, as defined in the toString method.
	 * @return New item class instance 
	 * @throws ClassNotFoundException If no item class is found to match the requested type.
	 */
	@SuppressWarnings("unchecked")
	public T createInstance(String type) throws ClassNotFoundException {
		
		T val = mytypes.get(type);
		
		try {
			if (val != null) return (T)val.newInstance();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
		
		throw new ClassNotFoundException(type);
	}

}
