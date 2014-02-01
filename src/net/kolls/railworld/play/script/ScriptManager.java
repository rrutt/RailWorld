package net.kolls.railworld.play.script;


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



import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.RailAccident;


/**
 * The "master" script access point.  Handles loading scripts and will also dispatch calls to all
 * scripts within it.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class ScriptManager extends ArrayList<Script> {

	/**
	 * The current mission, if any.
	 */
	public Mission mission;
	
	/**
	 * Create an empty script manager.
	 */
	public ScriptManager() {
		super();
		drawables = new ArrayList<DrawListener>();
		tals = new ArrayList<TalCondition>();
	}
	
	
	/**
	 * Return a {@link ScriptManager} containing all internal scripts, missions, and support scripts.
	 * 
	 * @return A new ScriptManager with the scripts.
	 */
	public static ScriptManager allWithSupportingScripts() {
		ScriptManager sm = allScripts();
		sm.addAll(allMissions());
		sm.add(new net.kolls.railworld.scripts.missions.support.CountDownTimer());
	
		return sm;
	}
	
	
	/**
	 * Return a {@link ScriptManager} containing all internal scripts that are user-selectable.
	 * 
	 * 
	 * @return A new ScriptManager with the scripts.
	 */
	public static ScriptManager allScripts() {
		ScriptManager scripts = new ScriptManager();
		
		scripts.add(new net.kolls.railworld.scripts.Fuel());
		scripts.add(new net.kolls.railworld.scripts.Timer());
		scripts.add(new net.kolls.railworld.scripts.SpringSwitches());
		scripts.add(new net.kolls.railworld.scripts.Completer());
		scripts.add(new net.kolls.railworld.scripts.SpeedLimits());
		scripts.add(new net.kolls.railworld.scripts.HornRequired());
	
		
		
		return scripts;
		
		
	}
	
	/**
	 * Return a {@link ScriptManager} containing all internal missions.
	 * 
	 * 
	 * @return A new ScriptManager with the missions.
	 */
	public static ScriptManager allMissions() {
		ScriptManager scripts = new ScriptManager();
		
		
		scripts.add(new net.kolls.railworld.scripts.missions.SeattleDispatcher());
		scripts.add(new net.kolls.railworld.scripts.missions.Brewsky());
		
		
		return scripts;
		
		
	}
	
	/**
	 * All drawing listeners
	 */
	protected ArrayList<DrawListener> drawables;
	
	/**
	 * All train listeners
	 */
	protected ArrayList<TalCondition> tals;
	
	/**
	 * Add a listener to be notified when the canvas is drawn.
	 * 
	 * @param d A DrawListener to add.
	 */
	public void addDrawListener(DrawListener d) {
		drawables.add(d);
	}
	
	/**
	 * Remove a draw listener.
	 * 
	 * @param d The DrawListener to remove.
	 */
	public void removeDrawListener(DrawListener d) {

		drawables.remove(d);
	}
	
	/**
	 * Add a train action listener in the form of a condition.
	 * 
	 * @param tal The train action listener.
	 * @param t The train to notify on, or null for all trains.
	 * @param event The event name to notify on.
	 * @see TalCondition
	 */
	public void addTrainActionListener(TrainActionListener tal, Train t, String event) {
		tals.add(new TalCondition(tal,t,event));
	}
	
	/**
	 * Remove a train action listener.  Will remove
	 * a listener that matches the three given values.
	 * 
	 * @param tal Action listener
	 * @param t Train
	 * @param event Event name
	 */
	public void removeTrainActionListener(TrainActionListener tal, Train t, String event) {
		// this work around is needed because we can't say remove new talc ...
		
		TalCondition rc = null;
		TalCondition compareTo = new TalCondition(tal, t, event);
		for (TalCondition talc : tals) {
			if (talc.equals(compareTo)) rc = talc; 
		}
		tals.remove(rc);
	
	}
	
	/**
	 * The canvas is being drawn. Notify all
	 * draw listeners.  
	 * 
	 * @param gc The canvas' graphics context
	 * @param onScreen The on-screen rectangle
	 */
	public void draw(Graphics2D gc, Rectangle onScreen) {

		for (DrawListener d : drawables)
			d.draw(gc, onScreen);
		
		
	}

	/**
	 * The game is being started.
	 * 
	 * @param pf The game's PlayFrame.
	 */
	public void init(PlayFrame pf) {
		
		for (Script s : this)
			s.init(pf);

	}
	
	/**
	 * Before starting, offer the scripts an opportunity to modify
	 * the segment array.
	 * 
	 * @param lines  The existing segments
	 * @return New segments
	 */
	public RailSegment[] modifySegments(RailSegment[] lines) {
		
		for (Script s : this)
			lines = s.modifySegments(lines);
		
		return lines;
	}

	/**
	 * An action has occurred in the PlayFrame.
	 * 
	 * @param s The name of the event.
	 * @return Should the action be canceled? True means cancel, false means proceed.
	 */
	public boolean playFrameAction(String s) {
		boolean v = false;
		
		
		for (Script scr : this) {
			
			boolean cv = scr.playFrameAction(s);
			v = cv || v; // once true, stay true
			
			
		}
		return v;
	}
	
	/**
	 * A train has performed an action.
	 * 
	 * @param t The train involved
	 * @param s Name of event
	 * @return True if the event should be canceled, False otherwise.
	 */
	public boolean trainAction(Train t, String s) {
		boolean v = false;
		
		for (TalCondition tc : tals) {
			if (tc.t != null && tc.t != t) continue; // train must match or be null
			if (tc.event != null && tc.event.equals(s) == false) continue; // event must match or be null
			
			boolean cv = tc.tal.trainAction(t, s);
			v = cv || v; // once true, stay true
		}
		return v;
	}
	
	/**
	 * A train has taken a "step".  This could be a
	 * regular trainAction except that this one can
	 * cause an accident.
	 * 
	 * @param t The train that is being processed
	 * @throws RailAccident An accident, if one occurs. Note that this exception is not thrown by
	 * Java code but by the scripts, so that is why you don't see a throw anywhere here.
	 * It isn't checked so doesn't need to be declared.
	 */
	public void trainStep(Train t) throws RailAccident {
		trainAction(t, "step");
	}

	
	/**
	 * Create a single-use TrainActionListener which picks up a given
	 * train on the next step, and then disbands.
	 * The runnable "r" will be run on the train's next step.
	 * This can be used to defer some action one step, or
	 * create an accident from a method not approved for throwing accidents.
	 * 
	 * @param pf The PlayFrame to reference
	 * @param t The train to watch
	 * @param r The runnable to run
	 */
	public static void DeferIntoStep(final PlayFrame mpf, final Train train, final Runnable r) {
		
		final TrainActionListener tal = new TrainActionListener() {
			
			@Override
			public boolean trainAction(Train t, String action) {
				if (t == train && action.equals("step")) {
					
					final TrainActionListener _tal = this;
					// to avoid a concurrent modification
				 	// of removing the listener while we are IN the listener
				 	// delay it until the next loop
				 	mpf.gl.runInLoop(new java.lang.Runnable() {
				 		public void run() {
				 			mpf.jdb.sm.removeTrainActionListener(_tal,
			 					train, "step");
			 				}
			 			});	 
					
					r.run();
				}
				return false;
			}
			
		};
		
		
		// to avoid a concurrent modification
	 	// of adding the listener while we are IN the listener
	 	// delay it until the next loop
	 	mpf.gl.runInLoop(new java.lang.Runnable() {
	 		public void run() {
	 			mpf.jdb.sm.addTrainActionListener(tal,
 					train, "step");
 				}
 			});
	 	
	}
	
	
}
