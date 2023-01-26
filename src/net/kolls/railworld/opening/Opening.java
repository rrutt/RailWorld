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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.kolls.railworld.Distance;
import net.kolls.railworld.Factories;
import net.kolls.railworld.Images;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailFrame;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.YesNoCancel;
import net.kolls.railworld.edit.EditFrame;
import net.kolls.railworld.io.ImageFilter;
import net.kolls.railworld.io.MetaData;
import net.kolls.railworld.io.RWGFilter;
import net.kolls.railworld.io.RWGReader;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.ScriptManager;

/**
 * Displays an opening window with graphic, allowing the user to choose whether to start a game, load a game,
 * or enter the edit module.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class Opening extends JFrame implements ActionListener {
	
	/**
	 * Current Rail World version
	 */
	public static final String version = "1.0.0+20230126";
	
	private JFrame f;
	
	private void run(final RailFrame frame) {
		
//		 if we just fire it up in this code, it will be running in the event
		// loop thread, and block all events!
		// so we must spawn it into a different thread
        Thread t  = new Thread(new Runnable() {
        	public void run() {
        		
				frame.setVisible(true);
				
				// loop will run until the window is closed
				try {
					frame.startLoop();
				} catch (Throwable ex) {
        			JOptionPane.showMessageDialog(f, "An error occured while running, and the game has been stopped.  Reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        			ex.printStackTrace();
    				
        			
        		}
				
				frame.dispose();
				setVisible(true);
				
        	}
	        
        });
        t.start();
	}
	
	
	/**
	 * Ladies and gentlemen, start your engines!  The main entry point
	 * for Rail World in application mode.
	 * 
	 * @param args None recognized
	 * 
	 */
	public static void main(String args[])  {
		
		final SplashScreen splash = SplashScreen.getSplashScreen();
		
		final ResourceLoader rl = new ResourceLoader(null);
		rl.setSize(420, 15);
		
		rl.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				if (splash == null) return;
				final Graphics2D splashg2 = splash.createGraphics();
				
				splashg2.translate(5, 90);
				rl.paint(splashg2);
				splashg2.dispose();		
				splash.update();
				
			}
			
		});
		
		rl.run();
		
		
		Opening o = new Opening();
		o.setVisible(true);
	}
		
	private void addWidgets(JPanel cp) {
		
		 JButton ng, lg, nm, lm, q, opts;
	     ng = new JButton("New Game");
	     ng.setActionCommand("NewGame");
	     ng.addActionListener(this);
				
	        
	     lg = new JButton("Load Game");
	     lg.setActionCommand("LoadGame");
	     lg.addActionListener(this);
	        
	     nm = new JButton("New Map");
	     nm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Factories.init();
				setVisible(false);
				MetaDataPanel mdp = new MetaDataPanel();
				final JFileChooser jfc = new JFileChooser();
				jfc.addChoosableFileFilter(new ImageFilter());
				jfc.setAccessory(mdp);
					
				setDirectoryFromPrefs(jfc);
				
				int rv = jfc.showOpenDialog(f);
					
				if (rv == JFileChooser.APPROVE_OPTION) {
						
						
					File file = jfc.getSelectedFile();
					setPrefsFromDirectory(file.getParentFile());
					BufferedImage bi;
					try {
						bi = ImageIO.read(file);	
					} catch (IOException ioe) {
						bi = null;
							
					}
					if (bi == null) {
						JOptionPane.showMessageDialog(f, "Unable to load requested image", "Error", JOptionPane.ERROR_MESSAGE);
						f.setVisible(true);
						return;
					}
						
						
				                        
				        
				    MetaData md = mdp.getMD();
				    md.imgfile = file.getName();
				    md.ourFile = file.getParentFile(); // the directory, gives a default save point
				        

				    Distance.feetPerPixels = md.feetPerPixel;
				    // The "ideal size" is 3 ft/px.  We want to present the default zoom at that size.

				    RailCanvas.zoom = Distance.getDefaultZoom(); 
						
					final BufferedImage fbi = bi;
					final MetaData fmd = md;
						
					EditFrame frame;
		        		
		        	try {
		        		frame = new EditFrame(new RailSegment[0], fbi, fmd);
		        	} catch (Throwable ex) {
		        		JOptionPane.showMessageDialog(f, "Unable to start the editor, reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		        		ex.printStackTrace();
		        		f.setVisible(true);
		    			return;
		        	}
		        		
		        	run(frame);
				  } else
					f.setVisible(true);
				}
	        });
	        nm.setAlignmentX(Component.RIGHT_ALIGNMENT);

	        lm = new JButton("Edit Map");
	        lm.setAlignmentX(Component.RIGHT_ALIGNMENT);
	        lm.addActionListener(this);
	        lm.setActionCommand("EditMap");
	        
	        q = new JButton("Quit");
	        q.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);	
				}
	        });
	        q.setAlignmentX(Component.RIGHT_ALIGNMENT);
	        
	        opts = new JButton("Options");
	        opts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Options ops = new Options();
					ops.setVisible(true);
				}
	        });
	        
	        
	        
	        
	       cp.add(createVertPanel(new Component[] {ng,lg,opts}), BorderLayout.WEST);
	        
	        
	       cp.add(createVertPanel(new Component[] {nm,lm,q}), BorderLayout.EAST);
	        
	        
	       
	       JPanel verp = new JPanel();
	        
	        
	       verp.setLayout(new BoxLayout(verp, BoxLayout.LINE_AXIS));
	       verp.setOpaque(false);

	       verp.add(new JLabel("Rail World Version "+version));
	       verp.add(Box.createHorizontalGlue());
	       verp.add(new JLabel("http://www.kolls.net/railworld"));
	        
	        
	       cp.add(verp, BorderLayout.SOUTH);
	}
	
	private JPanel createVertPanel(Component[] comps) {
		JPanel lp = new JPanel();
        lp.setLayout(new BoxLayout(lp, BoxLayout.PAGE_AXIS));
        
        lp.setOpaque(false);
        
        lp.add(Box.createRigidArea(new Dimension(comps[0].getWidth()+20, 150)));
        
        for (Component c : comps) {
        
        	lp.add(c);
        	lp.add(Box.createVerticalGlue());
        }
        return lp;
        
	}
	
	/**
	 * Construct an opening frame.
	 * 
	 */
	public Opening() {
		super();
		

		f = this;
	
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        
	    setResizable(false);
	    setLocationRelativeTo(null);
	    setIconImage(Images.frameIcon);
	    setTitle("Rail World");
	    
	    // Setup background image    
	    final JPanel cp = new JPanel();
	    cp.setPreferredSize(new Dimension(500,300));
	        
	    final Border bkgrnd = new CenteredBackgroundBorder(Images.opening);
	    cp.setBorder(bkgrnd);
	        
	        
	    cp.setLayout(new BorderLayout());
	    addWidgets(cp);
	   
	        
	    getContentPane().add(cp);
	        
	    pack();
	     	
	 }



	private File setDirectoryFromPrefs(JFileChooser jfc) {
		File fl = null;
		if (Options.getRemember()) {
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			fl = new File(prefs.get("Directory", "."));
			if (jfc != null) jfc.setCurrentDirectory(fl);
		}
		return fl;
	}
	
	private void setPrefsFromDirectory(File fl) {
		if (Options.getRemember()) {
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			prefs.put("Directory", fl.toString());
		}
	}


	public void actionPerformed(ActionEvent e) {
		ScriptManager scripts = new ScriptManager();
		
		// the resourceloader does this, but the factories
		// are extensible and may be changed by scripts
		// so reset them everytime
		Factories.init();
		
		setVisible(false);
		MapLoader mi = null;
		
		File rwm = null;
		File rwg = null;
		if (e.getActionCommand().equals("LoadGame")) {
			
			final JFileChooser jfc = new JFileChooser();
			jfc.addChoosableFileFilter(new RWGFilter());
			setDirectoryFromPrefs(jfc);
						
			// 1.  get the rwg file
			int rv = jfc.showOpenDialog(this);
		
			if (rv == JFileChooser.APPROVE_OPTION) {
			
				// 2.  read the rwm file from the rwg file
				rwg = jfc.getSelectedFile();
			
				// load scripts in that directory
				scripts = ScriptManager.allWithSupportingScripts();
			
				String rwmname = "";
				try {
					rwmname = RWGReader.readForRWM(rwg);	
					scripts = RWGReader.readForScripts(rwg, scripts);
				} catch (Throwable ex) {
					JOptionPane.showMessageDialog(this, "Unable to load the game. Error reading RWG file. reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					setVisible(true);
					return;
				}
				// 3. look in the same directory for the rwm file
				// load it and get its associated image and segments
				rwm = new File(rwg.getParent(), rwmname);
				
				
				setPrefsFromDirectory(rwg.getParentFile());
			} else {
//				 user cancel
				setVisible(true);
				return; 
			}
			
		}
		
		// 4. load the map
		try {
			if (e.getActionCommand().equals("EditMap")) scripts = null;

			boolean freeplay = true;
			File dir = setDirectoryFromPrefs(null);
			
			if (e.getActionCommand().equals("NewGame")) {
				int res = YesNoCancel.showDialog(this, "Do you want to start a mission or free play?", "Game type select", "Free play", "Mission", "Cancel");
				if (res == JOptionPane.CANCEL_OPTION) {
					setVisible(true);
					return;
				}
				
				freeplay = JOptionPane.YES_OPTION == res;
			}
			
			if (e.getActionCommand().equals("NewGame") && !freeplay) {
				MissionDialog mp = new MissionDialog(dir);
				mp.setVisible(true);
				
				if (mp.getSelectedMission() == null) {
					// user cancel
					setVisible(true);
					return;
				}

				mi = MapLoader.loadFromFile(new File(mp.getDirectory(), mp.getSelectedMission().rwmFilename()));
				scripts = mp.getSelectedMission().createScriptManager();
				
				setPrefsFromDirectory(mi.getMetaData().ourFile.getParentFile());
				
			}
			
			if ( (e.getActionCommand().equals("NewGame") && freeplay)
					|| e.getActionCommand().equals("EditMap")) {
				
				
				mi = MapLoader.loadFromUserPrompt(scripts, dir);
				if (mi == null) {
					// user cancel
					setVisible(true);
					return; 
				}
				setPrefsFromDirectory(mi.getMetaData().ourFile.getParentFile());
			}
			if (e.getActionCommand().equals("LoadGame"))
				mi = MapLoader.loadFromFile(rwm);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Unable to load requested map, reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			mi = null;
		}	
		
		if (mi == null) {
			setVisible(true);
			return;
		}
		
		// 5. set the scale and zoom
		RailCanvas.zoom = mi.getMetaData().zoom;
		Distance.feetPerPixels = mi.getMetaData().feetPerPixel;
		
		// 6. create the frame
		RailFrame frame = null;
		try {
			if (e.getActionCommand().equals("NewGame")) {
				frame = new PlayFrame(mi.getSegments(), mi.getImage(), mi.getMetaData(), scripts);
				scripts.init( (PlayFrame)frame);
			}
			if (e.getActionCommand().equals("LoadGame")) {
				frame = new PlayFrame(mi.getSegments(), mi.getImage(), mi.getMetaData(), scripts); 
   			
				// 7. load the rwg again, updating the canvas and trains
				RWGReader.read(rwg, (PlayFrame)frame);	
				
				
			}
			if (e.getActionCommand().equals("EditMap")) {
				frame = new EditFrame(mi.getSegments(), mi.getImage(), mi.getMetaData());
			}
		} catch (Throwable ex) {
			JOptionPane.showMessageDialog(this, "Unable to start the game, reason: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			frame = null;
		}
		
		
		// 8. run the frame
		if (frame != null)
			run(frame);
		else
			setVisible(true);
		
		mi = null;
		
	}
		

}

