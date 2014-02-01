package net.kolls.railworld.play.script;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.io.SaveLoad;
import net.kolls.railworld.play.*;

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
 * The methods a plug-in must implement.  Many may be implemented with empty methods.
 * 
 * @author Steve Kollmansberger
 */
public interface Script extends SaveLoad {
	/**
	 * Called before game start to give a selection list of possible plugins to the user.
	 *  
	 * @return The user displayed name or title for this script. 
	 */
	String toString();
	
	/**
	 * Setup the script.  Called once when the game begins.
	 * 
	 * @param pf The {@link PlayFrame} for this game
	 *
	 */
	void init(PlayFrame pf);
	
	
	/**
	 * Before the game starts, script is allowed to modify
	 * the segments.  Segment replacement should only occur
	 * here.  After this, the segments are fixed.
	 * 
	 * @param lines  The current segment array.
	 * @return The new segment array.
	 */
	RailSegment[] modifySegments(RailSegment[] lines);
	
	
	/**
	 * Informs the script that a {@link PlayFrame} button has been pressed and allows it to cancel
	 * the normal action.
	 * 
	 * @param action The name of the command executed
	 * @return Should the action be cancelled? (false is default, continue)
	 */
	boolean playFrameAction(String action);
	
	
	/**
	 * Queried before game start to determine if this script
	 * should be on by default or not.  This is generally
	 * only used the first time a player starts the game.
	 * After that, it records their selections.  Also
	 * used for applet.
	 * 
	 * @return true if this script should be on.
	 */
	boolean onByDefault();
}
