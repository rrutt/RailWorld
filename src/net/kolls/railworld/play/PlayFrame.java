package net.kolls.railworld.play;

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
import net.kolls.railworld.car.Caboose;
import net.kolls.railworld.io.MetaData;
import net.kolls.railworld.io.RWGFilter;
import net.kolls.railworld.io.RWGWriter;
import net.kolls.railworld.io.TRNFilter;
import net.kolls.railworld.io.TRNReader;
import net.kolls.railworld.io.TRNWriter;
import net.kolls.railworld.opening.MetaDataPanel;
import net.kolls.railworld.opening.Options;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.LUSegment;
import net.kolls.railworld.segment.Signal;
import net.kolls.railworld.segment.TrackSegment;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.prefs.Preferences;


/**
 * A window for playing a Rail World game.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class PlayFrame extends RailFrame {

	/**
	 * The canvas in use.  The canvas maintains the active train list and segments.
	 * 
	 */
	public PlayCanvas jdb;

	/**
	 * The game loop that manages action on this frame.
	 */
	public GameLoop gl;
	private MetaData mmd;
	private JToggleButton pause, cth;
	@SuppressWarnings("rawtypes")
	private JList dataList;
	private JButton addb, impb, expb, delb, popb, mcb;
	
	/**
	 * Immediately quits the game.  If a score is specified,
	 * and a mission is in progress, this score may be recorded
	 * as a high score.
	 * 
	 * @param score Optional, the player's final score
	 */
	public void stop(Integer score) {
		jdb.trains.select(null, null); // terminate any sound going on if train selected
		
		Sounds.allStop();
		gl.stop();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			
				jdb.stop();	
				
			}
		});
	}
	
	/**
	 * Removes the populate button from  the toolbar, as well as
	 * the four train control buttons on the bottom right.
	 * If a value for the mission control listener is provided,
	 * the mission control button will be linked to it
	 * and shown in the bottom right.
	 * 
	 * @param mcact Optional, a listener for mission control button
	 */
	public void hideTrainButtons(ActionListener mcact) {
		addb.setVisible(false);
		delb.setVisible(false);
		impb.setVisible(false);
		expb.setVisible(false);
		popb.setVisible(false);
		
		
		if (mcact != null) {
			mcb.setVisible(true);
			mcb.addActionListener(mcact);
		}
		
	}
	
	private void exportt(Train t) {
	
		JFileChooser jfc = new JFileChooser();
		
		
		// assume same directory as our files are in
		
		jfc.setCurrentDirectory(mmd.ourFile);
		jfc.addChoosableFileFilter(new TRNFilter());
		
		// generate file name
		String fn = "Y1089-01E";
		int eng = 0;
		int fr = 0;
		int cab = 0;
		for (int i = 0; i < t.array().length; i++) {
			if (t.array()[i].isEngine()) eng++;
			else if (t.array()[i] instanceof Caboose) cab++;
			else fr++;
		}
		
		// Y1089-01E02F023C0-782-Anyone-MyFirstTrain
		// yard number, exit, # engines, # freight, # cab, ??? #, from, to
		
		DecimalFormat df = new DecimalFormat("00");

		fn += df.format(eng);
		fn += "F";
		df.applyPattern("000");
		fn += df.format(fr);
		fn += "C";
		df.applyPattern("0");
		fn += df.format(cab);
		fn +="-000-Anyone-RailWorld.trn";
		
		jfc.setSelectedFile(new File(fn));
		int rv = jfc.showSaveDialog(this);
		if (rv != JFileChooser.APPROVE_OPTION) return;
		File file = jfc.getSelectedFile();
		
		if (file.getName().toUpperCase().endsWith(".TRN") == false) {
			// add extension if need
			file = new File(file.getParent(), file.getName() + ".trn");
			
		}
        if (file.exists ()) {
            int response = JOptionPane.showConfirmDialog (this,
              "Overwrite existing file?","Confirm Overwrite",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);
            if (response != JOptionPane.YES_OPTION) return;
        }
		
		try {
			TRNWriter.write(file, t);
			 	
        } catch (Exception ex) {
       	 	JOptionPane.showMessageDialog(this, "There was an error exporting the train, reason: "+ex.getMessage(), "Error Importing", JOptionPane.ERROR_MESSAGE);
        }
        
        
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Train importt() {
		JFileChooser jfc = new JFileChooser();
		
		
		// as in train creator, find all EE Segments so we can allow the user to choose
		Vector<EESegment> ees = new Vector<EESegment>();
		
		
		for (int i = 0; i < jdb.la.length; i++) {
			if (jdb.la[i] instanceof EESegment) 
				ees.add((EESegment)jdb.la[i]);

		}
		
		JPanel hp = new JPanel();
		hp.setLayout(new BoxLayout(hp, BoxLayout.Y_AXIS));
		hp.add(new JLabel("Entering At"));
		hp.add(Box.createHorizontalGlue());
		JComboBox enters = new JComboBox();
		Iterator<EESegment> ei = ees.iterator();
		while (ei.hasNext()) {
			EESegment ee = ei.next();
			enters.addItem(ee.label);
			
		}
		hp.add(enters);
		hp.add(Box.createVerticalGlue());
		
		
		
		// assume same directory as our files are in
		
		jfc.setCurrentDirectory(mmd.ourFile);
		jfc.addChoosableFileFilter(new TRNFilter());
		jfc.setAccessory(hp);
		int rv = jfc.showOpenDialog(this);
		if (rv != JFileChooser.APPROVE_OPTION) return null;
		File file = jfc.getSelectedFile();
		Train t = null;
		
		try {
       	 	t = TRNReader.read(file);
       	 	EESegment ee = ees.get(enters.getSelectedIndex());
 		
       	 	t.pos.r = ee;
       	 	t.pos.orig = ee.HES;
       	 	t.followMeOnce = true; 	
        } catch (Exception ex) {
       	 	JOptionPane.showMessageDialog(this, "There was an error importing the train, reason: "+ex.getMessage(), "Error Importing", JOptionPane.ERROR_MESSAGE);
        }
        
        return t;
		
		
	}
	
	/**
	 * Add a train.  The train adding occurs in the game loop.
	 * 
	 * @param t The train to add
	 * @param select Whether or not to select the train
	 * @see GameLoop#runInLoop(Runnable)
	 */
	public void addTrain(final Train t, final boolean select) {
		gl.runInLoop(new Runnable() {
			public void run() {
				jdb.trains.add(t);
				if (select) {
					// the thing is, the add has to notify the listbox
					// so this item won't be available for selection
					// until the listbox actually is updated
					// which occurs in the AWT event thread
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							jdb.trains.select(t, null);		
						}
					});
				}
					
					
				
			}
		});
	}
	
	/**
	 * Remove a train.  The train is removed at the beginning of the game loop.
	 * 
	 * @param t The train to remove.
	 * @see GameLoop#runInLoop(Runnable)
	 */
	public void removeTrain(final Train t) {
		gl.runInLoop(new Runnable() {
			public void run() {
				jdb.trains.remove(t);
				
			}
		});
	}
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addWidgets() {
		
		JPanel tch = new JPanel();
		// if no layout, components won't auto size!
		tch.setLayout(new BoxLayout(tch, BoxLayout.Y_AXIS));
		
		getContentPane().add(jdb); // defaults to center

		rightPanel.add(tch);
		
		dataList = new JList();
		
		jdb.trains = new Trains(tch, dataList);
		dataList.setModel(jdb.trains);
		
		FontMetrics metrics = dataList.getFontMetrics(dataList.getFont());
		dataList.setCellRenderer(new TrainListCellRenderer(this, metrics, 175));
		dataList.setFixedCellHeight(32);
		dataList.setFixedCellWidth(175);
		
		
		dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		JScrollPane scrollPane = new JScrollPane(dataList);
		scrollPane.setPreferredSize(new Dimension(200,100));
		
		JPanel bottom = new JPanel();
		JPanel btaddrem = new JPanel();
	
		btaddrem.setLayout(new BoxLayout(btaddrem, BoxLayout.X_AXIS));
		
		addb = new JButton(Images.newt);
		addb.setToolTipText("Create Train");
		addb.setActionCommand("CreateTrain");
		addb.addActionListener(this);
		
		btaddrem.setPreferredSize(new Dimension(200,48));
		btaddrem.add(Box.createRigidArea(new Dimension(5,5)));
		btaddrem.add(Box.createHorizontalGlue());
		
		addb.setPreferredSize(new Dimension(47,40));
		
		
		btaddrem.add(addb);
		
		
		impb = new JButton(Images.importt);
		impb.setToolTipText("Import Train from Yard Duty");
		impb.setActionCommand("ImportTrain");
		impb.addActionListener(this);
		
		impb.setPreferredSize(new Dimension(47,40));
		btaddrem.add(impb);
		
		
		expb = new JButton(Images.exportt);
		expb.setToolTipText("Export Selected Train to Yard Duty");
		expb.setEnabled(false);
		expb.setActionCommand("ExportTrain");
		expb.addActionListener(this);
		
		
		expb.setPreferredSize(new Dimension(47,40));
		btaddrem.add(expb);
		
		
		delb = new JButton(Images.delete);
		delb.setToolTipText("Delete Selected Train");
		delb.setEnabled(false);
		delb.setActionCommand("DeleteTrain");
		delb.addActionListener(this);
		
		delb.setPreferredSize(new Dimension(47,40));
		btaddrem.add(delb);
		
		mcb = new JButton("Mission Control");
		mcb.setToolTipText("Show Mission Control Window");
		mcb.setVisible(false);
		mcb.setActionCommand("MissionControl");
		
		btaddrem.add(mcb);
		btaddrem.add(Box.createHorizontalGlue());
		btaddrem.add(Box.createRigidArea(new Dimension(5,5)));
		
		
		bottom.setLayout(new BorderLayout());
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(5,5));
		bottom.add(jp, BorderLayout.WEST);
		jp = new JPanel();
		jp.setPreferredSize(new Dimension(5,5));
		bottom.add(jp, BorderLayout.EAST);
		
		bottom.add(scrollPane, BorderLayout.CENTER);
		bottom.add(btaddrem, BorderLayout.SOUTH);
		
		
		
		leftPanel.add(bottom, BorderLayout.SOUTH);
		
		
		dataList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;
				
				
				int i = ((JList)e.getSource()).getSelectedIndex();
				if (i == -1) {
					expb.setEnabled(false);
					delb.setEnabled(false);
					return;
				}
				expb.setEnabled(true);
				delb.setEnabled(true);
				Train t = jdb.trains.getSelectedTrain();
				Train nst = jdb.trains.get(i);
				
				if (t != nst) {
					jdb.trains.select(nst, null);
					nst.followMeOnce = true;
				}
				
			}
			
		});

		pack();
		
		



		

	}

	
	
	public void actionPerformed(final ActionEvent e) {
		
				
			
		// check with scripts; cancel if necessary
		if (jdb.sm.playFrameAction(e.getActionCommand())) return;
		
		if (e.getActionCommand().equals("DeleteTrain")) {
			if (dataList.getSelectedIndex() == -1) return;
			removeTrain((Train)dataList.getSelectedValue());
			//jdb.trains.remove(dataList.getSelectedIndex());
			jdb.trains.select(null, null);
		}
		
		if (e.getActionCommand().equals("ExportTrain")) {
			Train t = jdb.trains.getSelectedTrain();
			if (t != null) exportt(t);
		}
		
		if (e.getActionCommand().equals("ImportTrain")) {
			Train t = importt();
			if (t != null) {
				t.getController().setTrainActionScriptNotify(jdb.sm);
				addTrain(t, true);
			}
		}
		
		if (e.getActionCommand().equals("CreateTrain")) {
			JFrame j = new TrainCreator(jdb.la, this, jdb.sm);
			j.setVisible(true);
		}
		
		if (e.getActionCommand().equals("Populate")) {
			RailSegment[] la = jdb.la;
			Random r = new Random();
			Train t;
			double lnf;
			ArrayList<Car> vc;
			
			for (int i = 0; i < la.length; i++) {
				// consider only empty LUsegments
				if (la[i] instanceof LUSegment && la[i].trains().isEmpty()) {
					Car[] ca = ((LUSegment)la[i]).lu();
					lnf = 15; // start 15 feet into the segment
					vc = new ArrayList<Car>();
					
					do {
						int x = r.nextInt(ca.length);
						try {
							Car c = (Car)ca[x].newInstance();
							// make our car the opposite of what is accepted
							// so it is "done" and ready to leave
							if (ca[x].loaded()) c.unload(); else c.load();
							
						
							if (lnf + c.length().feet() + Car.DIST_BETWEEN_CARS.feet() < la[i].length().feet()) {
								lnf += c.length().feet() + Car.DIST_BETWEEN_CARS.feet();
								vc.add(c); 
							} else break;
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						// don't exceed the length of the segment, but don't always fill it either
					} while (r.nextDouble() < 0.75 );
				
					if (vc.size() == 0) continue;
					t = new Train(vc.toArray(new Car[0]));
					RailSegment rs = ((LUSegment)la[i]).getDest(TrackSegment.POINT_BEGIN);
					
					if (rs == null)  
						rs = ((LUSegment)la[i]).getDest(TrackSegment.POINT_END);
					double per = lnf / la[i].length().feet();
					t.pos = new CLoc(la[i], rs, per);
					t.setBrake(true);
					t.getController().setTrainActionScriptNotify(jdb.sm);
					addTrain(t, false);

					
				}
			}
			
		}
		if (e.getActionCommand().equals("Pause")) {
			JToggleButton tb = (JToggleButton)e.getSource();
			gl.paused = tb.isSelected();
			if (gl.paused)
				Sounds.allFreeze();	
			else
				Sounds.allUnfreeze();
			
			
		}
		
		if (e.getActionCommand().equals("Information")) {
			mmd.track = jdb.trackLength();
			final MetaDataPanel mdp = new MetaDataPanel(mmd, jdb.sm.toArray(new Script[0]), false);
			final JDialog d = new JDialog(this, "Map Information", true);
			d.setResizable(false);
			
			d.add(mdp);
			
			
			
			
			JPanel mp = new JPanel();
			JButton b = new JButton("OK");
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
		

		if (e.getActionCommand().equals("ContextHelp")) {
			jdb.showContextInfo = cth.isSelected();
			
			if (Options.getRemember()) {
				Preferences prefs = Preferences.userNodeForPackage(Options.class);
				prefs.putBoolean("ContextHelp", cth.isSelected());
			}
		}
		
		if (e.getActionCommand().equals("Save")) {
			
			if (mmd.ourFile == null) {
				JOptionPane.showMessageDialog(this, "Can't save games when running as applet", "Can't Save", JOptionPane.WARNING_MESSAGE);
				return;
			}
			JFileChooser jfc = new JFileChooser();
			
			// we want to save in the same directory as the map file
			jfc.setCurrentDirectory(mmd.ourFile);
			jfc.addChoosableFileFilter(new RWGFilter());
			int rv = jfc.showSaveDialog(this);
			if (rv != JFileChooser.APPROVE_OPTION) return;
			File file = jfc.getSelectedFile();
			if (file.getName().toUpperCase().endsWith(".RWG") == false) {
				// add extension if need
				file = new File(file.getParent(), file.getName() + ".rwg");
				
			}
            if (file.exists ()) {
                int response = JOptionPane.showConfirmDialog (this,
                  "Overwrite existing file?","Confirm Overwrite",
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) return;
            }
            
	         mmd.centerX = (int)jdb.getCenterPoint().getX();
	         mmd.centerY = (int)jdb.getCenterPoint().getY();
	         mmd.feetPerPixel = Distance.feetPerPixels;
	         mmd.zoom = RailCanvas.zoom;
	         mmd.elapsed = gl.elapsed;
	            
	            
	            
	            
	         try {
	        	 RWGWriter.write(jdb.la, jdb.trains, jdb.sm, mmd, file);
	         } catch (Exception ex) {
	        	 JOptionPane.showMessageDialog(this, "There was an error saving the file, reason: "+ex.getMessage(), "Error Saving", JOptionPane.ERROR_MESSAGE);
	        	 ex.printStackTrace();
	         }



            
		}
		
		if (e.getActionCommand().equals("Quit")) {
			if (mmd.ourFile != null && jdb.trains.size() > 0) {

				final int response = YesNoCancel.showDialog(this, "Do you wish to save the game before quitting?", 
						"Quit and/or Save", "Save and Quit", "Quit and Don't Save", "Don't Quit");
	        	if (response != JOptionPane.YES_OPTION && response != JOptionPane.NO_OPTION) return;
	        
	    		if (response == JOptionPane.YES_OPTION) {
					actionPerformed(new ActionEvent(this, 0, "Save"));
				
				}
	    	
			}
			
			stop(null);
			
		}
		
		if (e.getActionCommand().equals("ZoomIn")) {
			Point2D p = jdb.getCenterPoint();
			RailCanvas.zoom *= 1.5;
			
			jdb.recomp();
			jdb.submitCenterCoords((int)p.getX(), (int)p.getY());
			
		}
		
		if (e.getActionCommand().equals("ZoomOut")) {
			Point2D p = jdb.getCenterPoint();
			RailCanvas.zoom /= 1.5;
			
			jdb.recomp();
			jdb.submitCenterCoords((int)p.getX(), (int)p.getY());
		}
		
		
	}
	
	/**
	 * Create a new play frame.
	 * 
	 * @param lines Segments to use
	 * @param source Source image
	 * @param md Metadata for map
	 * @param sm Scriptmanager with scripts loaded
	 */
	public PlayFrame(RailSegment[] lines, BufferedImage source, MetaData md, ScriptManager sm) {
		
		super(lines, source, "Game");
		
		
		
		// add signals
		lines = Signal.createSignals(lines);
		lines = sm.modifySegments(lines);
		

		mmd = md;
		
		setTitle("Rail World - " + md.title);
		
		jdb = new PlayCanvas(source, lines, miniv);
		jdb.sm = sm;
		
		addWidgets();
		
		// when someone uses the mini viewer, disable any following
		miniv.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				Train t = jdb.trains.getSelectedTrain();
				
				if (t != null) t.followMe = false;
				
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			
		});
		
		

		

		JButton b;
		b = new JButton(Images.information);
		b.setActionCommand("Information");
		b.setToolTipText("Map Information");
		b.addActionListener(this);
		toolBar.add(b);
		
		JToggleButton tb;
		tb = new JToggleButton(Images.pause);
		tb.setActionCommand("Pause");
		tb.setToolTipText("Pause Game");
		tb.addActionListener(this);
		toolBar.add(tb);
		pause = tb;
		
		
		tb = new JToggleButton(Images.contexthelp);
		tb.setActionCommand("ContextHelp");
		tb.setToolTipText("Show In-game Tooltips");
		tb.addActionListener(this);
		toolBar.add(tb);
		
		if (Options.getRemember()) {
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			tb.setSelected(prefs.getBoolean("ContextHelp", true));
		} else
			tb.setSelected(true);
		cth = tb;
		jdb.showContextInfo = cth.isSelected();
		
		
		
		
		toolBar.addSeparator();
		
		popb = new JButton(Images.populate);
		popb.setActionCommand("Populate");
		popb.setToolTipText("Add cars to empty sidings");
		popb.addActionListener(this);
		toolBar.add(popb);
		
		jdb.recomp();
		jdb.submitCenterCoords(mmd.centerX, mmd.centerY);
		
		// prepare the game loop
		
		final int w = (int)(jdb.orig_src.getWidth()*Distance.getDefaultZoom());
		final int h = (int)(jdb.orig_src.getHeight()*Distance.getDefaultZoom());
		tlcr = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		gl = new GameLoop(jdb) {
			private void handleRA(RailAccident ra) {
				
				if (Options.getAccidents()) {
					paused = true;
					pause.setSelected(true);
					Sounds.allFreeze();
					Sounds.wreck.unfreeze();
					Sounds.wreck.play();
					jdb.submitCenterCoords((int)ra.pos.getX(), (int)ra.pos.getY());
					RailAccidentFrame raf = new RailAccidentFrame(jdb, ra, mmd.title);
					
					raf.setVisible(true);
					
					if (jdb.sm.mission != null)
						jdb.sm.mission.railAccident(ra);
					
				} else {
					System.out.println(ra.title());
					
					ra.t1.setThrottle(0);
					ra.t1.setBrake(true);
					ra.t1.setVel(0);
					
					if (ra.t2 != null) {
						ra.t2.setThrottle(0);
						ra.t2.setBrake(true);
						ra.t2.setVel(0);
					}
				}
				
				
				
			}
			
			@Override
			protected void prePaint() {
				try {
					jdb.trains.step();
					
					
					
					
				} catch (RailAccident ra) {
					handleRA(ra);

					
				}
			}
			@Override
			protected void run() {
				
				
				Train t;
				Iterator<Train> i = jdb.trains.iterator();
				
				while (i.hasNext()) {
					
					try {
						// get train item inside try block due to concurrent modification if train added/deleted
						t = i.next();
						
						
						if (t.getController().process())
							SwingUtilities.invokeAndWait(t.getController()); // avoid update flicker
						
						
						
						jdb.sm.trainStep(t);
						
					}
					catch (InterruptedException ex) { }
					catch (InvocationTargetException ex) { }
					catch (ConcurrentModificationException e) {
						e.printStackTrace();
						break; 
					} // must abandon this loop if modified
					catch (RailAccident ra) {
						handleRA(ra);
					}
					//catch (Exception e) { e.printStackTrace(); }

					
				}
		
				/* refresh the image used for train list cell renderer */
				
				Graphics2D g2 = tlcr.createGraphics();
				jdb.draw(g2, new Point2D.Double(w/2, h/2), w, h, Distance.getDefaultZoom(), false);
				g2.dispose();
				
				jdb.trains.refreshList();
				
			}
			
			
		};
		

	}

	
	/**
	 * A default zoom low detail version of the map, used in particular
	 * for the train list. 
	 */
	public BufferedImage tlcr;
	
	@Override
	public void startLoop() {
		
		gl.gameLoop();
	}
	
}
