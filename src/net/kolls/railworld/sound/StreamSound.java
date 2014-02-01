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
import net.kolls.railworld.Sound;

/**
 * Stream sound puts the details of sound playback in the program's hands. 
 * Requires more overhead but may allow for better sound behavior?
 * 
 * @author Steve Kollmansberger
 *
 */
public class StreamSound extends Sound {
	private SourceDataLine myClip;
	
	private DataLine.Info info;
	private AudioInputStream stream;
	private AudioFormat format;
	
	private byte[] bytes;
	private Thread player;
	private int bytesPerSecond;
	
	private boolean rst;
	
	private int stepSize;

	private boolean play;

	
	/**
	 * Create a stream sound.
	 * 
	 * @param filen URL of sound source
	 * @param restart Restart sound if play command given while currently playing
	 */
	public StreamSound(URL filen, boolean restart) {

		rst = restart;
		
		try {

		
		stream = AudioSystem.getAudioInputStream(filen);
		format = stream.getFormat();
		
		bytesPerSecond = (int)(format.getFrameSize()*format.getFrameRate());
		stepSize = (int)(format.getFrameSize()*0.5*format.getFrameRate());
		
		
		info = new DataLine.Info(
                                          SourceDataLine.class,
                                          format,
                                          stepSize
                                          );


		
		myClip = (SourceDataLine)AudioSystem.getLine(info);
		
				
		
		myClip.open(format, stepSize);
		
		
		loopcnt = 0;
		frozen = false;
		fzlc = 0;

		bytes = new byte[stream.available()];
		stream.read(bytes);
		
		} catch (Exception e) { System.out.println("Unable to prepare sound "+filen+": "+e); }
		
		player = new Thread(new Runnable() {

			public void run() {
				
				while(true) {
					// wait for run notification
					
					synchronized(player) {
						try {
							player.wait();
						} catch (InterruptedException e1) {
							// told to stop?
							continue;
					
						}
					}
					
					
					System.out.println("Running");
				
					int pos = 0;
					
					try {
						myClip.open(format, stepSize);
					} catch (Exception ex) {
						ex.printStackTrace();
						return;
					}
					myClip.start();
					
					
					System.out.println("LEN:"+bytes.length);
					while (pos < bytes.length && play) {
						int stepl = Math.min(stepSize, bytes.length-pos);
						stepl = Math.min(stepl, myClip.available());
						
						stepl = myClip.write(bytes, pos, stepl);
						if (stepl == 0) {
							/*System.out.println("Audio blocked, abort");
							myClip.flush();
							myClip.stop();
							return;*/
							continue;
						}
						System.out.println("WRITTEN:" + stepl);
						pos += stepl;
					
						try {
							// wait 75% of the time to avoid clicks due to delays
							Thread.sleep( (stepl*750)/bytesPerSecond);
						} catch (InterruptedException e) {
							// stop
							System.out.println("Interrupted");
							myClip.flush();
							myClip.stop();
							break;
							
						}
						if (pos == bytes.length && loopcnt > 0)
							pos = 0;
							
					
					}
					myClip.drain();
					myClip.stop();
					myClip.close();
					System.out.println("DONE");
				}
				
			}
			
		});
		player.setDaemon(true);
		player.setPriority(Thread.MAX_PRIORITY);
		player.start();
		
		
	}
	
	@Override
	public boolean canPlay() {
		return myClip.isOpen();
	}

	@Override
	public void play() {
		if (playing() && rst == false) return;
		
		if (playing()) {
			player.interrupt();
			Thread.yield();
		}
		
		play = true;
		synchronized (player) {
			player.notify();
		}
		System.out.println("OK");
		
		
		
	}

	@Override
	public void loop() {
		if (frozen) {
			fzlc++;
			return;
		}
		loopcnt++;
		if (loopcnt > 1) return; // already looping

		play = true;
		synchronized (player) {
			player.notify();
		}
		
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
		
		loopcnt = 0;
		play = false;
		player.interrupt();
		
	}

	@Override
	public boolean playing() {
		return myClip.isActive();
	}

	
}
