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

/**
 * Allows playback of sound.  The behavior is designed for multiple requestors to be handled automatically
 * and each treated as if they are the only requestor.  The sound can also be frozen (paused), although requests
 * by requestors must still be processed as if the sound was playing.  The multiple requestors are handled via a 
 * "loop count" which is incremented whenever a requestor asks for a loop and decremented whenever a requestor 
 * says "stop".  Thus, "stop" may not actually stop the sound.
 * 
 * @author Steve Kollmansberger
 */
public abstract class Sound {
	
	/**
	 * Frozen loop count
	 */
	protected int fzlc;
	
	/**
	 * Is the sound frozen
	 */
	protected boolean frozen;
	
	/**
	 * Loop count
	 */
	protected int loopcnt;
	
	/**
	 * Is the sound loaded and ready to play?
	 * 
	 * @return <code>true</code> if the sound can be played.
	 */
	public abstract boolean canPlay();
	
	/**
	 * Play the sound.
	 *
	 */
	public abstract void play();
	
	/**
	 * Loop the sound.  If the sound is already looping, increment the loop counter.
	 *
	 */
	public abstract void loop();
	
	/**
	 * Stop the sound.  If multiple loops are in effect, simply decrement the loop counter.
	 *
	 */
	public abstract void stop();
	
	/**
	 * Indicate if the sound is currently playing or looping.
	 * 
	 * @return <code>true</code> if the sound is playing.
	 */
	public abstract boolean playing();
	
	
	/**
	 * Freeze the sound.  Stops actual playing but maintains loop counts.
	 *
	 */
	public void freeze() {
		
		fzlc = loopcnt;
		while (playing()) stop();
		frozen = true;
	}
	
	/**
	 * Unfreeze the sound.  Resume playing if any loops remain.
	 *
	 */
	public void unfreeze() {
		frozen = false;
		while (fzlc > 0) {
			loop();
			fzlc--;
		}
	}

}
