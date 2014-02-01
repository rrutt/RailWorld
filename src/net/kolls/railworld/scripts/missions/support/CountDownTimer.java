package net.kolls.railworld.scripts.missions.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.kolls.railworld.scripts.Timer;


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
 * 
 * Maintains an hour/minute/second timer for the user.
 * Counts down from a specified point rather than up.
 * May notify listener(s) when time is up.  The basis is
 * when the map was started.
 * 
 * @author Steve Kollmansberger
 */
public class CountDownTimer extends Timer {

	/**
	 * The time to count down from, in milliseconds
	 */
	public long countDownFrom;
	
	
	private ArrayList<ActionListener> actionListeners = 
		new ArrayList<ActionListener>();
	
	/**
	 * Add a listener to be fired when time is up.  Note that
	 * the listener(s) may be fired arbitrarily often once time is up.
	 * Listeners are not saved with the save method.
	 * 
	 * @param a The listener to add
	 */
	public void addActionListener(ActionListener a) {
		actionListeners.add(a);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param a The listener to remove
	 * @see #addActionListener(ActionListener)
	 * 
	 */
	public void removeActionListener(ActionListener a) {
		actionListeners.remove(a);
	}
	
	/**
	 * Create a count down timer.  The countDownFrom
	 * defaults to 10 minutes.
	 */
	public CountDownTimer() {
		super();
		countDownFrom = 60 * 1000; // 10 min
	}
	
	public CountDownTimer(long milliseconds) {
		super();
		countDownFrom = milliseconds;
	}
	
	@Override
	public void load(Map<String, String> data) {
		countDownFrom = Long.parseLong(data.get("countDownFrom"));
	}
	
	@Override
	public String toString() {
		return "CountDownTimer";
	}

	@Override
	public Object newInstance() {
		return new CountDownTimer();
	}

	
	@Override
	public Map<String, String> save() {
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("countDownFrom", Long.toString(countDownFrom));
		return ht;
	}
	
	@Override
	protected long getTime() {
		long cd = countDownFrom - super.getTime();
		if (cd <= 0) {
			for (ActionListener al : actionListeners)
				al.actionPerformed(new ActionEvent(this, 0, "TimesUp"));
		}
		return Math.max(0, cd); // disallow negative time
	}
	
}
