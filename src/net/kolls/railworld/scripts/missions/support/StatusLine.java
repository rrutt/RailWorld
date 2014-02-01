package net.kolls.railworld.scripts.missions.support;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.play.script.DrawListener;

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
 * Displays a status line (e.g. points, etc) on the display.
 * 
 * @author Steve Kollmansberger
 */
public class StatusLine implements DrawListener {

	public String status = "";
	
	@Override
	public void draw(Graphics2D gc, Rectangle onScreen) {
		AffineTransform ct = gc.getTransform();
		gc.setTransform(new java.awt.geom.AffineTransform());


		RailCanvas.drawOutlineFont(gc, 20, 40,  
			status, 10, java.awt.Color.white, 0, false);
			
		gc.setTransform(ct);


	}

}
