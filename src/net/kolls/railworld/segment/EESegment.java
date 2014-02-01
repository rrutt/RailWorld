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
import java.awt.geom.*;
import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Entrance/Exit segment.  Connected to a long, invisible segment which allows trains to smoothly
 * enter and exit the map.
 * 
 * @author Steve Kollmansberger
 *
 */
public class EESegment extends TrackSegment {
	
	/**
	 * The description of this segment.  Used for train creator.
	 */
	public String label;
	
	/**
	 * The hidden segment that trains exit on to.
	 */
	public HiddenSegment HES;
	

	/**
	 * Construct an entry/exit segment
	 * 
	 * @param bg Begin segment
	 * @param en end segment
	 * @param crds Line coords
	 * @param lbl Text
	 */
	public EESegment(RailSegment bg, RailSegment en, Line2D crds, String lbl) {
		super(bg, en, crds);
		// coords don't matter for this segment, as long as it's long
		// long enought to accomodate ANY train! 
		
		label = lbl;
		
		HES = new HiddenSegment(this, null, new Line2D.Double(-1000000,-1000000,-10,-10)) {
			@Override
			public boolean singleton() { return true; }
		};
	
		recomp();
		
	}
	
	@Override
	public void recomp() {
		
		super.recomp();
		
		if (dests[0] == HES || dests[1] == HES) return;
		
		if (dests[0] != null) 
			dests[1] = HES;
		else if (dests[1] != null) dests[0] = HES;
		
		
	}
	
	@Override
	public boolean singleton() {
		
		// multiple trains can co-exist in an EE segment
		// but only one may be in the hidden part at a time
		// since only the head is checked, if the hidden has multiple trains, we're a singleton
		// this will catch two trains created on the same entrance
		if (HES.trains().size() > 1) return true; else return false;
		
	}
	
	@Override
	public boolean isDynamic() { return true; } // just so we can clear HES trains 
	
	
	@Override
	public String mouseOver(Point2D pos) {
		if (coords.ptSegDist(pos) <= MOUSE_NEAR.iPixels()) {
			return label;
		}
		return null;
	}
	
	
	@Override
	public RailSegment dest(RailSegment source) {
		
		
		if (source == HES) {
			if (dests[0] != null && dests[0] != HES) return dests[0]; else return dests[1];
		}
		if (source == null) {
			if (dests[0] == HES) return dests[1]; else return dests[0];
		}
		RailSegment x = super.dest(source);
		if (x == null) return HES; else return x;
	}
	@Override
	public Point2D getPoint(RailSegment start, double myPos) {
		//if (start == HES) start = null;
		return super.getPoint(start, myPos);
	}
	@Override
	public void draw(int z, Graphics2D gc) {
		
		if (z == 4) {
			HES.trains().clear();
			return;
		}
		Paint p = gc.getPaint();

		cap = BasicStroke.CAP_SQUARE; // end segments always have a cap, and its square
		

		if (z == 1) {
			

			gc.setPaint(Color.magenta);
			gc.setStroke(railBedStroke);
			gc.draw(coords);
		}
		if (z == 2) {
			super.draw(z, gc);

		}

		gc.setPaint(p);
	}
	
	@SuppressWarnings("serial")
	private class EEEditUndo extends AbstractUndoableEdit {
		private String _lbl, _lbl2;
		private JPanel t;
		
		/**
		 * Construct an entry/exit label edit undo
		 */
		public EEEditUndo() {
			
			_lbl = label;
		
			t = mp;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			
			
			_lbl2 = label;
			
			
			
			label = _lbl;
			
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			label = _lbl2;
			
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof EEEditUndo) {
				EEEditUndo x = (EEEditUndo)anEdit;
				if (x.t != t) return false;
				
				return true;
			} else return false;
		}
		@Override
		public String getPresentationName() { return "Set EE Segment Name"; }
	}
	
	private JPanel mp;
	
	@Override
	public JPanel editPanel() {
		mp = new JPanel();
		
		
		mp.add(new JLabel("Name:"));
		
		final JTextField txtlbl = new JTextField(label,10);
		mp.add(txtlbl);
		
		txtlbl.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { ec.addUndo(new EEEditUndo()); label = txtlbl.getText(); }
		    public void removeUpdate(DocumentEvent e) {  ec.addUndo(new EEEditUndo());  label = txtlbl.getText(); }
		    public void changedUpdate(DocumentEvent e) {  ec.addUndo(new EEEditUndo());  label = txtlbl.getText(); }
		});
		
		
		return mp;
	}
	
	
	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment anchor) {
		if (dests[ptIdx] == HES && anchor != null) return null;
		
		return super.createSEP(ptIdx, anchor);
		
		
	}
	

}
