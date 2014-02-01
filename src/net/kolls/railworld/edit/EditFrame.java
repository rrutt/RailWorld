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


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import net.kolls.railworld.Distance;
import net.kolls.railworld.GameLoop;
import net.kolls.railworld.Images;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailFrame;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.YesNoCancel;
import net.kolls.railworld.edit.EditCanvas;
import net.kolls.railworld.io.MetaData;
import net.kolls.railworld.io.RWMMapFilter;
import net.kolls.railworld.io.RWMWriter;
import net.kolls.railworld.opening.MetaDataPanel;

/**
 * A window for editing segments.  Encloses an {@link EditCanvas}.
 * 
 * @author Steve Kollmansberger
 *
 */
public class EditFrame extends RailFrame {

	private EditCanvas jdb;
	private JPanel buts;
	private ButtonGroup tools;
	private JFileChooser jfc = new JFileChooser();
	private MetaData mmd;
	private GameLoop gl;
	private JButton  undo, redo;
	
	private void addButton(Object label, final EditCanvas.Tool tool, String tt) {
		addButton(label, tool, tt, false);
	}
	
	private void addButton(Object label, final EditCanvas.Tool tool, String tt, boolean selected) {
		
		JToggleButton b;
		
		if (label instanceof ImageIcon) b = new JToggleButton((ImageIcon)label);
			else b = new JToggleButton((String)label);
		
		b.setToolTipText(tt);
		
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jdb.selectedTool = tool;
			}
		});
		
		b.setSelected(selected);
		
		tools.add(b);
		buts.add(b);
	}
	
	public void actionPerformed(ActionEvent e) {
		Point2D p;
		
		
		if (e.getActionCommand().equals("Edit")) {
			mmd.track = jdb.trackLength();
			final MetaDataPanel mdp = new MetaDataPanel(mmd, null, true);
			final JDialog d = new JDialog(this, "Edit Metadata", true);
			d.add(mdp);
			JPanel mp = new JPanel();
			JButton b = new JButton("OK");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ex) {
					
					mmd = mdp.getMD();
					Distance.feetPerPixels = mmd.feetPerPixel;
					setTitle("Rail World - " + mmd.title);
					d.dispose();
				}
			});
			mp.add(b);

			b = new JButton("Cancel");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ex) {
					d.dispose();
				}
			});
			mp.add(b);

			d.add(mp, BorderLayout.SOUTH);
			
			
			d.getRootPane().setDefaultButton(b);
			
			d.pack();
			
			d.setVisible(true);
			
			
			
			
		}
		
		if (e.getActionCommand().equals("Quit")) {
			
			// ask to save only if unsaved modifications
			if (jdb.justSaved == false) {
			
				final int response = YesNoCancel.showDialog(this, "Do you wish to save the map before quitting?", 
						"Quit and/or Save", "Save and Quit", "Quit and Don't Save", "Don't Quit");
	        	if (response != JOptionPane.YES_OPTION && response != JOptionPane.NO_OPTION) return;
	        
	    		if (response == JOptionPane.YES_OPTION) {
					actionPerformed(new ActionEvent(this, 0, "Save"));
				
				}
			}
	    		
			gl.stop();
			jdb.stop();
			
			
		}
		
		if (e.getActionCommand().equals("ZoomIn")) {
			
			
			p = jdb.getCenterPoint();

			RailCanvas.zoom *= 1.5;
					
					
			jdb.recomp();
			jdb.submitCenterCoords((int)p.getX(), (int)p.getY());
					
			
				
			
			
			
			
			
		}
		
		if (e.getActionCommand().equals("ZoomOut")) {
			p = jdb.getCenterPoint();
			
			
			
			RailCanvas.zoom /= 1.5;
			
			
			
			jdb.recomp();
			jdb.submitCenterCoords((int)p.getX(), (int)p.getY());
			
			
		}
		
		if (e.getActionCommand().equals("SEP")) {
			jdb.displaySEP = ((JToggleButton)e.getSource()).isSelected();
			
			jdb.recomp();
		}
		
		if (e.getActionCommand().equals("Save")) {
			File file;
			

			
			// do we already have a file to save to?
			// warning: if import from yrd still ask for place to save
			
			if (mmd.ourFile.isDirectory() || mmd.ourFile.getName().toUpperCase().endsWith(".YRD")) {
				// since images and maps must be in the same directory, start the file chooser out 
				// in the directory where the image lives
				jfc.setCurrentDirectory(mmd.ourFile);
				
				File defrwm = new File(mmd.imgfile.substring(0, mmd.imgfile.length() - 3) + "RWM"); // same file name, different ext
				jfc.setSelectedFile(defrwm);
				
				jfc.addChoosableFileFilter(new RWMMapFilter());
				int rv = jfc.showSaveDialog(this);
				if (rv != JFileChooser.APPROVE_OPTION) return;
				file = jfc.getSelectedFile();
				if (file.getName().toUpperCase().endsWith(".RWM") == false) {
					// add extension if need
					file = new File(file.getParent(), file.getName() + ".rwm");
					
				}
	            if (file.exists ()) {
	                int response = JOptionPane.showConfirmDialog (this,
	                  "Overwrite existing file?","Confirm Overwrite",
	                   JOptionPane.YES_NO_OPTION,
	                   JOptionPane.QUESTION_MESSAGE);
	                if (response != JOptionPane.YES_OPTION) return;
	            }

	            mmd.ourFile = file;
			} else {
				file = mmd.ourFile;
			}
			
			
	        

	            

	            
	         mmd.centerX = (int)jdb.getCenterPoint().getX();
	         mmd.centerY = (int)jdb.getCenterPoint().getY();
	         mmd.feetPerPixel = Distance.feetPerPixels;
	         mmd.zoom = RailCanvas.zoom;
	            
	            
	            
	            
	         try {
	        	 RWMWriter.write(jdb.la, mmd, file);
	        	 jdb.justSaved = true;
	        	 checkUndoRedo();
	         } catch (Exception ex) {
	        	 JOptionPane.showMessageDialog(this, "There was an error saving the file, reason: "+ex.getMessage(), "Error Saving", JOptionPane.ERROR_MESSAGE);
	         }

	            
	            
	         
			
			
		}
		
		if (e.getActionCommand().equals("Undo")) {
			jdb.undos.undo();
			
			checkUndoRedo();
			jdb.recomp();
			jdb.showEdit();
			
		}
		
		if (e.getActionCommand().equals("Redo")) {
			jdb.undos.redo();
			
			checkUndoRedo();
			jdb.recomp();
			jdb.showEdit();
			
		}
		
		
	}
	
	private void addWidgets() {
		

		
		getContentPane().add(jdb); // defaults to center
		
		tools = new ButtonGroup();
		
		buts = new JPanel();
		buts.setLayout(new GridLayout(5,2));
		
		addButton(Images.pointer, EditCanvas.Tool.TOOL_SELECT, "Edit/Move Segment", true);
		
		addButton(Images.eraser, EditCanvas.Tool.TOOL_ERASE, "Remove a Segment");
		
		
		
		
		addButton(Images.track, EditCanvas.Tool.TOOL_TRACK, "Add Track");
		
		addButton(Images.lu, EditCanvas.Tool.TOOL_LUTRACK, "Add Load/Unload Track");
		
		
		addButton(Images.hidden, EditCanvas.Tool.TOOL_HTRACK, "Add Hidden Track");
		
		addButton(Images.hiddenlu, EditCanvas.Tool.TOOL_HLUTRACK, "Add Hidden Load/Unload Track");
		

		addButton(Images.ee, EditCanvas.Tool.TOOL_EETRACK, "Add Map Entrance/Exit");
		
		addButton(Images.crossing, EditCanvas.Tool.TOOL_CROSSING, "Add Crossing");
		
		addButton("Text", EditCanvas.Tool.TOOL_TEXT, "Place a Label");
		
		addButton(Images.curve, EditCanvas.Tool.TOOL_CURVE, "Add Curve");
		
		JPanel bh = new JPanel();
		bh.setLayout(new BoxLayout(bh, BoxLayout.LINE_AXIS));
		bh.add(Box.createHorizontalGlue());
		
		bh.add(buts);
		bh.add(Box.createHorizontalGlue());
		
		
		JPanel holder = new JPanel();
		holder.setLayout(new BorderLayout());
		holder.add(bh, BorderLayout.NORTH);
		
		jdb.seppanel = new JPanel();
		holder.add(jdb.seppanel, BorderLayout.CENTER);
		
		
		rightPanel.add(holder, BorderLayout.CENTER);
		
		
		pack();
	}
	
	private void keypresses() {
		// allow arrow keys to move pixel level

		
		
		
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), "up");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), "left");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), "right");
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), "down");
		getRootPane().getActionMap().put("up", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				
				jdb.xymove(0,-1);
			}
		});
		getRootPane().getActionMap().put("down", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				jdb.xymove(0,1);
			}
		});
		getRootPane().getActionMap().put("left", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				jdb.xymove(-1,0);
			}
		});
		getRootPane().getActionMap().put("right", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				jdb.xymove(1,0);
			}
		});
	}
	
	/**
	 * Create an edit frame.
	 * 
	 * @param lines The lines to use
	 * @param source The source image
	 * @param md The metadata (may be edited)
	 */
	public EditFrame(RailSegment[] lines, BufferedImage source, MetaData md) {

		super(lines, source, "Map");
		
		mmd = md;
		
		setTitle("Rail World - " + md.title);
		
		jdb = new EditCanvas(source, lines, miniv);
		
		addWidgets();
		
		keypresses();
		
		jdb.submitCenterCoords(md.centerX, md.centerY);
		
		JButton b;
		b = new JButton(Images.edit);
		b.setActionCommand("Edit");
		b.setToolTipText("Edit Metadata");
		b.addActionListener(this);
		toolBar.add(b);
		
		JToggleButton tb;
		tb = new JToggleButton(Images.sep, true);
		tb.setActionCommand("SEP");
		tb.setToolTipText("Show/Hide Edit Points");
		tb.addActionListener(this);
		toolBar.add(tb);
		
		toolBar.addSeparator();
		
		
		undo = new JButton(Images.undo);
		undo.setActionCommand("Undo");
		undo.setToolTipText("Undo Drawing Edit");
		undo.addActionListener(this);
		undo.setEnabled(false);
		toolBar.add(undo);
		
		redo = new JButton(Images.redo);
		redo.setActionCommand("Redo");
		redo.setToolTipText("Redo Drawing Edit");
		redo.addActionListener(this);
		redo.setEnabled(false);
		toolBar.add(redo);
		
		jdb.recomp();
		
	}
	
	private void checkUndoRedo() {
		redo.setEnabled(jdb.undos.canRedo());
		redo.setToolTipText(jdb.undos.getRedoPresentationName());
		undo.setEnabled(jdb.undos.canUndo());
		undo.setToolTipText(jdb.undos.getUndoPresentationName());
		
		
		if (jdb.justSaved == false)
			setTitle("Rail World - " + mmd.title + " [modified]");
		else
			setTitle("Rail World - " + mmd.title);
	}
	
	@Override
	public void startLoop() {
		gl = new GameLoop(jdb) {
			@Override
			protected void prePaint() { }
			@Override
			protected void run() {	checkUndoRedo(); } };
		gl.gameLoop();
	}
}

