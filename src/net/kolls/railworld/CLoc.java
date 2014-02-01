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

import java.awt.geom.*;
import java.util.ArrayList;


/**
 * The CLoc class pinpoints a location and direction within the map (on a track).  
 * Locations are represented
 * using three pieces: The rail segment, the originating rail segment, and the percentage from the
 * originating rail segment.  For example, say segment A is 50 feet long, connecting at one end
 * to segment B and at the other end to segment C.  If the current segment is A, the originating
 * segment is B, and the percentage is 0.75, the we are on segment A, heading toward segment C, currently at
 * 37.5 feet into segment A.  
 * 
 * @author Steve Kollmansberger
 *
 */
public class CLoc  {
	
	/**
	 * Constructs a CLoc given the three essentials.
	 * 
	 * @param cur Current {@link RailSegment}
	 * @param start The {@link RailSegment} we came from (originating segment)
	 * @param p The percentage through the current segment we are.
	 */
	public CLoc(RailSegment cur, RailSegment start, double p) { r= cur; orig=start; per=p;}
	
	/**
	 * Constructs a CLoc will all null values.
	 *
	 */
	public CLoc() { r = orig = null; per = 0.0; }

	

	/**
	 * The current {@link RailSegment}.  The position is somewhere on this segment.
	 */
	public RailSegment r; 


	/**
	 * The origin {@link RailSegment}.  This is where we came from.  This provides 
	 * directionality to the location.
	 */
	public RailSegment orig;

	/**
	 * Percentage (0-1) through the segment.
	 * 0 means we are right at the origin segment.
	 * 1 means we are all the way to the opposite end, where the segment ends.
	 * Movement can be accomplished by increasing this value.
	 */
	public double per;

	/**
	 * Returns the same position with opposite directionality.  This relies on another segment
	 * beyond the current one in the facing direction to become the origin.
	 * It also flips the percentage.
	 * 
	 * @return A new CLoc containing the same position facing the opposite direction.
	 */
	public CLoc reverse() {
		return new CLoc(r, r.dest(orig), 1.0-per);
		
	}
	
	/**
	 * Returns an absolute point for this location.
	 * 
	 * @return A {@link Point2D} for this location.
	 */
	public Point2D getPoint() {
		return r.getPoint(orig, per);
	}

	/**
	 * Moves forward in the direction of travel a given distance.  
	 * 
	 * @param len A {@link Distance} representing how far to move.  The distance may exceed the current segment.
	 * @return A {@link DLoc} representing the new location.
	 */
	public DLoc segFwd(Distance len) {
		return segFwd(len, null, null);
	}
	
	/**
	 * Moves forward in the direction of travel a given distance, and generate rendering information for a given
	 * {@link Train} and/or {@link Car}.  This method generates the lines which will be drawn to render cars
	 * in a Train.  Note that lines will not be rendered for hidden segments, and cars will not be curved.
	 * This method also registers the train and car with the segments and vice versa.
	 * 
	 * @param len A {@link Distance} representing how far to move.  The distance may exceed the current segment.
	 * @param myC A {@link Car} for tracking segments.  May be <code>null</code>.
	 * @param myT A {@link Train} for tracking segments and recordings enters.  May be <code>null</code>
	 * @return A {@link DLoc} representing the new location, and including rendering information.
	 */
	public DLoc segFwd(Distance len, Car myC, Train myT) {
		
		int pixels = len.iPixels();
		
				
		//Line2D[] lan = new Line2D[pixels+1];
		ArrayList<Line2D> lan = new ArrayList<Line2D>();
		Line2D[] l2;
		
		RailSegment pr = r, porig = orig, tmp;
		Point2D s, e;
		double pper = per;
		

		s = pr.getPoint(porig, pper);
		
		while (pixels /* -- */ > 0) {
			// if our desired pixels exceeds
			// the number of pixels remaining
			// in this segment,
			// jump to the end
			int pixelsLeft = (int)(pr.length().pixels() * (1.0-pper));
			if (/*pr.length().feet() > 0 && */ pixels >= pixelsLeft) {
				pper = 1;
				pixels -= pixelsLeft;
			} else { 
				// we finish up within this segment
				pper += pixels / pr.length().pixels();
				pixels = 0;
				//pper = pr.pixelStep(pper);
			}
			
			//e = pr.getPoint(porig, pper);
			
			
			if (pper == 1) {
				// zero length segments don't count
				if (pr.length().feet() == 0) pixels++;

				// finalize the line segment if appropriate
				e = pr.getPoint(porig, pper);
				
				
				
				if (pr.carHidden() == false)
					lan.add(new Line2D.Double(s, e));
				
				
				
				// register our presence in this segment if needed
				if (myT != null) {
					pr.trains().add(myT);
					pr.enter(myT);
					
				}
				if (myC != null) myC.segs().add(pr);

				// move to the next segment				
				pper = 0;
				tmp = pr;
				
				pr = pr.dest(porig);
				if (pr == null) break;
				porig = tmp;
				s = pr.getPoint(porig, pper); 	
			}
		}
		if (pr != null && pr.carHidden() == false) {
			e = pr.getPoint(porig, pper);
			lan.add(new Line2D.Double(s, e));
		}
		// register our presence in this segment if needed
		if (myT != null && pr != null) {
			pr.trains().add(myT);
			pr.enter(myT);
			
		}
		if (myC != null && pr != null) myC.segs().add(pr);
		

		//l2 = new Line2D[ln];
		//System.arraycopy(lan, 0, l2, 0, ln);
		l2 = lan.toArray(new Line2D[0]);

		return new DLoc(makeStraight(l2), new CLoc(pr, porig, pper));
	}
	
	private Line2D[] makeStraight(Line2D[] lines) {
		Line2D[] l1, l2;

		if (lines.length == 0) return lines; // nothing to straighten

		l1 = new Line2D[lines.length];
		int c = 0;
		Point2D s = lines[0].getP1();
		int i;

		for (i = 1; i < lines.length; i++) {
			if (lines[i].getP1().distance(lines[i-1].getP2()) < 1) continue;
			//if (lines[i].getP1().equals(lines[i-1].getP2())) continue;

			// broken lines (hidden, whatever)
			l1[c++] = new Line2D.Double(s, lines[i-1].getP2());
			s = lines[i].getP1();
		}
		l1[c++] = new Line2D.Double(s, lines[i-1].getP2());
		l2 = new Line2D[c];
		System.arraycopy(l1, 0, l2, 0, c);
		return l2;

	}

}
