package net.kolls.railworld.play.script;

import javax.swing.JPanel;

import net.kolls.railworld.play.RailAccident;

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
 * A script which acts as an over-arching definer of play.
 * Although many scripts may run, only one mission is expected
 * at a time.
 * 
 * @author Steve Kollmansberger
 */
public interface Mission extends Script {

	/**
	 * Informs the mission that a {@link RailAccident} has occurred.
	 * 
	 * @param ra The accident instance.
	 */
	void railAccident(RailAccident ra);
	
	/**
	 * Shows text/graphic describing the mission to the user.
	 * @return a panel to display
	 */
	JPanel briefing();
	
	/**
	 * Creates a script manager with scripts configured
	 * as desired for this mission.  The mission
	 * should setup itself in the new script manager as well.
	 * @return A new script manager
	 */
	ScriptManager createScriptManager();

	/**
	 * Indicate what map should be used with this mission.
	 * 
	 * @return Filename of the map to use
	 */
	String rwmFilename();
	
}
