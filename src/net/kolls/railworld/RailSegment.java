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
import java.awt.Graphics2D;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import net.kolls.railworld.edit.EditCanvas;
import net.kolls.railworld.edit.SegmentEditPoint;

/**
 * The interface which must be implemented for something to be considered a rail segment.
 * Most segments will derive from an existing segment (usually {@link net.kolls.railworld.segment.TrackSegment} and not implement this interface directly!
 * 
 * @author Steve Kollmansberger
 *
 */
public abstract class RailSegment {
	// a rail segment can be regular track, a switch, a signal, etc.
	// each rail segment must at the very least tell us where it comes from
	// and where it goes, currently.
	// indicate where you are coming from, it says where you're going
	// null value means end of line
	
	
	
	private class SetUndo extends AbstractUndoableEdit {
		private int _p;
		private RailSegment _v, _vn;
		
		/**
		 * Create an undo for setting a destination value.
		 * 
		 * @param p The destination index
		 * @param v The old segment that it should be undone to
		 */
		public SetUndo(int p, RailSegment v) {
			_p = p;
			_v = v;
		}
		@Override
		public String getPresentationName() { return "Set Segment"; }
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			dests[_p] = _vn;
			recomp();
		}
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			
			_vn = dests[_p];
			
			dests[_p] = _v; 
			recomp();
		}
	}

	/**
	 * The width of the rail bed (gravel)
	 */
	public static final Distance RAIL_BED_WIDTH = new Distance(24, Distance.Measure.FEET);
	
	/**
	 * The width of the rail line itself.  This is the rail-to-rail width.
	 */
	public static final Distance RAIL_WIDTH = new Distance(3, Distance.Measure.FEET);

	/**
	 * How close does the mouse need to be to count as being "near" something?
	 */
	protected static final Distance MOUSE_NEAR = new Distance(10.0, Distance.Measure.FEET);

	

	/**
	 * The canvas to register undo information with, if any.
	 * @see #editPanel()
	 */
	public EditCanvas ec;



	/**
	 * Various connections to this segment.  Indexed based on point values for set.
	 */
	protected RailSegment[] dests;
	
	/**
	 * Points that make up this segment.  May be used for various reasons,
	 * should not be interpreted as a line or any other shape.
	 */
	protected Point2D[] pts;

	/**
	 * Trains present in this segment
	 */
	protected Set<Train> trains;

	// check if we can erase this segment
	// no if it is connected to other dependent segments
	/**
	 * Can this segment be erased?  In some cases, other segments must be erased first.
	 * 
	 * @return Indicates if the segment may be erased as-is.
	 */
	public abstract boolean canErase();
	
	// some types of track may suppress the drawing of cars
	/**
	 * Should cars on this segment be drawn, or hidden?
	 * Defaults to false (not hidden).
	 * 
	 * @return A boolean indicating if cars are hidden or not.
	 */
	public boolean carHidden() { return false; }
	
	// when a click occurs, check to see if we should take any action
	/**
	 * Whenever a user clicks on the canvas, all dynamic segments receive this event.
	 * Check to see if the position is at your segment (within some reasonable grace)
	 * Indicate if you performed an action or not.  This tells the system to continue
	 * to try to find a segment to handle the event or not.
	 * Defaults to no action, returns false to indicate nothing happened, system
	 * should try another segment.
	 * 
	 * @param pos The click position
	 * @param rc The current canvas which contains this segment
	 * @return If an action/change was performed
	 * @see #isDynamic()
	 */
	public boolean click(Point2D pos, RailCanvas rc) { return false; }
	
	/**
	 * Each rail segment must at the very least tell us where it comes from
	 * and where it goes, currently.
	 * Indicate where you are coming from, it says where you're going.
	 * Null value means end of line.  These values may be dynamic, as in a switch.
	 * 
	 * @param source The rail segment of origin
	 * @return The destination rail segment, or <code>null</code> if there is none.
	 */
	public abstract RailSegment dest(RailSegment source);
	
	/**
	 * Finds the first non-zero length RailSegment going away from the source.
	 * Returns the sequence of segments (*including* the current one)
	 * required to get there. 
	 * 
	 * @param source The originating segment
	 * @return A vector of the segments, in order, traversed to arrive at the non-zero length segment.
	 * @see #dest(RailSegment)
	 */
	
	public final ArrayList<RailSegment> destNZ(RailSegment source) {
		RailSegment r = dest(source);
		RailSegment orig = this;
		RailSegment tmp;
		ArrayList<RailSegment> t = new ArrayList<RailSegment>();
		
		t.add(this);
		while (r != null && r.length().feet() == 0) {
			t.add(r);
			tmp = r;
			r = r.dest(orig);
			orig = tmp;
		}
		if (r != null) t.add(r);
		return t;
	}

	
	
	// must be able to be drawn
	// z = 1,2 static
	// z = 4 dynamic
	// z = 5 edit pts
	/**
	 * Draw the segment.  Segments have several z-layers to allowed slightly overlaping pieces to still look nice.
	 * z = 1 is a static layer (lowest)
	 * z = 2 is a static layer (highest)
	 * z = 3 is reserved
	 * z = 4 is the dynamic layer.  This is redrawn frequently, if requested by the segment.
	 * 
	 * 
	 * @param z The currently z-layer to draw
	 * @param gc A {@link Graphics2D} graphics context.
	 * @see #isDynamic()
	 */
	public abstract void draw(int z, Graphics2D gc);
	
	// edit points should be able to display info panels
	// must be allowed to redraw the canvas
	/**
	 * You are guaranteed that the variable {@link #ec} will be set prior to this call.
	 * So if you need to refresh the canvas or make any changes to it, you can use
	 * that variable.
	 * 
	 * @return A {@link JPanel} to display in the sidebar to edit this segment's properties.  Please keep width at 200 pixels top.
	 */
	public abstract JPanel editPanel();

	// 
	/**
	 * Notification that a train is in the segment.
	 * You may be notified arbitrarily often/many times.
	 * Check {@link #trains()} to see if it remains.
	 * Defaults to no behavior.
	 * 
	 * @param t The train in the segment.
	 */
	public void enter(Train t) { }
	
	/**
	 * Returns a given destination RailSegment.
	 * 
	 * @param dest Which connection number (as defined by the individual class) to retrieve.
	 * @return The RailSegment corresponding to that connection.
	 */
	public final RailSegment getDest(int dest) {
		return dests[dest];
	}
	/**
	 * Returns a given point.
	 * 
	 * @param ptIdx The point index to retreive.
	 * @return The Point2D from the pts array.
	 */
	public final Point2D getPoint(int ptIdx) {
		return pts[ptIdx];
	}
	
	// must be able to get a position on the rail
	/**
	 * 
	 * @param start The segment of origin
	 * @param myPos Percentage (0-1) along the segment
	 * @return A {@link Point2D} indicating the actual point that location represents
	 */
	public abstract Point2D getPoint(RailSegment start, double myPos);
	
	// does this segment need to be re-drawn dynamically?
	// if so, it will receive continual draw-level 4 updates
	// levels 1 and 2 still static
	/**
	 * Dynamic segments receive additional events while the game is running; non-dynamic segments
	 * are only asked to draw z levels 1 and 2 at draw static time.
	 * Defaults to false (not dynamic).  Non-dynamic segments also
	 * may not receive notifications for various events.
	 * 
	 * @return Indicates if the segment should receive z=4 {@link #draw(int, Graphics2D) draw} events
	 * and {@link #click(Point2D, RailCanvas) click} events.
	 * @see #draw(int, Graphics2D)
	 * @see #click(Point2D, RailCanvas)
	 */
	public boolean isDynamic() { return false; }
	
	// all segments have a length
	// can be 0 for signals, etc.
	// length must be the same no mattter which 'way' you traverse the segment
	/**
	 * all segments have a length
	 * can be 0 for signals, etc.
	 * length must be the same no mattter which 'way' you traverse the segment
	 * Distance may be a 0-foot distance, but not may be null.
	 * 
	 * @return The {@link Distance} of the segment.
	 */
	public abstract Distance length();
	
	/**
	 * Whenever a user mouses over the canvas, all dynamic segments receive this event.
	 * Check to see if the position is at your segment (as in click).
	 * Return a describe if applicable, otherwise null.
	 * Defaults to returning null.
	 * 
	 * @param pos The mouse position
	 * @return Description if applicable, otherwise null
	 * @see #isDynamic()
	 */
	public String mouseOver(Point2D pos) { return null; }
	
	
	// check in edit mode to see if we are near an anchor
	// some can connect additional segments
	// return the precise location if so, null if otherwise
	/**
	 * Given a point, find out if it is near one of our edit handles (z=5).  If so, check if we can
	 * attach the segment requested (may be null for no segment to attach).  If so, return
	 * an edit point
	 * 
	 * @param loc The point location
	 * @param attach The segment (or <code>null</code>) that wants to attach here
	 * @return A {@link SegmentEditPoint}, or <code>null</code>.
	 */
	public final SegmentEditPoint nearEditPoint(Point2D loc, RailSegment attach) {
		
		for (int i = 0; i < pts.length; i++) {
			
			if (loc.distance(pts[i]) < MOUSE_NEAR.pixels())
				return createSEP(i, attach);
		}
		return null;
	}
	
	// sometimes we want to move in the realm of pixels
	/**
	 * Given a position (percentage), move one pixel and give the new percentage.
	 * 
	 * @param myPos The current percentage (0-1) along the segment.
	 * @return The new percentage, or 1 if the end of the segment has been exceeded, for moving forward one pixel.
	 */
	public final double pixelStep(double myPos) {
		// we can cache the hard part
		if (lp == -1) {
			if (length().feet() == 0) lp = -2;
			else {
				double l = length().pixels(); // start out with the length
				lp = 1.0 / l; // reciprocal. one pixel per lp
			}
		}
		
		if (lp == -2) return 1;

		double newPos = lp + myPos;
		if (newPos > 1.0) return 1.0; 
		
		return newPos;
	}
	
	private double lp = -1;

	
	// various kinds of rail segments have connections
	// point indicates 0.., which connection, depends on class
	// ifNotNull only sets the value if the valye is null
	/**
	 * Updates one of the connecting segments to this segment.  Each type of segment
	 * must define its own point constants.  Note this is an undoable edit
	 * if the edit canvas undo manager is available.
	 * 
	 * @param point A value defined by the rail segment to indicate one of its ends
	 * @param ifNotNull Only perform the update if the value is currently null
	 * @param value The new rail segment
	 */
	public final void setDest(int point, boolean ifNotNull, RailSegment value) {
		if (ifNotNull && dests[point] != null) return;
		// create undo
		
		if (ec != null) ec.addUndo(new SetUndo(point, dests[point]));
		
		dests[point] = value;
		recomp();
	}
	
	/**
	 * Update a drawing point.
	 * 
	 * @param point The point index (meaning defined by the individual segment) to update.
	 * @param p The new point
	 */
	public final void setPoint(int point, Point2D p) {
		pts[point] = p;
	}
	
	/**
	 * 
	 * @return The numbers of destinations this segment has 
	 */
	public final int getDests() { return dests.length; }
	
	/**
	 * 
	 * @return The number of drawing points this segment has
	 */
	public final int getPoints() { return pts.length; }
	
	
	/**
	 * Singleton segments may only have one train in them at a time. If additional
	 * trains are detected, a rail accident occurs.
	 * Defaults to false (not a singleton).
	 * 
	 * @return If the segment may only have one train on it at a time
	 */
	public boolean singleton() { return false; }
	
	// rail segments can also be transited
	// to do this, the end user must indicate how far along the segment they are
	// and their speed.  the system must either give them a new far along measure
	// or 1 to indicate they have reached the end of the segment
	// myPos is 0 to 1, a percentage.  returns a new percentage (position)
	/**
	 * 
	 * @param myPos The current percentage (0-1) along the segment
	 * @param mySpeed The current MPH
	 * @return The new percentage location, or 1 if the end of the segment has been exceeded.
	 * Note that the origin does not matter because the length must be the same either way.
	 */
	public final double step(double myPos, double mySpeed) {
		if (mySpeed == 0) return myPos;
		if (length().feet() == 0) return 1;
		
		double l = length().feet(); // start out with the length
		
		double d = GameLoop.feetPerStepSpeed(mySpeed) / l;
		double newPos = d + myPos;

		if (newPos + (d/2.0) > 1.0) return 1.0; //rounding for smoothness

		if (newPos > 1.0) return 1.0; 
		return newPos;

	}
	
	
	// what trains are (at least partially) in this segment
	// maintained automatically, read only
	/**
	 * Note: Set contents will be maintained automatically, read only
	 * 
	 * 
	 * @return What trains are (at least partially) in this segment 
	 */
	public final Set<Train> trains() { if (trains == null) trains = new HashSet<Train>(); return trains; }
	
	
	// also we can update the physical location
	// given an initial point, anything at that point is updated
	// to the new point
	/**
	 * Update a point location for this segment.  You must be able to handle original
	 * locations that are not your points (in which case do nothing)
	 * This updates end points only.
	 * 
	 * @param original The original location
	 * @param replacement The new location
	 */
	public final void update(Point2D original, Point2D replacement) {
		for (int i = 0; i < pts.length; i++) {
			if (pts[i].equals(original))
				pts[i] = replacement;
			
		}
				
	}

	
	// update finds any segments equal to the one passed in
	// and changes them
	/**
	 * Updates a connection from a given segment to a new one.  
	 * This is an undoable edit if the edit canvas undo manager is available.
	 * 
	 * @param test The original segment to replace
	 * @param newValue The new segment to replace it with
	 * @return The point id (used for @link{#set}) which was updated, or -1 if no update occured.
	 */
	public final int update(RailSegment test, RailSegment newValue) {
		for (int i = 0; i < dests.length; i++) {
			if (dests[i] == test) {
				
				if (ec != null) ec.addUndo(new SetUndo(i, dests[i]));
				dests[i] = newValue;
				recomp();
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Given a point index in the pts array and possibly a railsegment to attach,
	 * return a segmenteditpoint if such attachment is possible.  If the attach
	 * segment is null, no segment is attaching, just user editing.
	 * 
	 * @param ptIdx  The index into the pts array.
	 * @param attach The segment that desires to attach.  (Note: do not attach the segment
	 * automatically; it will be called through the anchor method in the edit point).
	 * @return A SegmentEditPoint if possible, otherwise <code>null</code>.
	 */
	protected abstract SegmentEditPoint createSEP(int ptIdx, RailSegment attach);
	
	/**
	 * Called when changes are made; recompute or update any associated values.
	 *
	 */
	public abstract void recomp();
}
