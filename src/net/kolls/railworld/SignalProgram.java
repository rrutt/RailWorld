package net.kolls.railworld;

import java.awt.Color;

import javax.swing.Icon;

import net.kolls.railworld.io.SaveLoad;


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
 * Describes the necessary operations to be a signal program (control a signal).
 * 
 * @author Steve Kollmansberger
 */
public interface SignalProgram extends SaveLoad {

	/**
	 * Indicates the name of the program to display to the user as a tooltip.
	 * 
	 * @return Information to display over signal and in chooser window.
	 */
	String toString();
	
	/**
	 * Called by the signal when a train enters the signal from the facing side only.
	 * Train entering in the opposite direction will not cause notification.
	 * May be called arbitrarily often while the train is passing or stationary in the signal
	 * in the facing direction.
	 * 
	 * @param t The {@link Train} entering the signal.
	 */
	void enter(Train t);
	
	/**
	 * Indication that a train's controller is monitoring, responding to or waiting on the signal.
	 * This means the train is "in range" of the signal and could be influenced by it.
	 * Called by the controller arbitrarily often.
	 * 
	 * @param t The {@link Train} monitoring/responding/waiting.
	 */
	void reacting(Train t);
	
	/**
	 * Indicate the current color of the signal. (Green = go, yellow = 5 MPH, red = stop).
	 * This is the displayed color and also what trains may respond to.
	 * Signal calls this to find out what color it should display.
	 * 
	 * 
	 * @return Current color
	 * 
	 */
	Color status();
	
	/**
	 * For use by the signal chooser.
	 * 
	 * @return A 24x24 icon representing this signal program.
	 */
	Icon icon();
	
	
	
	
	
}
