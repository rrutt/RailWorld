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
import net.kolls.railworld.tuic.TrainPainter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.awt.*;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * 
 * Load/unload segment.
 * 
 * @author Steve Kollmansberger
 *
 */
public class LUSegment extends TrackSegment {
	private Car[] lucars;
	
	/**
	 * Indicates if the segment should draw the cars it accepts.
	 */
	protected boolean drawAc;
	

	/**
	 * Create a load/unload segment.
	 * 
	 * @param bg Begin segment
	 * @param en End segment
	 * @param crds Line coords
	 * @param accept Which cars do we accept (load or unload) here.
	 * @param drawAccept Draw cars that we accept?
	 */
	public LUSegment(RailSegment bg, RailSegment en, Line2D crds, Car[] accept, boolean drawAccept) {
		super(bg, en, crds);
		lucars = accept;
		drawAc = drawAccept;
		
	}
	
	/**
	 * 
	 * @return An array contain the {@link Car}s the segment accepts.
	 */
	public Car[] lu() {
		return lucars;
		
	}
	/**
	 * 
	 * @param drawAccept Should the segment draw accepted cars or not?
	 */
	public void setDrawAccept(boolean drawAccept) {
		drawAc = drawAccept;
	}
	
	/**
	 * 
	 * @return If the segment draws accepted cars
	 */
	public boolean doesDrawAccept() {
		return drawAc;
	}
	
	private String cars;
	
	@Override
	public void recomp() {
		super.recomp();
		
		cars = "";
		if (lucars == null) return;
		
		String loads = "", unloads = "";
		
		for (int i = 0; i < lucars.length; i++) {
			if (lucars[i].loaded()) {
				if (unloads.length() > 0) unloads += ", ";
				unloads += lucars[i].show();
			} else {
				if (loads.length() > 0) loads += ", ";
				loads += lucars[i].show();	
			}
			
		}
		
		if (loads.length() > 0) {
			cars = "Loads: " + loads;
		}

		if (unloads.length() > 0) {
			if (cars.length() > 0) cars += "; ";
			cars += "Unloads: " + unloads;
		}
		
	}
	
	@Override
	public String mouseOver(Point2D pos) {
		if (coords.ptSegDist(pos) <= MOUSE_NEAR.iPixels()) {
			return cars;
		}
		return null;
	}
	
	@Override
	public boolean isDynamic() { return true; } // to provide mouse over
	
	/**
	 * Draw the cars which this LU segment accepts.  The cars are drawn next
	 * to the segment.
	 * 
	 * @param gc The graphics context to draw on
	 */
	protected void drawAccept(Graphics2D gc) {
			Distance belowDist = new Distance(15, Distance.Measure.FEET);
			Line2D c2 = coords;
			
			Point2D orig = RailCanvas.angle(c2, c2.getP1(), 90, belowDist).getP2();
			

			

			Line2D l;

			//if (begin instanceof LUSegment) return;

			// paint the cars we accept below the track
			for (int i = 0; i < lucars.length; i++) {
				TrainPainter tp = new TrainPainter(null, gc, false, null);

 				l = RailCanvas.angle(c2, orig, 0, lucars[i].length());
				
				tp.segment(lucars[i], l);
				
				orig = RailCanvas.angle(c2, l.getP2(), 0, belowDist).getP2();
			}
	}
	
	/**
	 * 
	 * @param c A {@link Car}
	 * @return If that car can be loaded/unloaded at this segment.
	 */
	public boolean canLU(Car c) {
		for (int i = 0; i < lucars.length; i++) {
			if (lucars[i].equals(c)) {
				
				return true;
			}
		}
		return false;
	}
	@Override
	public void draw(int z, Graphics2D gc) {
		
		Paint p = gc.getPaint();

		
		

		
		if (z == 2) {
			
			gc.setPaint(Color.magenta);
			
			
			
			gc.setStroke(railStroke);
			gc.draw(coords);
			
			


			if (drawAc) drawAccept(gc);
			
			
		} else {
			super.draw(z, gc);
		}

		gc.setPaint(p);

		
	}
	
	
	
	@SuppressWarnings("serial")
	private class LUEditUndo extends AbstractUndoableEdit {
		private Car[] lu, _lu;
		private JPanel t;
		
		/**
		 * Create a load/unload undo that tracks undoing selecting cars
		 * to load and unload.
		 */
		public LUEditUndo() {
			
			lu = new Car[lucars.length];
			System.arraycopy(lucars, 0, lu, 0, lu.length);
		
			t = mp;
		}
		
		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			
			
			_lu = new Car[lucars.length];
			System.arraycopy(lucars, 0, _lu, 0, _lu.length);
				
			
			
			
			lucars = lu;
			
			
		}
		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			
			lucars = _lu;
			
			
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			if (anEdit instanceof LUEditUndo) {
				LUEditUndo x = (LUEditUndo)anEdit;
				if (x.t != t) return false;
				
				return true;
			} else return false;
		}
		
		@Override
		public String getPresentationName() { return "Set Accepted Cars"; }
	}
	
	private JToggleButton carButton(final Car c) {
		
		
		Icon i = new ImageIcon(TrainPainter.image(c, ec));
		JToggleButton j = new JToggleButton(i, canLU(c));
		j.setToolTipText(c.show());
		j.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// because of the way the cars are added to the train
				// we have to have separate instances for each car
				// because a referencer is just copied
				// so it's actually many of the same car otherwise
				// this does create a problem for selected cars
				ec.addUndo(new LUEditUndo());
				try {
					
					Car c2 = (Car)c.newInstance();
					Car[] nca;
					if (c.loaded()) c2.load(); else c2.unload();
					
					
					if (e.getStateChange() == ItemEvent.SELECTED) {
						nca = new Car[lucars.length + 1];
						System.arraycopy(lucars, 0, nca, 0, lucars.length);
						nca[lucars.length] = c2;
					} else {
						int ii, jj = 0;
						nca = new Car[lucars.length - 1];
						for (ii = 0; ii < lucars.length; ii++) {
							if (lucars[ii].equals(c2) == false) {
								nca[jj] = lucars[ii];
								jj++;
							}
						}
					
					}
					
					lucars = nca;	
				} catch (Exception ex) {
					ex.printStackTrace();
					
				}
				
				ec.recomp();
				
			}
		});
		return j;
		
	
	}
	
	private JPanel mp;
		
	@Override
	public JPanel editPanel() {
		mp = new JPanel();
		
		mp.setLayout(new GridLayout(0,2));
		
		mp.add(new JLabel("Load"));
		mp.add(new JLabel("Unload"));
		
		Car c;
		

		ArrayList<Car> at = Factories.cars.allTypes();
		for (int i = 0; i < at.size(); i++) {
			c = at.get(i);
			if (c.isLoadable() == false) continue; // LU only handles loadable cars
			c.unload();
			mp.add(carButton(c));
			try {
				c = (Car)c.newInstance();	
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			c.load();
			mp.add(carButton(c));
		}
		
		JPanel mmp = new JPanel();
		mmp.setLayout(new BoxLayout(mmp, BoxLayout.PAGE_AXIS));
		mmp.add(mp);
		
		
		mmp.add(Box.createRigidArea(new Dimension(0,5)));
		
		final JCheckBox da = new JCheckBox("Display Accepted Cars");
		da.setAlignmentX(Component.CENTER_ALIGNMENT);
		da.setSelected(doesDrawAccept());
		da.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDrawAccept(da.isSelected());
				ec.recomp();
			}
		});
		mmp.add(da);
		
		
		
		
		return mmp;
		
	}

}

