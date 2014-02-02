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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.AccessControlException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.kolls.railworld.Images;
import net.kolls.railworld.Sounds;
import net.kolls.railworld.Sounds.SoundSystem;

/**
 * Displays a preferences dialog where the user can choose various
 * preferences for Rail World operation.  Generally not suitable for
 * applet use; assumed only for application use.
 * <p>
 * Wrapper methods allow safe access to default values from applets
 * so they can still run.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class Options extends JDialog implements ActionListener {
	
	private static Preferences prefs;
	private JComboBox<?> sound, fps;
	private JCheckBox remember, metric, antialias, accidents;
	
	/**
	 * 
	 * 
	 * @return Retrieve the user's selected sound system.  If none has been selected,
	 * return the applet sound system.
	 * @see SoundSystem
	 */
	public static Sounds.SoundSystem getSoundSystem() {
		if (prefs == null) return Sounds.SoundSystem.APPLET;
		
		return Sounds.SoundSystem.values()[prefs.getInt("Sound", Sounds.SoundSystem.APPLET.ordinal())];
		
	}
	
	/**
	 * 
	 * @return Does the user want to remember their settings? 
	 * Default to yes unless preferences are not available, then no.
	 */
	public static boolean getRemember() {
		if (prefs == null) return false;
		
		return prefs.getBoolean("Remember", true);
	}
	
	/**
	 * 
	 * @return Get desired FPS.  0 means "auto" (default).
	 */
	public static int getFPS() {
		if (prefs == null) return 0;
		
		return prefs.getInt("FPS", 0);
	
	}
	
	// TODO: implement
	/**
	 * Currently ignored!
	 * 
	 * @return Retrieve if the user wants to use metric measurements.
	 */
	public static boolean getMetric() {
		if (prefs == null) return false;
		
		return prefs.getBoolean("Metric", false);
	}
	
	/**
	 * 
	 * @return Retrieve if the user wants anti-aliased graphics. Defaults to false.
	 */
	public static boolean getAntialias() {
		if (prefs == null) return true;
		
		return prefs.getBoolean("Antialias", true);
	}
	
	/**
	 * 
	 * @return Retrieve if the rail accidents are enabled.  Defaults to true.
	 */
	public static boolean getAccidents() {
		if (prefs == null) return true;
		
		return prefs.getBoolean("Accidents", true);
	}
	
	
	/**
	 * Attempt to load user preferences.
	 */
	public static void loadPreferences() {
		try {
			prefs = Preferences.userNodeForPackage(Options.class);
		} catch (AccessControlException ex) {
			System.out.println("Unable to access preferences");
		}
	}
	
	
	/**
	 * Create an options window.
	 */
	public Options() {
		super();
		setModal(true);
		setTitle("Options");
		setIconImage(Images.frameIcon);
		
		addWidgets();
		pack();
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addWidgets() {
		setLayout(new BorderLayout());
		
		OptionPanel s = new OptionPanel();
		
		sound = new JComboBox(Sounds.SoundSystem.values());
		sound.setSelectedIndex(getSoundSystem().ordinal());
		sound.setToolTipText("If you are having freezes or performance problems, try various sound systems.");
		
		s.addLabeledControl("Sound System", sound);
		
		
		remember = new JCheckBox("Remember");
		remember.setSelected(getRemember());
		remember.setToolTipText("Remember window layout, scripts, and other options");
		
		s.addLabeledControl("Layout and Directories", remember);
		
		antialias = new JCheckBox("Antialiasing");
		antialias.setSelected(getAntialias());
		antialias.setToolTipText("Use smoother graphics (for fast machines only)");
		s.addLabeledControl("", antialias);

		accidents = new JCheckBox("Rail Accidents");
		accidents.setSelected(getAccidents());
		accidents.setToolTipText("Enable rail accidents");
		s.addLabeledControl("", accidents);

		
		fps = new JComboBox(new String[] {"Auto", "5", "10", "15", "20", "25"});
		fps.setEditable(true);
		fps.setSelectedItem(getFPS() == 0 ? "Auto" : getFPS());
		fps.setToolTipText("Frames per second the game should attempt to run.  Generally should be left on Auto");
		s.addLabeledControl("Target FPS", fps);
		
		
		metric = new JCheckBox("Metric");
		metric.setSelected(getMetric());
		metric.setToolTipText("Convert all American units into metric units");
		// TODO: implement
		/*
		s.addLabeledControl("Measurement Units", metric);
		*/
		
		getContentPane().add(s);
		
		JPanel closing = new JPanel();
		closing.setLayout(new BoxLayout(closing, BoxLayout.LINE_AXIS));
		
		
		
		JButton save, reset, cancel;
		
		save = new JButton("Save");
		save.setActionCommand("Save");
		save.addActionListener(this);
		
		reset = new JButton("Reset");
		reset.setActionCommand("Reset");
		reset.addActionListener(this);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		
		closing.add(Box.createRigidArea(new Dimension(20,20)));
		closing.add(save);
		closing.add(Box.createHorizontalGlue());
		closing.add(reset);
		closing.add(Box.createHorizontalGlue());
		closing.add(cancel);
		closing.add(Box.createRigidArea(new Dimension(20,20)));
		
		getContentPane().add(closing, BorderLayout.SOUTH);
		
		
		
	}

	public void actionPerformed(ActionEvent e) {
		Sounds.SoundSystem currss = getSoundSystem();
		if (e.getActionCommand().equals("Save")) {
			prefs.putInt("Sound", sound.getSelectedIndex());
			prefs.putBoolean("Remember", remember.isSelected());
			prefs.putBoolean("Antialias", antialias.isSelected());
			prefs.putBoolean("Accidents", accidents.isSelected());
			try {
				// combobox could contain a string representing an int
				// an int itself
				// or a string representing not an int
				// so to find the int if possible, we convert to string and parse again
				prefs.putInt("FPS", Integer.parseInt(fps.getSelectedItem().toString()));	
			} catch (Exception ex) {
				prefs.putInt("FPS", 0);
			}
			prefs.putBoolean("Metric", metric.isSelected());
			
			try {
				prefs.sync();
			} catch (BackingStoreException e1) {
				e1.printStackTrace();
			}
			
		}
		
		if (e.getActionCommand().equals("Reset")) {
			try {
				prefs.clear();
			} catch (BackingStoreException e1) {
				e1.printStackTrace();
			}
		}
		
		Sounds.SoundSystem newss = getSoundSystem();
		
		// reload sounds if system changed
		if (currss != newss) {
			Sounds s = new Sounds();
			s.loadSounds(null, getSoundSystem());
		}
		setVisible(false);
		
	}

}
