package net.kolls.railworld.segment;

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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Line2D;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Sounds;

/**
 * At-grade crossing.  Plays crossing sound when train is present or on adjacent segment.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Crossing extends TrackSegment {

	/**
	 * Create a crossing.
	 * 
	 * @param bg Begin segment
	 * @param en End segment
	 * @param crds Line coords.
	 */
	public Crossing(RailSegment bg, RailSegment en, Line2D crds) {
		super(bg, en, crds);
		wasPlaying = false;
		
	}
	
	private boolean hasTrains() {
		
		
		
		// if trains in our segment
		if (trains().isEmpty() == false) return true;
		
		
		// for adjacent segment, ensure they exist first
		if (dests[0] != null && dests[0].trains().isEmpty() == false) return true;
		if (dests[1] != null && dests[1].trains().isEmpty() == false) return true;
		
		
		
		return false;
	}
	
	private boolean wasPlaying;
	
	
	@Override
	public void draw(int z, Graphics2D gc) {
		
		Paint p = gc.getPaint();


		switch (z) {
		case 1:
			
			gc.setPaint(Color.darkGray);
			gc.setStroke(railBedStroke);
			gc.draw(coords);
			break;
		case 4:
			// dynamic level
			// check for trains in this or adjacent segment

			boolean h = hasTrains();

			
			if (h && wasPlaying == false) {
				Sounds.crossing.loop();
				wasPlaying = true;
				
			} 
			if (h == false && wasPlaying) {
				Sounds.crossing.stop();
				wasPlaying = false;
				
			}
			
			
			break;
		default:
			super.draw(z, gc);				
		}

		
		

		gc.setPaint(p);

		
	}
	
	@Override
	public boolean isDynamic() {
		// although the appearance is not dynamic (at this time), we want to be assured
		// that draw will be called so we can set the sound
		return true;
		
	}
}
