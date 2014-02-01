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

import javax.swing.filechooser.FileFilter;

/**
 * Filters for image files.
 * 
 * @author Steve Kollmansberger
 *
 */
public class ImageFilter extends FileFilter {

    
	/**
	 * Accept all directories and all gif, jpg, tiff, or png files.
	 * 
	 *  @return <code>true</code> to accept.
	 */
    @Override
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String u = f.getName().toUpperCase(); 
        if (u.endsWith(".TIFF")) return true;
        if (u.endsWith(".TIF")) return true;
        if (u.endsWith(".GIF")) return true;
        if (u.endsWith(".JPEG")) return true;
        if (u.endsWith(".JPG")) return true;
        if (u.endsWith(".PNG")) return true;
        
        return false;
    }

    
    /**
     * @return The description of this filter 
     */
    @Override
	public String getDescription() {
        return "Image Files (TIFF, GIF, JPEG, PNG)";
    }
}