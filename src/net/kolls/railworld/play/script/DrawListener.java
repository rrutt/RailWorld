package net.kolls.railworld.play.script;

import java.awt.Graphics2D;
import java.awt.Rectangle;

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
 * The interface used by a script to indicate it is interested in handling draw events.
 * 
 * @author Steve Kollmansberger
 */
public interface DrawListener {
	/**
	 * Called at every step to allow the script to draw on the map.  This is called
	 * after all z-levels for segments.
	 * 
	 * @param gc  Graphics context of the image to draw on.
	 * @param onScreen What portion (coordinates) of the image are on screen.
	 */
	void draw(Graphics2D gc, Rectangle onScreen);
}
