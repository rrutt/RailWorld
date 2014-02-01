package net.kolls.railworld.scripts;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.DrawListener;
import net.kolls.railworld.play.script.Script;

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
 * 
 * Maintains an hour/minute/second timer for the user.
 * 
 * 
 * @author Steve Kollmansberger
 */
public class Timer implements Script, DrawListener {


	protected PlayFrame mpf;
	
	@Override
	public void init(PlayFrame pf) {
 
		mpf = pf; 
		pf.jdb.sm.addDrawListener(this);

	}

	public RailSegment[] modifySegments(RailSegment[] lines) {
		return lines;
	
	}
	
	@Override
	public boolean playFrameAction(String action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load(Map<String, String> data) {
	
	}
	
	@Override
	public String toString() {
		return "Timer";
	}

	@Override
	public Object newInstance() {
		return new Timer();
	}

	@Override
	public Map<String, String> save() {
		return null;
	}

	protected long getTime() {
		return mpf.gl.elapsed;
	}
	
	@Override
	public void draw(Graphics2D gc, Rectangle onScreen) {

		long elapsed = getTime();
		
		// format elapsed
		long esec = elapsed / 1000;
		long hrs = (esec / 60 / 60);
		long mins = ((esec / 60) - hrs*60);
		long secs = (esec - mins*60 - hrs*60*60);
		String b = "";
		if (hrs < 10) b += "0";
		b += hrs + ":";
		if (mins < 10) b += "0";
		b += mins + ":";
		if (secs < 10) b += "0";
		b += secs;
		
		AffineTransform ct = gc.getTransform();
		gc.setTransform(new java.awt.geom.AffineTransform());


		RailCanvas.drawOutlineFont(gc, 20, 20, // onScreen.getX()+20, onScreen.getY()+20, 
			b, 10, java.awt.Color.white, 0, false);
			
		gc.setTransform(ct);
		
	}

	@Override
	public boolean onByDefault() {
		return true;
	}

}
