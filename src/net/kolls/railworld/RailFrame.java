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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;

import net.kolls.railworld.opening.Options;

/**
 * The main window which contains a canvas, a mini viewer, and the right hand control area.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public abstract class RailFrame extends JFrame implements ActionListener, WindowListener {
	
	

	
	/**
	 * The mini viewer
	 */
	protected MiniViewer miniv;
	
	/**
	 * The left panel
	 */
	protected JPanel leftPanel;
	
	/**
	 * The right panel
	 */
	protected JPanel rightPanel;
	
	/**
	 * The tool bar.  Several buttons are given by default, and individual frames may add more.
	 * Each individual frame, however, is responsible for all the functionality.
	 */
	protected JToolBar toolBar;
	
	/**
	 * Create a rail frame.
	 * 
	 * @param lines A {@link RailSegment} array.
	 * @param source The source image.
	 * @param tbnoun The noun for the button tooltips (game or map) e.g. "Save Game", "Save Map", ...
	 */
	public RailFrame(RailSegment[] lines, BufferedImage source, String tbnoun) {
		super("Rail World");
		
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		setLocationRelativeTo(null);
		setIconImage(Images.frameIcon);
		
		miniv = new MiniViewer();
		miniv.setPreferredSize(new Dimension(195,195));
		miniv.setMaximumSize(new Dimension(195,195));
		miniv.setBackground(Color.white);

		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		JPanel mp = new JPanel();
		mp.setBorder(BorderFactory.createRaisedBevelBorder());
		mp.setPreferredSize(new Dimension(205,205));
		mp.setMaximumSize(new Dimension(205,205));
		mp.add(miniv);
		leftPanel.add(mp, BorderLayout.NORTH);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		
		// since tch looks ugly butting up against the edge,
		// add filler
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(5,5));
		rightPanel.add(jp, BorderLayout.WEST);
		jp = new JPanel();
		jp.setPreferredSize(new Dimension(5,5));
		rightPanel.add(jp, BorderLayout.EAST);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(leftPanel, BorderLayout.WEST);
		getContentPane().add(rightPanel, BorderLayout.EAST);
		
		toolBar = new JToolBar();
		
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		JButton b;
		
		
		
		
		
		b = new JButton(Images.save);
		b.setActionCommand("Save");
		b.setToolTipText("Save " + tbnoun);
		b.addActionListener(this);
		toolBar.add(b);
		
		b = new JButton(Images.stop);
		b.setActionCommand("Quit");
		b.setToolTipText("Quit");
		b.addActionListener(this);
		toolBar.add(b);
		
		toolBar.addSeparator();
		
		b = new JButton(Images.zoomin);
		b.setActionCommand("ZoomIn");
		b.setToolTipText("Zoom In");
		b.addActionListener(this);
		toolBar.add(b);
		
		b = new JButton(Images.zoomout);
		b.setActionCommand("ZoomOut");
		b.setToolTipText("Zoom Out");
		b.addActionListener(this);
		toolBar.add(b);
		
		toolBar.addSeparator();
		
		
		
		getContentPane().add(toolBar, BorderLayout.NORTH);
		


		if (Options.getRemember()) {
			// store everything in options so that it can be easily reset
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			setLocation(prefs.getInt("X", 0), prefs.getInt("Y", 0));
			setPreferredSize(new Dimension(prefs.getInt("Width", 750),
					prefs.getInt("Height", 700)));
		} else
			setPreferredSize(new Dimension(750,700));
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		

	}
	
	
	
	
	/**
	 * Prepare and start the game loop.
	 *
	 */
	public abstract void startLoop();
	
	public void 	windowActivated(WindowEvent e) { }
	public void 	windowClosed(WindowEvent e) {
		if (Options.getRemember()) {
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			prefs.putInt("X", getX());
			prefs.putInt("Y", getY());
			prefs.putInt("Width", getWidth());
			prefs.putInt("Height", getHeight());
		}
		
		
	}
	public void 	windowClosing(WindowEvent e) { 
		// as if we pushed the quit button
		ActionEvent ae = new ActionEvent(this, 0, "Quit");
		actionPerformed(ae);
		
		
	}
	public void 	windowDeactivated(WindowEvent e) { }
	public void 	windowDeiconified(WindowEvent e) { }
	public void 	windowIconified(WindowEvent e) { }
	public void 	windowOpened(WindowEvent e) { }

}
