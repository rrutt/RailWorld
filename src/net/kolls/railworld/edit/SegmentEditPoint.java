package net.kolls.railworld.edit;

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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import net.kolls.railworld.Distance;
import net.kolls.railworld.RailSegment;

/**
 * A segment edit point consists of a point on a segment as well as some extra abilities for editing,
 * such as drawing temporary versions, moving the point (which should in turn move all segments
 * connected to it) and determining if it should connect to other segments
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class SegmentEditPoint {
	
	
	/**
	 * The point index of the segment we are editting
	 */
	protected int pidx;
	
	/**
	 * The color that the handle should use. 
	 * Red = may connect to another segment
	 * Green = already connected/not connectable
	 */
	protected Color c;

	// pos 0 must be my segment
	/**
	 * Segments involved in this edit point.  There must
	 * be at least one -- and the first *MUST* be
	 * the segment which owns this edit point.
	 */
	public RailSegment myr[];
	
	// first one in segs is the segment that this edit point belongs to
	/**
	 * Create a segment edit point.
	 * @param segs The segments involved.  See {@link #myr} for details.
	 * @param ptIndex The index of the point on the owning segment. See {@link #pidx}
	 * @param col Color of the handle to display. See {@link #c}
	 */
	public SegmentEditPoint(RailSegment segs[], int ptIndex, Color col) {
		pidx = ptIndex;
		c = col;
		myr = segs;
	}
	
	// returns null if anchor successful
	// returns a value if a new segment is created that
	// a) needs to be added to the array
	// b) anything that was going to anchor to me should now anchor to the returned segment
	/**
	 * Attachs a segment to this edit point.
	 * 
	 * @param r The {@link RailSegment} to anchor to this point.
	 * @return Returns a new segment if that segment should be added to the array of segments, and
	 * also anything else that wanted to anchor here should anchor at the returned segment.  May
	 * return <code>null</code>.
	 */
	public abstract RailSegment anchor(RailSegment r);
	
	/**
	 * Gets the color of this point.  Green indicates a connected segment,
	 * Yellow indicates an non-connectable segment, red indicates
	 * a disconnected segment.
	 *
	 * @return The color to display the edit point as.
	 */
	public final Color getColor() { return c; }
	
	/**
	 * Returns the railsegment this edit point is associated with.
	 * 
	 * @return The RailSegment associated with this edit segment; the first entry in myr.
	 */
	public final RailSegment getSegment() { return myr[0]; }
	
	/**
	 * Returns the current location of this edit point.
	 * 
	 * @return A point2D containing the current, zoomed, location of this edit point.
	 */
	public final Point2D getPoint() { return getSegment().getPoint(pidx); }
	
	/**
	 * Moves this edit point.
	 * 
	 * @param p New location.
	 */
	public final void moveTo(Point2D p) {
		Point2D p2 = p;
		Point2D orig = getPoint();
		
		// update just this ptIdx of my segment
		myr[0].setPoint(pidx, p2);
		
		
		// update other segments only
		for (int i = 1; i < myr.length; i++)
			if (myr[i] != null) myr[i].update(orig, p2);
		
		
	}
	
	/**
	 * Draws this edit point.
	 * 
	 * @param gc The graphics context
	 */
	public final void draw(Graphics2D gc) {
		double diam = Distance.toPixels(15);
		double rad = diam / 2.0;
		Point2D pp = getPoint();
		Shape sb = new Ellipse2D.Double(pp.getX()-rad, pp.getY()-rad, diam, diam);
		
		gc.setPaint(getColor());
		gc.fill(sb);
		
		gc.setPaint(Color.black);
		gc.draw(sb);
		
		
			
	}
	
	// if I'm dragging it around, should it fix onto things?
	/**
	 * If this segment is being moved, should it offer to anchor onto other segments?
	 * Note that the other segment will be asked to approve with {@link RailSegment#nearEditPoint(Point2D, RailSegment)}.
	 * 
	 * @return <code>true</code> if we should snap-to and anchor onto other segments.
	 */
	public abstract boolean isAnchorSource();
	
	
	
}
