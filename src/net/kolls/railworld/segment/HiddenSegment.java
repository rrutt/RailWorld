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

import net.kolls.railworld.*;

import java.awt.geom.*;
import java.awt.*;


/**
 * A hidden segment.  Trains on this segment are not drawn.
 * 
 * @author Steve Kollmansberger
 *
 */
public class HiddenSegment extends TrackSegment {

	private Stroke dl;
	/**
	 * Create a hidden segment
	 * 
	 * @param bg Begin segment
	 * @param en End segment
	 * @param crds Line coords
	 */
	public HiddenSegment(RailSegment bg, RailSegment en, Line2D crds) {
		super(bg,en,crds);
	
	}
	@Override
	public void recomp() {
		super.recomp();
		dl = new BasicStroke(RAIL_WIDTH.iPixels(),BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {5.0f, 5.0f}, 0.0f);
	}
	@Override
	public void draw(int z, Graphics2D gc) {
		
		Paint p = gc.getPaint();

		
		

		// no base, dashed line
		// for track
		if (z == 2) {
			
			gc.setPaint(Color.black);
			gc.setStroke(dl);
			gc.draw(coords);
		}
		
		gc.setPaint(p);
	}
	@Override
	public boolean carHidden() { return true; }
}
