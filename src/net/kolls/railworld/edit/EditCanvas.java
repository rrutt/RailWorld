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
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.kolls.railworld.Car;
import net.kolls.railworld.MiniViewer;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Distance;
import net.kolls.railworld.segment.*;
import net.kolls.railworld.segment.TrackSegment.TSEP;



/**
 * A canvas for editing segments.  Does not support having trains.
 * 
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class EditCanvas extends RailCanvas {

	
	private CompoundEdit createSegment;
	
	private SegmentEditPoint sep, osep;
	
	private ArrayList<SegmentEditPoint> allsegs;
	
	/**
	 * One of the TOOL_* constants.
	 */
	public Tool selectedTool;
	
	/**
	 * The available tools in the edit canvas.
	 * 
	 *
	 */
	public enum Tool { 
		/**
		 * Selection
		 */
		TOOL_SELECT,
		/**
		 * Eraser
		 */
		TOOL_ERASE,
		/**
		 * Straight track
		 */
		TOOL_TRACK, 
		/**
		 * Hidden track
		 */
		TOOL_HTRACK,
		/**
		 * Load/unload track
		 */
		TOOL_LUTRACK, 
		/**
		 * Hidden load/unload track
		 */
		TOOL_HLUTRACK, 
		/**
		 * Entry/exit segment
		 */
		TOOL_EETRACK, 
		/**
		 * Text (label)
		 */
		TOOL_TEXT, 
		/**
		 * Crossing (at-grade) with bells
		 */
		TOOL_CROSSING,
		/**
		 * Curve
		 */
		TOOL_CURVE
	}
	
	/**
	 * Allows calls to undo and redo for edits.
	 */
	public UndoManager undos = null;
	
	/**
	 * Still as it was when loaded or saved? 
	 */
	public boolean justSaved;
	
	
	private int eecnt;
	
	/**
	 * Should edit points be displayed?
	 */
	public boolean displaySEP;

	/**
	 * The edit panel provided by the currently selected edit point.
	 */
	public JPanel seppanel;
	
	/**
	 * Construct an edit canvas with a given source image, some segments, and a mini viewer.
	 * 
	 * @param s Source {@link BufferedImage}
	 * @param lines Array of {@link RailSegment}s
	 * @param mini {@link MiniViewer} to use.
	 */
	public EditCanvas(BufferedImage s, RailSegment[] lines, MiniViewer mini) {
		super(s,lines,mini);
		eecnt = 0;
		displaySEP = true;
		osep = null;
		createSegment = null;
		

		undos = new UndoManager();
	
		justSaved = true;
		
		selectedTool = Tool.TOOL_SELECT;
		
		
		
		recomp();
		
		
	}
	
	@Override
	public void recomp() {
		super.recomp();
		createAllSegs();
	}
	
	private void createAllSegs() {
		allsegs = new ArrayList<SegmentEditPoint>();
		// assign all lines this editcanvas and get all edit points
		// the latter just for display purposes
		for (int i = 0; i < la.length; i++) {
			
			la[i].ec = this;
			for (int j = 0; j < la[i].getPoints(); j++) {
				SegmentEditPoint asep = la[i].nearEditPoint(la[i].getPoint(j), null);
				if (asep != null) allsegs.add(asep);
			}
		}
		
	}
	
	// various classes for undo information
	
	private class ArrayUndo extends AbstractUndoableEdit {

		
		private RailSegment[] org;
		private RailSegment[] rev;
		private String act;
		
		/**
		 * Create a new array undo
		 * 
		 * @param original The original array to fall back to
		 * @param revised The new array to redo to
		 * @param action The action name
		 */
		public ArrayUndo(RailSegment[] original, RailSegment[] revised, String action) {
			org = original;
			rev = revised;
			act = action;
			
		}
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			
			la = org;
			
			
			osep = sep = null;
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			
			la = rev;
			
			
			osep = sep = null;
			
		}
		@Override
		public String getPresentationName() { return act + " Segment"; }
	}
	
	
	
	
	private class XYMoveUndo extends AbstractUndoableEdit {
		
		private Point2D up, p;
		private SegmentEditPoint uo;
		
		/**
		 * Create a new movement undo
		 * 
		 * @param sep The edit point being moved
		 * @param from The original point to undo to
		 * @param to The new point to redo to
		 */
		public XYMoveUndo(SegmentEditPoint sep, Point2D from, Point2D to) {
			uo = sep;
			up = from;
			p = to;
		}
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			uo.moveTo(up);
			
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			uo.moveTo(p);
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof XYMoveUndo) {
				XYMoveUndo x = (XYMoveUndo)anEdit;
				if (x.uo != uo) return false;
				p = x.p;
				return true;
			} 
			return false;
		}
		
		@Override
		public String getPresentationName() { return "Move Segment"; }
	}
	
	
	
	
	
	/**
	 * Move the previous selected segment a given amount.  Used for arrow keys.
	 * 
	 * @param dx
	 * @param dy
	 */
	public void xymove(int dx, int dy) {
		
		
		
		if (osep == null) return;
		
		
		final Point2D up = osep.getPoint();
		
		
		final Point2D p = new Point2D.Double(up.getX(), up.getY());
		p.setLocation(p.getX()+dx, p.getY()+dy);
		

		addUndo(new XYMoveUndo(osep, up, p));		
		
		osep.moveTo(p);
		

		for (RailSegment r : osep.myr)
			if (r != null) r.recomp();
		
		
		
		
		
	}
	
	/**
	 * Add an undo to the sequence; if a segment creation is in progress,
	 * the undo is added to that undo group.
	 * 
	 * @param ue The undo to add
	 */
	public void addUndo(UndoableEdit ue) {
		if (createSegment != null && createSegment.isInProgress()) createSegment.addEdit(ue);
		else {
			if (undos == null) return;
			undos.addEdit(ue);
			justSaved = false;
			
		}
		
	}
	
	@Override
	public void doMiniPaint(Graphics2D ofg) {
		// nothing to add to the mini canvas

	}

	@Override
	protected void doPaint(Graphics2D ofg, int hvx, int hvy, boolean detailed) {
		int i;
		
		
		for ( i = 0; i < la.length; i++) {
			if (la[i].isDynamic()) 
				la[i].draw(4,ofg);
			la[i].draw(5, ofg); // draw z-5 edit layer (usually nothing)
		}
		
		if (displaySEP) 
			for (SegmentEditPoint asep : allsegs) 
				asep.draw(ofg);
			
		
		


	}

	private void checkEdgeScroll(MouseEvent e) {
		int mxp, myp;
		int dx = 0, dy = 0;
		
		
		mxp = e.getX();
		myp = e.getY();
		
		// check to see if we are dragging near the edge
		if (mxp < 25) dx = -10;
		if (myp < 25) dy = -10;
		if (mxp > getWidth() - 25) dx = 10;
		if (myp > getHeight() - 25) dy = 10;
		
		if (dx != 0 || dy != 0)
			submitCoords( (int)(vx + dx * RailCanvas.zoom), (int)(vy + dy * RailCanvas.zoom));
		
		
		
	}
	
	private SegmentEditPoint nearEditPoint(Point2D cp) {
		SegmentEditPoint ap;
		
		
		for ( int i = 0; i < la.length; i++) {
		
		
			
			ap = la[i].nearEditPoint(cp, sep != null ? sep.getSegment() : null);
			if (ap != null && sep != null) {
				if (ap.getSegment() == sep.getSegment()) continue; // can't link to ourselves!
			}
			if (ap != null) return ap;
			
				
		} 
		return null;
		
	}
	
	
	
	@Override
	public void leftDrag(MouseEvent e) {

		
		// snap to any anchorable points nearby
		
		checkEdgeScroll(e);
		Point2D cp = transform(e);
		
		
		
		SegmentEditPoint ap = nearEditPoint(cp);
		
		
		if (selectedTool == Tool.TOOL_ERASE) {
			sep = ap;
			erase();
			return;
		}
		
		
		
		if (sep != null) {
			if (ap != null && sep.isAnchorSource()) cp = ap.getPoint();
			

			addUndo(new XYMoveUndo(sep, sep.getPoint(), cp));

			
			sep.moveTo(cp);
			
			for (RailSegment r : sep.myr)
				if (r != null) r.recomp();
		}
		
		
	}
	
	
	@Override
	public void leftRelease(MouseEvent e) {
		
		SegmentEditPoint ap = null;
		Point2D cp = transform(e);
		
		RailSegment r, r2;
		
		
		
		
		
		
		if (sep != null) {
			
			ap = nearEditPoint(cp);
			
			if (ap != null && sep.isAnchorSource()) {
				// anchoring will be compound
				if (createSegment == null) {
					createSegment = new CompoundEdit() {
						@Override
						public String getPresentationName() { return "Attach Segment"; }
						@Override
						public String getUndoPresentationName() { return "Undo Attach Segment"; }
						@Override
						public String getRedoPresentationName() { return "Redo Attach Segment"; }
					};	
				}
				
				r = ap.anchor(sep.getSegment());
				
				if (r != null) r.ec = this;
				
				r2 = sep.anchor(r == null ? ap.getSegment() : r);
				extendArray(r);
				
				
				if (r2 != null) {
					System.out.println("Dual created segments WTF");
				}
				
				if (r instanceof FourWay) {
					// kludge: need to remove the legacy switch
					sep = ap; // delete the switch
					erase();
					
					
				} 
				
				
			}
			recomp();
			showEdit();
			
			
			sep = null;
			osep = nearEditPoint(cp);
			
		} else recomp();
		
		if (createSegment != null) {
			createSegment.end();
			addUndo(createSegment);
			createSegment = null;
			
		}
		
	}
	
	private int extendArray(RailSegment r) {
		if (r == null) return -1;
		
		RailSegment[] l2 = new RailSegment[la.length+1];
		System.arraycopy(la, 0, l2, 0, la.length);
		l2[la.length] = r;
		int p = la.length;
		addUndo(new ArrayUndo(la, l2, "Create"));
		la = l2;
		
		createAllSegs();
		
		return p;
		
	}
	
	private void erase() {
		if (sep == null) return;
		if (sep.getSegment().canErase() == false) return;
		
		
		// special behavior for track segments: disconnect them
		if (sep.getSegment() instanceof TrackSegment && sep.isAnchorSource() == false) {


			if (createSegment == null) createSegment = new CompoundEdit() {
				@Override
				public String getPresentationName() { return "Disconnect Segment"; }
				@Override
				public String getUndoPresentationName() { return "Undo Disconnect Segment"; }
				@Override
				public String getRedoPresentationName() { return "Redo Disconnect Segment"; }
			};
			
			((TSEP)sep).disconnect();
			
		} else {
			
			if (createSegment == null) createSegment = new CompoundEdit();
		
			// disconnect all segments from this piece
			// remember what was disconnected for undo
			
			
			int pos = 0;
		
			RailSegment[] l2 = new RailSegment[la.length - 1];
			for (int i = 0; i < la.length; i++) {
			
				la[i].update(sep.getSegment(), null);
				
				if (la[i] != sep.getSegment()) {
					l2[pos] = la[i];
					pos++;
				}
			}
		
				
			addUndo(new ArrayUndo(la, l2, "Erase"));
			la = l2;
			createAllSegs();
		}
		
		if (createSegment != null) {
			createSegment.end();
			addUndo(createSegment);
			createSegment = null;
			
		}
		
		recomp();
		sep = null;
		osep = null;
		showEdit();
	}
	
	
	
	/**
	 * Show the edit panel for the selected segment.
	 */
	public void showEdit() {
		seppanel.removeAll();
		if (sep != null) {
			// the editcanvas variable should be set
			// but let's make 100% sure
			sep.getSegment().ec = this;
			seppanel.add(sep.getSegment().editPanel());
		}
		seppanel.revalidate();
		seppanel.repaint();
	}
	
	
	
	@Override
	public void leftPress(MouseEvent e) {
		
		
		
		Point2D cp = transform(e);
		
		// setting sep to null is needed for nearEditPoint
		// which uses sep to find snap-to points
		sep = null;
		sep = nearEditPoint(cp);
		
		
		if (selectedTool == Tool.TOOL_SELECT) {
			if (sep == null) osep = null;
			// display edit control
			showEdit();
			
			
			return; // move existing piece, sep, or null if none
			
		}
		
		
		
		if (selectedTool == Tool.TOOL_ERASE) {
			erase();
			return;
		}
		
		
		
		Point2D cpp = cp;
		
		
		if (selectedTool == Tool.TOOL_TEXT) {
			Label rl = new Label("Label", new Distance(30, Distance.Measure.FEET), Color.white, cpp, 0);
			
			rl.ec = this;
			
			
			extendArray(rl);
			
			
			
			sep = rl.nearEditPoint(cp, null);
			return;
			
			
		}
		
		

		
		// create new segment
		
		RailSegment r;
		if (sep != null && sep.isAnchorSource()) cpp = sep.getPoint(); //snap to
		

		
		
		
		switch (selectedTool) {
		case TOOL_TRACK:
			r = new TrackSegment(null, null, new Line2D.Double(cpp, cpp));
			break;
		case TOOL_HTRACK:
			r = new HiddenSegment(null, null, new Line2D.Double(cpp, cpp));
			break;
		case TOOL_LUTRACK:
			r = new LUSegment(null, null, new Line2D.Double(cpp, cpp), new Car[0], true);
			break;
		case TOOL_HLUTRACK:
			r = new HiddenLUSegment(null, null, new Line2D.Double(cpp, cpp), new Car[0], true);
			break;
		case TOOL_EETRACK:
			eecnt++;
			r = new EESegment(null, null, new Line2D.Double(cpp, cpp), "EE #" + Integer.toString(eecnt));
			break;
		case TOOL_CROSSING:
			r = new Crossing(null, null, new Line2D.Double(cpp, cpp));
			break;
		case TOOL_CURVE:
			r = new Curve(null, null, new QuadCurve2D.Double(cpp.getX(), cpp.getY(),
					cpp.getX() - 15, cpp.getY() - 15,
					//cpp.getX() + 15, cpp.getY() + 15,
					cpp.getX(), cpp.getY()));
			break;
		default:
			return;
			
		}
		
		r.ec = this;
		
		
		createSegment = new CompoundEdit() {
			@Override
			public String getPresentationName() { return "Create Segment"; }
			@Override
			public String getUndoPresentationName() { return "Undo Create Segment"; }
			@Override
			public String getRedoPresentationName() { return "Redo Create Segment"; }
		};
		
		extendArray(r);
		
		
		if (sep != null && sep.isAnchorSource()) {
			// the neareditpoint returns the begin point by default
			// we have to grab the other point
			// a little undercover coupling here
			r.setDest(TrackSegment.POINT_END, false, sep.getSegment());
			sep.anchor(r);
			
			
		}
		// same as above
		
		
		
		
		
		sep = r.nearEditPoint(cp, null);
		
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

}
