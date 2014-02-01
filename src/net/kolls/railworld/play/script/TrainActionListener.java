package net.kolls.railworld.play.script;

import net.kolls.railworld.Train;

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
 * This interface is used to receive some or all events regarding trains.
 * 
 * @author Steve Kollmansberger
 */
public interface TrainActionListener {

	/**
	 * Informs the script that a {@link Train} action has been requested and allows it to cancel
	 *  the normal behavior.  Only instantanous actions are available here; actions like
	 *  brake and throttle can be read via the state.
	 *  
	 *  Behaviors:
	 *  load: Occurs when one or more cars are going to be loaded
	 *  unload: Occurs when one or more cars are going to be unloaded
	 *  horn: Occurs when the horn will be sounded
	 *  reverse: Occurs when the train's direction will be reversed
	 *  split: Occurs when the train will be split
	 *  selected: Occurs when the train is selected. CANNOT BE CANCELED (return value ignored)
	 *  deselected: Occurs when the train is deselected. CANNOT BE CANCELED (return value ignored)
	 *  step: Occurs at each step. CANNOT BE CANCELED (return value ignored)
	 *  
	 * 
	 * @param t The train involved
	 * @param action The name of the action requested
	 * @return Should the action be cancelled? (false is default, continue)
	 */
	boolean trainAction(Train t, String action);
	
}
