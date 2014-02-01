package net.kolls.railworld.scripts;

import java.awt.Color;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.event.ListDataEvent;

import net.kolls.railworld.Car;
import net.kolls.railworld.Distance;
import net.kolls.railworld.Factories;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.car.AbstractCar;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.play.script.TrainActionListener;
import net.kolls.railworld.segment.LUSegment;

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
 * Allows cars to be "completed" when they are loaded,
 * or unloaded if no facility for loading exists.
 * 
 * @author Steve Kollmansberger
 */
public class Completer implements Script, TrainActionListener {

	private HashSet<String> canFill;
	private PlayFrame mpf;
	private RailSegment[] la;
	private boolean pop = false;
	
	@SuppressWarnings("unused")
	private Train trainToComplete;
	private boolean[] cars;
	
	
	@Override
	public String toString() {
		return "Completed Cars";
	}
	
	@Override
	public void init(PlayFrame pf) {
		// find out from the map what kinds of car we can fill
		canFill = new HashSet<String>();
		
		mpf = pf;
		la = pf.jdb.la; 
	
		// find out what kinds of cars we can fill
		// when those become empty, they are ok
		// other kinds (we can't fill) are then complete
		for (int i = 0; i < la.length; i++)	 {
			if (la[i] instanceof LUSegment) {
				LUSegment myseg = (LUSegment)la[i];
				for (int j = 0; j < myseg.lu().length; j++) {
					if (myseg.lu()[j].loaded() == false) // accepts empty, can load
						canFill.add(myseg.lu()[j].show());
				}
			} 
		}
		
	 	// have to register completed car to allow it to be loaded
 		net.kolls.railworld.Factories.cars.addType(new CompletedCar());
 		
 		 pf.jdb.sm.addTrainActionListener(this,
 			null, "load");

 		 pf.jdb.sm.addTrainActionListener(this,
 			null, "unload");
 			
 			
 		 
 		pf.jdb.trains.addListDataListener(new javax.swing.event.ListDataListener() {

			@Override
			public void contentsChanged(ListDataEvent e) { }

			@Override
			public void intervalAdded(ListDataEvent e) {
				trainCreate(mpf.jdb.trains.get(e.getIndex0()));
				
			}

			@Override
			public void intervalRemoved(ListDataEvent e) { }
 			
 		});
 		
 
	}
	
	protected void trainCreate(Train train) {
		for (int j = 0; j < train.array().length; j++) {
			if (train.array()[j].isLoadable() == false) continue;
							
				// if the train was created by populate, then loaded
				// cars are already done
				// how to tell?? First idea: look at train location
				// problem: split and join are new trains
				// so they look just like populate commands
				// but we don't want an incomplete loaded
				// to become complete just because we split or joined
				// so use a flag instead
						
				if (pop) {
					if (train.array()[j].loaded()) {
						// car has been loaded
						train.array()[j] = new CompletedCar(train.array()[j]);				
					} else { 
						if (canFill.contains(train.array()[j].show()) == false) {
							// no, it's complete
							train.array()[j] = new CompletedCar(train.array()[j]);
						}	
					}
				}
							
			}
			train.getController().fillConsist();
	}


	@Override
	public RailSegment[] modifySegments(RailSegment[] lines) {
		return lines;
	}

	@Override
	public boolean playFrameAction(String action) {
		if (action == "Populate") {
			pop = true;
		} else 
			pop = false;
		
			

		return false;	
	}

	@Override
	public void load(Map<String, String> data) {

	}

	@Override
	public Object newInstance() {
		return new Completer();
	}

	@Override
	public Map<String, String> save() {
		return null;
	}
	
	@Override
	public boolean trainAction(final Train train, String action) {

		
		
			// action must be load or unload


			trainToComplete = train;
			cars = new boolean[train.array().length];
			for (int i = 0; i < train.array().length; i++) {
				cars[i] = train.array()[i].loaded();
			}
			
			
			
			// because the load/unload action happens BEFORE its loaded
			// wait until the next step to actually do the work
			ScriptManager.DeferIntoStep(mpf, train, new Runnable() {
				@Override
				public void run() {
					trainLU(train);
				}
			});
			
			
		 
		return false;
		
	}
	
	private void trainLU(Train train) {
		Car sel = mpf.jdb.trains.getSelectedCar();
		
		
		for (int i = 0; i < train.array().length; i++) {
			boolean dsel = false;
			if (cars[i] == false && train.array()[i].loaded()) {
				// car has been loaded
				if (train.array()[i] == sel) dsel = true;
				
				train.array()[i] = new CompletedCar(train.array()[i]);
			}
			if (cars[i] && train.array()[i].loaded() == false) {
				// car has been unloaded
				// check if we can load this one
				if (canFill.contains(train.array()[i].show()) == false) {
					// no, it's complete
					if (train.array()[i] == sel) dsel = true;
					
					train.array()[i] = new CompletedCar(train.array()[i]);

				}
			}
			if (dsel) {
				mpf.jdb.trains.select(train, train.array()[i]);
				dsel = false;
			}
		}
		train.getController().fillConsist();
		trainToComplete = null;
	}
	
	@Override
	public boolean onByDefault() {
		return false;
	}
	
	/**
	 * Encapsulates a car which is "completed" and can't do anything else.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class CompletedCar extends AbstractCar {

		/**
		 * The encapsulated car.  No changes are expected to occur to it.
		 */
		public Car c;
		
		private final Color midc = new Color(0f, 0.5f, 0f);
		
		public CompletedCar() { }
		
		public CompletedCar(Car c) { this.c = c; }
		
		@Override
		public Color color() {
			return c.color();
		}

		@Override
		public Distance length() {
			return c.length();
			
		}

		@Override
		public String show() {
			return "Comp. " + c.show();
		}

		@Override
		public int weight() {
			return c.weight();
		}
		
		@Override
		public Color midColor() { 
			return midc; 
		}
		
		@Override
		public boolean loaded() { return true; }
    	
    	@Override
		public boolean isLoadable() { return false; }
    	
    	@Override
		public String toString() { return "Completed"; }
    	
    	@Override
		public boolean canUserCreate() { return false; }
    	
    	@Override
		public Object newInstance() {
    		return new CompletedCar(c);
    	}
		
    	@Override
		public void load(Map<String, String> data) {
    		
    		try {
				c = Factories.cars.createInstance(data.get("type"));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
			
			if (data.get("loaded").equals("true"))
				c.load();
			
    		
    	}
    	
    	@Override
		public Map<String, String> save() {
    		Hashtable<String, String> s = new Hashtable<String, String>();
    		s.put("type", c.toString());
    		s.put("loaded", Boolean.toString(c.loaded()));
    		return s;
    	}
	}

	
	
	

}
