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


import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JProgressBar;

import net.kolls.railworld.sound.AppletSound;
import net.kolls.railworld.sound.JSSound;
import net.kolls.railworld.sound.NoSound;
import net.kolls.railworld.sound.StreamSound;

/**
 * Sound management class.  Manages all sounds.  
 * May load sounds using either Java Sound API or Applet sound system
 * 
 * @author Steve Kollmansberger
 *
 */
public class Sounds {
	public static Sound couple, uncouple, brake, switchd, crossing, wreck, horn;
	public static Sound[] engine;
	
	private static Vector<Sound> vs;
	
	/**
	 * Which sound system to use
	 *
	 */
	public enum SoundSystem {
		/**
		 * Java Sound API
		 */
		JS, 
		/**
		 * Applet sound API
		 */
		APPLET, 
		/**
		 * Stream (precision controlled) sound API
		 */
		STREAM, 
		/**
		 * No sound (mute)
		 */
		NONE
	}
	
	private SoundSystem myss;
	
	private Sound load(URL f, boolean restartOnPlay) {
		Sound s = null;
		
		for (int i = 0; i < 3; i++)  {
			switch (myss) {
			case APPLET:
				s = new AppletSound(f);
				break;
			case JS:
				s = new JSSound(f, restartOnPlay);
				break;
			case STREAM:
				s = new StreamSound(f, restartOnPlay);
				break;
			case NONE:
				s = new NoSound();
				break;
			}
			
			if (s.canPlay()) {
				vs.add(s);
				break;
			}
			if (i < 2) {
				System.out.println("Retrying...");
				try {
					Thread.sleep(100);
				} catch (Exception e) { }
			} else
				System.out.println("Giving up");
			
		}
		if (prog != null)
			prog.setValue(++val);
		return s;
		
		
		
	}
	
	
	
	private static void stop(Sound s) {
		while (s.playing())
			s.stop();
	}
	
	/**
	 * Stop all sounds.  This will execute a stop command continaully on each sound until the sound
	 * is no longer playing.  Thus, regardless of how many loops are in place, the sound will be stopped.
	 *
	 */
	public static void allStop() {
		
		Iterator<Sound> i = vs.iterator();
		
		while (i.hasNext())
			stop(i.next());
		
		
	}
	
	/**
	 * Freeze all sounds.
	 *
	 */
	public static void allFreeze() {
		
		Iterator<Sound> i = vs.iterator();
		
		while (i.hasNext())
			i.next().freeze();
	}
	
	/**
	 * Unfreeze all sounds.
	 *
	 */
	public static void allUnfreeze() {
		
		Iterator<Sound> i = vs.iterator();
		
		while (i.hasNext())
			i.next().unfreeze();
	}

	private int val;
	private JProgressBar prog;
	
	/**
	 * Load all sounds.  
	 * @param progress The progress bar to update (may be null)
	 * @param ss The sound system to use when loading sounds.
	 * @see SoundSystem
	 * 
	 */
	public void loadSounds(JProgressBar progress, SoundSystem ss) {
		
		
		prog = progress;
		if (progress != null) val = progress.getValue();
		
		vs = new Vector<Sound>();
		
		myss = ss;
		
		couple = load(getClass().getResource("/net/kolls/railworld/sound/couple.wav"), true);
		uncouple = load(getClass().getResource("/net/kolls/railworld/sound/uncouple.wav"), true);
		brake = load(getClass().getResource("/net/kolls/railworld/sound/brake.wav"), true);
		switchd = load(getClass().getResource("/net/kolls/railworld/sound/switch.wav"), true);
		crossing = load(getClass().getResource("/net/kolls/railworld/sound/crossing.wav"), false);
		wreck = load(getClass().getResource("/net/kolls/railworld/sound/wreck.wav"), false);
		
		
		
		
		engine = new Sound[9];
		
		
		engine[0] = load(getClass().getResource("/net/kolls/railworld/sound/engine0.wav"), false);
		
		engine[1] = load(getClass().getResource("/net/kolls/railworld/sound/engine1.wav"), false);
		
		engine[2] = load(getClass().getResource("/net/kolls/railworld/sound/engine2.wav"), false);
		
		engine[3] = load(getClass().getResource("/net/kolls/railworld/sound/engine3.wav"), false);
		
		engine[4] = load(getClass().getResource("/net/kolls/railworld/sound/engine4.wav"), false);
		
		engine[5] = load(getClass().getResource("/net/kolls/railworld/sound/engine5.wav"), false);
		
		engine[6] = load(getClass().getResource("/net/kolls/railworld/sound/engine6.wav"), false);
		
		engine[7] = load(getClass().getResource("/net/kolls/railworld/sound/engine7.wav"), false);
		
		engine[8] = load(getClass().getResource("/net/kolls/railworld/sound/engine8.wav"), false);
		
		// on my system, if this is loaded before the engine, then the last engine sound
		// loaded is corrupted (!)
		horn = load(getClass().getResource("/net/kolls/railworld/sound/horn.wav"), true);		
		
		
		System.out.println("End of sound loading");
	}


}