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
import net.kolls.railworld.edit.SegmentEditPoint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Provides a Y connector which can be toggled by the user to route trains.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Switch extends RailSegment {
	

	private static final Distance zd = new Distance(0, Distance.Measure.FEET);
	@Override
	public Distance length() { return zd; }
	
	
	@Override
	public Point2D getPoint(RailSegment s, double pos) { return pts[0]; }
	//Point2D np = new Point2D.Double(myPoint.getX()*zoom, myPoint.getY()*zoom );
	//return np; }
	
	
	
	
	@Override
	public String mouseOver(Point2D pos) { return null; }
	
	
	
	private boolean d1h, d2h;
	
	private Line2D makeLine(RailSegment r) {
		Line2D l;
		boolean fl = flipped;
		ArrayList<RailSegment> nzs;
		
		if (r == dests[1]) 
			flipped = false;
		 else 
			flipped = true;
			
		
		nzs = destNZ(dests[0]);
		
		RailSegment d = nzs.get(nzs.size()-1);
		RailSegment orig = nzs.size() > 1 ? nzs.get(nzs.size() - 2) : dests[0];
		
		if (r == dests[1])
			d1h = d.carHidden();
		else
			d2h = d.carHidden();
		
		l = new Line2D.Float(d.getPoint(orig,1), pts[0]);
		
		flipped = fl;
		
		
		
		return l;
	}
	@Override
	public boolean singleton() { return true; }
	
	/**
	 * When a train enters this segment, the switch will automatically
	 * flip, if necessary, to align itself with the direction the train
	 * is coming from.
	 */
	@Override
	public void enter(Train t) {
		// check if we need to flip the switch

		
			
		if (t.pos.orig == dests[1] && flipped == true) {
			flipped = false;
			Sounds.switchd.play();
		}
		if (t.pos.orig == dests[2] && flipped == false) {
			flipped = true;
			Sounds.switchd.play();
		}	
		

	}
	private static final Distance id = new Distance(12.0, Distance.Measure.FEET);
	private Line2D bl, e1l, e2l, indicator;
	private Stroke regulars, dashs;
	
	@Override
	public void recomp() {
		if (dests[0] == null || dests[1] == null || dests[2] == null) return;
		
		
		bl = makeLine(dests[0]);
		e1l = makeLine(dests[1]);
		e2l = makeLine(dests[2]);
		
		indicator = RailCanvas.angle(bl, pts[0], Math.PI / 2.0, id);
		
		dashs = new BasicStroke(RAIL_WIDTH.iPixels(),BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, new float[] {5.0f, 5.0f}, 0.0f);
		regulars = new BasicStroke(RAIL_WIDTH.iPixels());
		
	}
	
	@Override
	public void draw(int z, Graphics2D gc) {
		if (z != 4) return;
		
		Line2D l;

		boolean dashed;
		
		if (flipped) {
			l = e2l; 
			dashed = d2h; 

		} else {
			l = e1l;
			dashed = d1h; 
		}

		

		gc.setPaint(Color.lightGray);
	
		
		
		if (dashed) 
			gc.setStroke(dashs);
		else 
			gc.setStroke(regulars);
	
		gc.draw(l);
		
		if (flipped) 
			gc.setPaint(Color.red);
		else 
			gc.setPaint(Color.green);
		
		gc.setStroke(regulars);
		gc.draw(indicator);
		

	}
	
	
	@Override
	public RailSegment dest(RailSegment src) {
		

		if (src == dests[1] || src == dests[2]) return dests[0];
		if (flipped) return dests[2]; else return dests[1];
	}

	/**
	 * Create a switch.
	 * 
	 * @param bg The origin segment
	 * @param en1 The straight-lined destination segment
	 * @param en2 The turn-out destination segment
	 * @param pos The location on the map
	 */
	public Switch(RailSegment bg, RailSegment en1, RailSegment en2, Point2D pos) {
		dests = new RailSegment[3];
		dests[0] = bg;
		dests[1] = en1;
		dests[2] = en2;
		pts = new Point2D[1];
		pts[0] = pos;
		flipped = false;
		recomp();
		trains = new HashSet<Train>();	
	
	}
	
	/**
	 * The origin (base) segment
	 */
	public static final int POINT_BEGIN = 0;
	
	/**
	 * The default straight destination segment
	 */
	public static final int POINT_END1 = 1;
	
	/**
	 * The alternate turn-out destination segment
	 */
	public static final int POINT_END2 = 2;
	
	
	
	@Override
	public boolean click(Point2D pos, RailCanvas rc) {
		if (trains.size() > 0) return false;
		if (indicator.ptSegDist(pos) <= MOUSE_NEAR.iPixels()) {
		
			flipped = !flipped;
			Sounds.switchd.play();
			
			
			
			return true;
		} 
		return false;
	}
	@Override
	public boolean isDynamic() { return true; }

	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment anchor) {
		
		if (anchor == null || anchor instanceof TrackSegment) return new SEP(this);
		return null;
	}
	
	@SuppressWarnings("serial")
	private class SwitchEditUndo extends AbstractUndoableEdit {
		
		private JPanel t;
		
		private boolean doit;
		
		/**
		 * Create an undo that tracks which leg is the turnout.
		 */
		public SwitchEditUndo() {
		
			t = mp;
			
			doit = true;
		}
		
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
		
			if (!doit) return;
			
			RailSegment tmp = dests[1];
			dests[1] = dests[2];
			dests[2] = tmp;
			recomp();
			
					
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			if (!doit) return;
			
			RailSegment tmp = dests[1];
			dests[1] = dests[2];
			dests[2] = tmp;
			recomp();
			
			
			
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof SwitchEditUndo) {
				SwitchEditUndo x = (SwitchEditUndo)anEdit;
				if (x.t != t) return false;
				
				doit = !doit;
				
				return true;
			} else return false;
		}
		
		@Override
		public String getPresentationName() { return "Flip Switch Default"; }
	}
	
	private JPanel mp;
	
	@Override
	public JPanel editPanel() {
		mp = new JPanel();
		
		
		JButton flip = new JButton("Flip Default");
		mp.add(flip);
		flip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ec.addUndo(new SwitchEditUndo());
				RailSegment tmp = dests[1];
				dests[1] = dests[2];
				dests[2] = tmp;
				recomp();
				ec.recomp();
				
			}
		});
		
		return mp;
		
	}
	@Override
	public boolean canErase() { return true; }
	
	
	
	
	/**
	 * Indicates if the switch is in the default or flipped orientation.
	 * Default connects begin to end1, flipped connects begin to end2
	 */
	public boolean flipped;
	
	
	// switch edit point
	private class SEP extends SegmentEditPoint {
		private Switch ms;
		
		
		/**
		 * Create a switch edit point.
		 * 
		 * @param s The switch
		 */
		public SEP(Switch s) {
			super(new RailSegment[] {s, dests[0], dests[1], dests[2]}, 0, Color.green);
			ms = s;
			
		}
		@Override
		public boolean isAnchorSource() {
			return false;
			
		}
		@Override
		public RailSegment anchor(RailSegment r) {
			// convert into a four way with r
			FourWay fw = new FourWay(dests[0], dests[1], r, dests[2], pts[0]);
			dests[0].update(ms, fw);
			dests[1].update(ms, fw);
			dests[2].update(ms, fw);
			return fw;
			
			
		}
		
	
	}

}
