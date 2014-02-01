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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import net.kolls.railworld.Distance;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.edit.SegmentEditPoint;



/**
 * A label for displaying text.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Label extends RailSegment {

	@Override
	public void recomp() { }
	
	/**
	 * What the label says
	 */
	public String value;
	
	/**
	 * The label's color
	 */
	public Color c;
	
	/**
	 * How tall is the label text
	 */
	public Distance size;
	
	
	/**
	 * Rotation angle, in radians.
	 */
	public double angle;
	
	/**
	 * The coordinates refer to upper-left or the center of the label?
	 * By default this is true, meaning the coordinates are the center of the label.
	 */
	public boolean centered;
	
	/**
	 * Create a label
	 * 
	 * @param value Text value
	 * @param size Font size
	 * @param color Color
	 * @param pos Location on map (this is the center point)
	 * @param angle Angle, in radians
	 * @see RailCanvas#drawOutlineFont(Graphics2D, int, int, String, int, Color, double, boolean)
	 */
	public Label(String value, Distance size, Color color, Point2D pos, double angle) {
		this.value = value;
		this.size = size;
		c = color;
		pts = new Point2D[1];
		pts[0] = pos;
		this.angle = angle;
		centered = true;
		
		dests = new RailSegment[0];
		
	}
	
	
	
	@Override
	public boolean canErase() {
		return true;
	}

	

	@Override
	public String mouseOver(Point2D pos) { return null; }
	
	
	@Override
	public RailSegment dest(RailSegment source) {
		return null;
	}

	@Override
	public void draw(int z, Graphics2D gc) {
		if (z == 1 && value.length() > 0) {
			Point2D p2 = pts[0];
			
			RailCanvas.drawOutlineFont(gc, (int)p2.getX(), (int)p2.getY(), value, size.iPixels(), c, angle, centered);
			
			
		}

		

	}

	
	private class LabelEditUndo extends AbstractUndoableEdit {
		private Distance _size, _size2;
		private Color _c, _c2;
		private String _lbl, _lbl2;
		private double _angle, _angle2;
		private JPanel t;
		
		/**
		 * Create a label edit undo.  This undo tracks all sorts of
		 * label changes, like size, color, text, etc.
		 */
		public LabelEditUndo() {
			_size = size;
			_c = c;
			_lbl = value;
			_angle = angle;
			t = mp;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			
			_size2 = size;
			_c2 = c;
			_lbl2 = value;
			_angle2 = angle;
			
			size = _size;
			c = _c;
			value = _lbl;
			angle = _angle;
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			size = _size2;
			c = _c2;
			value = _lbl2;
			angle = _angle2;
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof LabelEditUndo) {
				LabelEditUndo x = (LabelEditUndo)anEdit;
				if (x.t != t) return false;
				
				return true;
			} else return false;
		}
		@Override
		public String getPresentationName() { return "Configure Label"; }
	}
	
	private JPanel mp;
	
	private void addColorButton(final Color cl) {
		JButton tb = new JButton();
		tb.setPreferredSize(new Dimension(24,24));
		tb.setBackground(cl);
		tb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ec.addUndo(new LabelEditUndo());
				c = cl;
				ec.recomp();
			}
			
		});
		mp.add(tb);
		
		
	}
	
	private void addSizeButton(final Distance px, final String lbl) {
		JButton tb = new JButton(lbl);
		
		
		tb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ec.addUndo(new LabelEditUndo());
				size = px;
				ec.recomp();
			}
			
		});
		mp.add(tb);
	}
	
	
	@Override
	public JPanel editPanel() {
		JPanel gp = new JPanel();
		gp.setLayout(new BoxLayout(gp, BoxLayout.PAGE_AXIS));
		mp = new JPanel();
		
		
		mp.add(new JLabel("Text:"));
		
		final JTextField txtlbl = new JTextField(value,10);
		mp.add(txtlbl);
		
		
		
		txtlbl.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {	}

			public void focusLost(FocusEvent e) {
				ec.addUndo(new LabelEditUndo());
				value = txtlbl.getText(); ec.recomp(); 
				
			}
			
		});
		
		gp.add(mp);
		
		
		mp = new JPanel();
		
		
		mp.add(new JLabel("Angle (deg):"));
		
		final JTextField txtangle = new JTextField(Double.toString(angle / Math.PI / 2.0 * 360.0),3);
		mp.add(txtangle);
		txtangle.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {	}

			public void focusLost(FocusEvent e) {
				
				ec.addUndo(new LabelEditUndo());
				try {
					// we are asking in degrees but the system uses radians
					angle = Double.valueOf(txtangle.getText()) / 360.0 * Math.PI * 2.0;
				} catch (Exception e2) { }
				
				ec.recomp(); 
				
			}
			
		});

		
		
		gp.add(mp);
		
		mp = new JPanel();
		
		
		
		
		addColorButton(Color.cyan);
		addColorButton(Color.magenta);
		addColorButton(Color.pink);
		addColorButton(Color.red);
		addColorButton(Color.yellow);
		addColorButton(Color.white);
		
		
		
		
		
		gp.add(mp);
		
		
		mp = new JPanel();
		mp.setLayout(new GridLayout(2,2));
		
		addSizeButton(new Distance(20, Distance.Measure.FEET), "S");
		addSizeButton(new Distance(30, Distance.Measure.FEET), "M");
		addSizeButton(new Distance(50, Distance.Measure.FEET), "L");
		addSizeButton(new Distance(70, Distance.Measure.FEET), "XL");
		
		gp.add(mp);
		
		
		
		return gp;
	}



	@Override
	public Point2D getPoint(RailSegment start, double myPos) {
		return pts[0];
	}



	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment attach) {
		if (attach == null)
			return new LSEP(this);
		return null;
	}




	private static final Distance zd = new Distance(0, Distance.Measure.FEET);
	@Override
	public Distance length() { return zd; }
	
	

	
	
	// label segment edit point class
	
	private class LSEP extends SegmentEditPoint {
		
		
		/**
		 * Create a label edit point
		 * 
		 * @param t The label to edit
		 */
		public LSEP(Label t) {
			super(new RailSegment[] {t}, 0, Color.green);
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
