package net.kolls.railworld.opening;

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.kolls.railworld.Distance;
import net.kolls.railworld.Images;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.ScriptManager;

/**
 * Instance of JApplet for starting Rail World in play mode using an applet.
 * Allows user to select map and scripts based on applet parameters.
 * Starts a {@link PlayFrame} when the user clicks Play.
 * <p>
 * A list of map URLs must be passed in the
 * MAPS parameter, comma separated.
 * <p>
 * A good size for the applet is 450x300.
 * @author Steve Kollmansberger
 *
 */
public class Applet extends JApplet implements Runnable {
	
	private JPanel appletPic;
	private Thread tlr;
	private JLabel lbl;
	private ResourceLoader rl;
	
	/**
	 * Creates the applet.  This is called by the browser or applet viewer.
	 */
	@Override
	public void start() {
		appletPic = new JPanel() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(Images.applet, (getWidth()-Images.applet.getWidth())/2, 0, this);
			}
		};
		appletPic.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		getContentPane().setBackground(new Color(0, 200, 0));
		final JLabel loading = new JLabel("Loading...");
		loading.setFont(loading.getFont().deriveFont(42f));
		loading.setHorizontalAlignment(SwingConstants.CENTER);
		
		
		lbl = new JLabel("Loading, please wait...");
		
		lbl.setOpaque(false);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lbl, BorderLayout.SOUTH);
		
		
		getContentPane().add(loading, BorderLayout.NORTH);
		rl = new ResourceLoader(this);
		rl.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				if (Images.applet != null) {
					rl.removeChangeListener(this);
					getContentPane().remove(loading);
					appletPic.setPreferredSize(new Dimension(Images.applet.getWidth(), Images.applet.getHeight()));
					getContentPane().add(appletPic, BorderLayout.NORTH);
					validate();
					
				}
				
			}
			
		});
		rl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (rl.getValue() == rl.getMaximum()) {
					synchronized (tlr) { 
						tlr.notify();
					}
				}
				
			}
		});
		
		
		getContentPane().add(rl);
		
		
		validate();
		setVisible(true);
		
		tlr = new Thread(rl);
		tlr.start();
		
		Thread afl = new Thread(this);
		afl.start();
		
		
	}
	
	public void run() {


		try {
			synchronized (tlr) { tlr.wait(); }
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		
		
		getContentPane().removeAll();
		
		
		
		
		
		final JButton b = new JButton("START!");
		
		
		
		
		
		
		
		
		
		
		
		getContentPane().add(appletPic, BorderLayout.NORTH);
		
		JPanel selector = new JPanel();
		selector.setLayout(new BoxLayout(selector, BoxLayout.PAGE_AXIS));
		selector.setOpaque(false);
		
		
		final JTabbedPane freeOrMission = new JTabbedPane();
		
		
		// BEGIN FREE PLAY
		JPanel freePlaySelector = new JPanel();
		freePlaySelector.setLayout(new BoxLayout(freePlaySelector, BoxLayout.PAGE_AXIS));
	
		final JComboBox mapList = new JComboBox(rl.maps);
		freePlaySelector.add(makeRow(new JLabel("Select Map"), mapList));
	
		
		final ScriptPanel sp = new ScriptPanel();
				
		
		freePlaySelector.add(makeRow(new JLabel("Select Script(s) to Use"), sp));
	
		
		
		// END FREE PLAY
		freeOrMission.addTab("Free Play", freePlaySelector);
		
		final MissionPanel mp = new MissionPanel();
		freeOrMission.addTab("Mission", mp);
		
		selector.add(freeOrMission);
		selector.add(Box.createRigidArea(new Dimension(50,10)));
		b.setAlignmentX(Component.CENTER_ALIGNMENT);
		selector.add(b);
		
		selector.add(Box.createRigidArea(new Dimension(50,10)));
		getContentPane().add(selector);
		
		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);
		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
		
		
		lbl.setText("Rail World Version " + Opening.version + " (Applet)");
		getContentPane().add(lbl, BorderLayout.SOUTH);
		
		validate();
		repaint();
		
		b.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				if (mapList.getSelectedIndex() == -1 && freeOrMission.getSelectedIndex() == 0) {
					b.setText("Select a Map First");
					return;
				}
				if (mp.getSelectedMission() == null && freeOrMission.getSelectedIndex() == 1) {
					b.setText("Select a Mission First");
					return;
				}
				
				freeOrMission.setEnabled(false);
				b.setEnabled(false);
        		b.setText("Downloading Map");
        		
        		sp.setEnabled(false);
        		mapList.setEnabled(false);
        		
				Thread t  = new Thread(new Runnable() {
		        	public void run() {
		        		// download the image
		        		MapLoader mymap = null;
		        		
		        		if (freeOrMission.getSelectedIndex() == 0)
		        			mymap = rl.maps[mapList.getSelectedIndex()];
		        		else {
		        			for (MapLoader ml : rl.maps)
		        				if (ml.getFilename().toLowerCase().equals(mp.getSelectedMission().rwmFilename().toLowerCase()))
		        					mymap = ml;
		        		}
		        		
		        		BufferedImage bi;
						try {
							bi = mymap.getImage();
						} catch (IOException ex) {
							ex.printStackTrace();
							return;
						}
		        		
		        		
		        		b.setText("Starting Game");
		        
		        		Distance.feetPerPixels = mymap.getMetaData().feetPerPixel;
		        		RailCanvas.zoom = mymap.getMetaData().zoom;

		        		ScriptManager scripts;
		        		
		        		if (freeOrMission.getSelectedIndex() == 0) {
		        			scripts = new ScriptManager();
		        			for (int i = 0; i < sp.getScripts().length; i++) {
		        				scripts.add( sp.getScripts()[i] );
		        			}
		        		} else {
		        			scripts = mp.getSelectedMission().createScriptManager();
		        		}
		        		
		        		final PlayFrame frame = new PlayFrame(mymap.getSegments(), bi, mymap.getMetaData(), scripts);
		        		
				
		        		for (int i = 0; i < scripts.size(); i++)
		        			scripts.get(i).init(frame);
		        		
				
						
						frame.setVisible(true);
						
						// loop will run until the window is closed
						frame.startLoop();	
						
						frame.dispose();
						
						// after the map has been run, it has been altered in two ways:
						// the map image has been loaded and remains in memory in
						// the mymap MapLoader
						// also, the segments have been modified to refer to signals
						// the signals themselves are not in the maploader,
						// but the segments in the array have been modified
						// so they are now broken
						// and cannot be reused
						try {
							rl.maps[mapList.getSelectedIndex()] = mymap.loadAgain();
						} catch (Exception ex) {
							// we loaded it fine the first time!
							ex.printStackTrace();
						}
		        		
						b.setEnabled(true);
						b.setText("START!");
						
						sp.setEnabled(true);
		        		mapList.setEnabled(true);
		        		freeOrMission.setEnabled(true);
						
						
		        	}
			        
		        });
		        t.start();
				
				
				
			}
			
		});
		
	}
	
	private JPanel makeRow(JComponent left, JComponent right) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		
		p.add(Box.createRigidArea(new Dimension(50,10)));
		
		left.setAlignmentY(Component.TOP_ALIGNMENT);
		p.add(left);
		
		p.add(Box.createHorizontalGlue());
		
		right.setAlignmentY(Component.TOP_ALIGNMENT);
		p.add(right);
		
		p.add(Box.createRigidArea(new Dimension(50,10)));
		
		return p;
		
	}
}

