package net.kolls.railworld.scripts;


import java.awt.Color;
import java.text.NumberFormat;
import java.util.Map;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.kolls.railworld.Car;
import net.kolls.railworld.GameLoop;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.car.Engine;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.TrainActionListener;

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
 * An engine with finite fuel
 * 
 * @author Steve Kollmansberger
 */
public class Fuel implements Script, TrainActionListener, ListDataListener {

	// idle: 5 g/h
	// 400 ton-miles per gallon (avg)
	// 200 gallon/hour at full throttle

	// 2112000 ton-feet per gallon (avg)
	//
	// (tons * feet) / 2112000  = gallons used A
	// if accelerating, a penalty, if decelerating, a bonus
	// 200 / 40 (max mph) = 5
	// for each mph above current speed, take 5 g/h which 
	// 3 600 000 milliseconds per hour is
	private final double permphB = 5.0 / 3600000.0;

	// capacity 5000 gal
	// fuel weight: 7 lbs/gal

	private TrainActionListener tal;

	private PlayFrame pf;
	
	@Override
	public void init(PlayFrame pf) {
		// have to register new cars
		// 5000 gals is not interesting... give them 15
		FuelEngine x = new FuelEngine();
 		net.kolls.railworld.Factories.cars.addType(x);
 		this.pf = pf;
 		this.tal = this;
 		
 		pf.jdb.trains.addListDataListener(this);

	}

	@Override
	public String toString() {
		return "Fuel";
	}
	
	@Override
	public boolean playFrameAction(String action) {
		
		return false;
	}

	public RailSegment[] modifySegments(RailSegment[] lines) {
		return lines;
	
	}
	
	
	@Override
	public Object newInstance() {
		return new FuelEngine();
	}

	@Override
	public boolean onByDefault() {
		return true;
	}

	@Override
	public boolean trainAction(Train train, String action) {

		int tons = train.weight();
		double mph = train.vel();
		int throt = train.getThrottle();
		

		

		double usageB = 0;
		double tmaxv = throt * (Train.MAX_SPEED_MPH / Train.MAX_THROTTLE);
		double diff = tmaxv - mph;
		
		if (diff > 0) usageB = diff * permphB * 500.0; // fudge factor to make this more apparent

		double feet = GameLoop.feetPerStepSpeed(tmaxv);
		double usageA = (tons * feet) / 2112000;
			

			
		// distribute usage among all fueled engines
		int fengs = 0;
		Car[] carrs = train.array();
		for (Car c : carrs) {
			if (c.isEngine()) {
				fengs++;
			}
		}

		for (Car c : carrs) {
			if (c instanceof FuelEngine) {
			
				double of = ((FuelEngine)c).fuel;
				double f = of;
				f -= (usageA+usageB) / fengs;
				if (f < 0) f = 0;
			
				((FuelEngine)c).fuel = f;
			
				if ( (int)of - (int)f >= 1
						|| (of > 0 && f == 0)) 
				train.getController().fillConsist();
			
			
			}
		}
		
		return false;
	}

	public class FuelEngine extends Engine {
		
		/**
		 * Fuel remaining, in gallons
		 */
		public double fuel;
		private final Color midcf = new Color(0f, 0.5f, 0f);
		private final Color midce = new Color(1f, 1f, 1f);
		
		@Override
		public FuelEngine newInstance() {
			return new FuelEngine();
		}
		
		public FuelEngine() {
			super();
			fuel = 15;
		}
		
		@Override
		public Color midColor() {
			if (this.fuel > 0) 
				return midcf; 
			else 
				return midce; 
		}
		
    	@Override
		public String show() { 
    		String cdesc = super.show();
    		if (this.fuel > 2)
	    		return cdesc+ " (" + NumberFormat.getInstance().format((int)this.fuel) + " gals)"; 
	    	if (this.fuel >= 1)
	    		return cdesc+ " (1 gal)";
	    	if (this.fuel > 0)
	    		return cdesc+ " (<1 gal)";
	    	return cdesc+ " (empty)";
	    	
    	}
    	
    	@Override
		public String toString() { return "FuelEngine"; }
    	
    	@Override
		public boolean isEngine() {
    		return (this.fuel > 0);
    	}
    	
    	@Override
		public Map<String, String> save() {
    		Map<String, String> s = super.save();
    		s.put("fuel", Double.toString(this.fuel));
    		return s;
    	}
    	
    	@Override
		public void load(Map<String, String> m) {
    		super.load(m);
    		this.fuel = Double.parseDouble(m.get("fuel"));
    	}
	}

	@Override
	public void load(Map<String, String> data) {	}

	@Override
	public Map<String, String> save() {
		return null;
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		final Train train = pf.jdb.trains.get(e.getIndex0());
		boolean isfe = false;
		for (Car c : train.array()) {
			if (c instanceof FuelEngine) isfe = true;
		}
		
		if (isfe) {
			
				pf.gl.runInLoop(new java.lang.Runnable() {
					public void run() {
						pf.jdb.sm.addTrainActionListener(tal,
							train, "step");
					}
				});
			}
			
		
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		// TODO Auto-generated method stub
		
	};
	
	
}
