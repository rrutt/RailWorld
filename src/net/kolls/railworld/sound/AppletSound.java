package net.kolls.railworld.sound;

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

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

import net.kolls.railworld.Sound;

/**
 * Provides sound via the applet sound API.  
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("removal")
public class AppletSound extends Sound {

	private AudioClip ac;
	
	/**
	 * Create an applet sound based on the given sound resource
	 * 
	 * @param filen URL of sound to load
	 */
	public AppletSound(URL filen) {
		try {
			ac = Applet.newAudioClip(filen);
		} catch (Exception e) {
			System.out.println("Unable to prepare sound "+filen+": "+e);
			ac = null;
		}
	}
	
	@Override
	public boolean canPlay() {
		return (ac != null);
	}

	@Override
	public void loop() {
		if (ac == null) return;
		if (frozen) {
			fzlc++;
			return;
		}
		loopcnt++;
		if (loopcnt > 1) return; // already looping

		ac.loop();

	}

	@Override
	public void play() {
		ac.play();

	}

	@Override
	public boolean playing() {
		return (loopcnt > 0);
	}

	@Override
	public void stop() {
		if (ac == null) return;
		if (fzlc > 0 && frozen) {
			fzlc--;
			return;
		}
		if (loopcnt > 1) {
			loopcnt--; // deregister a loop
			return;
		}
		if (loopcnt == 1) loopcnt = 0;
		
		
		
		ac.stop();	

	}

}
