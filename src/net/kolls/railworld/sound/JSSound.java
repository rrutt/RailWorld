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


import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.SwingUtilities;


import net.kolls.railworld.Sound;

/**
 * Java Sound API (the standard Java approach to sound)
 * 
 * @author Steve Kollmansberger
 *
 */
public class JSSound extends Sound {
	private Clip myClip;
	
	private DataLine.Info info;
	private AudioInputStream stream;
	private AudioFormat format;
	
	private boolean rst;
	
	// Port.Info SPEAKER

	
	/**
	 * Create a Java sound clip.
	 * @param filen URL of sound source
	 * @param restart  Should the sound restart if played while currently playing?
	 */
	public JSSound(URL filen, boolean restart) {

		rst = restart;
		
		try {

//		File myFile = new File (".", filen);
		
		stream = AudioSystem.getAudioInputStream(filen);
		format = stream.getFormat();
		

		info = new DataLine.Info(
                                          Clip.class,
                                          format,
                                          format.getFrameSize()*1000
                                          /*((int) stream.getFrameLength() *
                                              format.getFrameSize())*/);


		
		myClip = (Clip)AudioSystem.getLine(info);
		
		
		
		//ln.open(stream, format.getFrameSize()*12000);
		myClip.open(stream);
		System.out.println(myClip.getBufferSize());
		
		loopcnt = 0;
		frozen = false;
		fzlc = 0;

		} catch (Exception e) { System.out.println("Unable to prepare sound "+filen+": "+e); }
	}
	
	@Override
	public boolean canPlay() {
		return myClip.isOpen();
	}

	@Override
	public void play() {
		
		
		loopcnt = 0;
		myClip.loop(0);

		if (playing() && rst == false) return;

		myClip.setFramePosition(0);
		myClip.start();
	}

	@Override
	public void loop() {
		if (frozen) {
			fzlc++;
			return;
		}
		loopcnt++;
		if (loopcnt > 1) return; // already looping


		myClip.setFramePosition(0);
		myClip.setLoopPoints(0, -1);

		myClip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	@Override
	public void stop() {
		if (fzlc > 0 && frozen) {
			fzlc--;
			return;
		}
		if (loopcnt > 1) {
			loopcnt--; // deregister a loop
			return;
		}
		if (loopcnt == 1) {
			loopcnt = 0;
			myClip.loop(0);
			
			myClip.setFramePosition(myClip.getFrameLength()-1);
			return;
		}
		
		
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				BooleanControl bc = (BooleanControl)myClip.getControl(BooleanControl.Type.MUTE);
				bc.setValue(true);
			}
			
		});
		
		
		
		
		//myClip.setFramePosition(myClip.getFrameLength());
		//myClip.stop();		
		
		
	}

	@Override
	public boolean playing() {
		return myClip.isActive();
	}

	
}
