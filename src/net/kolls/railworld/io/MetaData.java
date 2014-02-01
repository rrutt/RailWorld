package net.kolls.railworld.io;

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

import java.io.File;

/**
 * A data holding class for the meta-data of a map.
 * 
 * @author Steve Kollmansberger
 *
 */
public class MetaData {
	/**
	 * The file name (without path) of the image.
	 */
	public String imgfile;
	
	/**
	 * Name of the author.
	 */
	public String author;
	
	/**
	 * Comment about the map.
	 */
	public String comment;
	
	/**
	 * Map title.  Will be displayed in the title bar.
	 */
	public String title;
	
	 
	/**
	 * Represents the file that contains the map data; or, if none is yet determined,
	 * represents the directory that contains the image file.  In some cases may be
	 * <code>null</code> if the image and map are not files (e.g. URLs).
	 */
	public File ourFile;
	
	/**
	 * The center X position shown on the display.
	 */
	public int centerX;
	/**
	 * The center Y position shown on the display.
	 */
	public int centerY;
	
	/**
	 * Distance scale.  Should be loaded into the {@link net.kolls.railworld.Distance} class ASAP.
	 * 
	 * @see net.kolls.railworld.Distance#feetPerPixels
	 */
	public double feetPerPixel;
	
	/**
	 * Current zoom.
	 * 
	 * @see net.kolls.railworld.RailCanvas#zoom
	 */
	public double zoom;
	
	
	/**
	 * Current elapsed play time.
	 * 
	 * @see net.kolls.railworld.GameLoop#getElapsed()
	 */
	public long elapsed;
	
	/**
	 * Temporary variable, used for calculating the total length of track in feet.
	 */
	public double track; // total feet
	

	
}
