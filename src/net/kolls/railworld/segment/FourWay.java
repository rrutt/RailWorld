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


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import net.kolls.railworld.Distance;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.edit.SegmentEditPoint;


/**
 * Track crossing itself without switching capabilities.
 * 
 * @author Steve Kollmansberger
 *
 */
public class FourWay extends RailSegment {

	/**
	 * Starting segment of the first track
	 */
	public static final int POINT_BEGINA = 0;
	/**
	 * Ending segment of the first track
	 */
	public static final int POINT_ENDA = 1;
	/**
	 * Starting segment of the second track
	 */
	public static final int POINT_BEGINB = 2;
	/**
	 * Ending segment of the second track
	 */
	public static final int POINT_ENDB = 3;
	
	
	
	@Override
	public void recomp() { 
		double px = RAIL_BED_WIDTH.pixels();
		
		el = new Ellipse2D.Double(pts[0].getX() - px/2, pts[0].getY() - px/2, px, px);
	}

	private static final Distance zd = new Distance(0, Distance.Measure.FEET);
	@Override
	public Distance length() { return zd; }
	
	
	@Override
	public Point2D getPoint(RailSegment s, double pos) { return pts[0]; }
	//Point2D np = new Point2D.Double(myPoint.getX()*zoom, myPoint.getY()*zoom );
	//return np; }

	/**
	 * Create a four way crossing
	 * 
	 * @param ba Beginning segment of track A
	 * @param ena Ending segment of track A
	 * @param bb Beginning segment of track B
	 * @param enb Ending segment of track B
	 * @param pos Location on map
	 */
	public FourWay(RailSegment ba, RailSegment ena, RailSegment bb, RailSegment enb, Point2D pos) {
		dests = new RailSegment[4];
		dests[0] = ba;
		dests[2] = bb;
		dests[1] = ena;
		dests[3] = enb;
		pts = new Point2D[1];
		pts[0] = pos;
		
		trains = new HashSet<Train>();	
	
	}
	
	@Override
	public boolean canErase() {
		return true;
	}

	
	@Override
	public String mouseOver(Point2D pos) { return null; }

	@Override
	public RailSegment dest(RailSegment source) {
		if (source == dests[0]) return dests[1];
		if (source == dests[1]) return dests[0];
		if (source == dests[2]) return dests[3];
		if (source == dests[3]) return dests[2];
		return null;
		
	}

	private Ellipse2D el;
	
	@Override
	public void draw(int z, Graphics2D gc) {
		Paint p = gc.getPaint();




		
		
		if (z == 4) {
			
			
			gc.setPaint(Color.lightGray);

			gc.fill(el);
			
			for (RailSegment r : dests)
				r.draw(2, gc);


		}
		
		if (z == 5) {
			// but we'll draw in the straight indicator for edit mode
			
			gc.setPaint(Color.lightGray);
			
			Line2D l;
			
			
			l = makeLine(dests[0]);
			
			gc.setStroke(new BasicStroke(RAIL_WIDTH.iPixels()));
			gc.draw(l);
			
			
			
			l = makeLine(dests[1]);
			gc.draw(l);

			
		}
		
		gc.setPaint(p);

	}
	
	private Line2D makeLine(RailSegment r) {
		Line2D l;
		
		
		
		l = (((TrackSegment)r).getCoords());
		
		
		return l;
	}
	
	@Override
	public boolean isDynamic() { return true; }
	
	@Override
	public boolean singleton() { return true; }
	
	
	@SuppressWarnings("serial")
	private class FWEditUndo extends AbstractUndoableEdit {
		
		private JPanel t;
		
		private boolean doit;
		
		/**
		 * Create an undo for flipping the the "straight-through"
		 * This works by flipping end A and end B, so if you have
		 * start A <-> end A 
		 * start B <-> end B
		 * it becomes
		 * start A <-> end B
		 * start B <-> end A
		 */
		public FWEditUndo() {
		
			t = mp;
			
			doit = true;
		}
		
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
		
			if (!doit) return;
			
			RailSegment tmp = dests[1];
			dests[1] = dests[3];
			dests[3] = tmp;
			recomp();
			
					
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			if (!doit) return;
			
			RailSegment tmp = dests[1];
			dests[1] = dests[3];
			dests[3] = tmp;
			recomp();
			
			
			
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof FWEditUndo) {
				FWEditUndo x = (FWEditUndo)anEdit;
				if (x.t != t) return false;
				
				doit = !doit;
				
				return true;
			} else return false;
		}
		
		@Override
		public String getPresentationName() { return "Flip Fourway Straight"; }
	}
	
	private JPanel mp;
	
	@Override
	public JPanel editPanel() {
		mp = new JPanel();
		
		
		JButton flip = new JButton("Flip Straight");
		mp.add(flip);
		flip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ec.addUndo(new FWEditUndo());
				
				RailSegment tmp = dests[1];
				dests[1] = dests[3];
				dests[3] = tmp;
				
				
				
				
				recomp();
				ec.recomp();
				
			}
		});
		
		return mp;
	}

	

	
	
	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment anchor) {
		if (anchor == null) return new FWEP(this);
		return null;
	}
	
	
		
	// fourway edit point
	private class FWEP extends SegmentEditPoint {
		
		
		
		/**
		 * Create a fourway edit point.  Created by {@link FourWay#createSEP(int, RailSegment)}.
		 * @param s The fourway to edit
		 */
		public FWEP(FourWay s) {
			super(new RailSegment[] {s, dests[0], dests[1], dests[2], dests[3]}, 0, Color.green);
			
		}
		@Override
		public boolean isAnchorSource() {
			return false;
			
		}
		@Override
		public RailSegment anchor(RailSegment r) {
			return null;
		
		}
		
	
	}


}
