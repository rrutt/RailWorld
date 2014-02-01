package net.kolls.railworld.tc;


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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.kolls.railworld.Car;
import net.kolls.railworld.Factories;
import net.kolls.railworld.Train;
import net.kolls.railworld.TrainControl;
import net.kolls.railworld.play.script.ScriptManager;

/**
 * Allows two controllers to be mixed.  One controller is used when the train is selected,
 * the other controller is used when the train is not selected.
 * 
 * @author Steve Kollmansberger
 *
 */
public class MixControl extends TrainControl {

	private TrainControl current, s ,ds;
	
	@Override
	public void fillConsist() {
		// only selected is shown
		s.fillConsist();
	}
	
	@Override
	public void attach(Train t) {
		super.attach(t);
		s.attach(t);
		ds.attach(t);
		
		

		// whatever the selected controller shows, we show too
		// remove the consist added by the super
		removeAll();
		add(s);
		
	}
	
	/**
	 * Initialize with controllers.
	 * 
	 * @param selected  The controller to use when selected
	 * @param notSelected THe controller to use when not selected
	 */
	public MixControl(TrainControl selected, TrainControl notSelected) {
		super();
		
		
		set(selected, notSelected);
	}
	
	/**
	 * Create a default mix controller with no sub-controllers.
	 * For factory methods you need this. Followup with
	 * {@link #set(TrainControl, TrainControl)}
	 */
	public MixControl() {
		super();
	}
	
	@Override
	public TrainControl newInstance() {
		if (s != null && ds != null)
			return new MixControl((TrainControl)s.newInstance(), (TrainControl)ds.newInstance());
		else
			return new MixControl();
	}
	
	@Override
	public void setTrainActionScriptNotify(ScriptManager s) {
		super.setTrainActionScriptNotify(s);		
		this.s.setTrainActionScriptNotify(s);
		this.ds.setTrainActionScriptNotify(s);
	}
	
	/**
	 * Set the train controllers to use.
	 * 
	 * @param selected Controller to use when train is selected
	 * @param notSelected Controller to use when train is not selected
	 */
	public void set(TrainControl selected, TrainControl notSelected) {
		
		
		s = selected;
		current = ds = notSelected;
		
		attach(selected.getTrain());
		

	}
	
	@Override
	public void deselect() {
		super.deselect();
		ds.deselect();
		s.deselect();
		current = ds;
		

	}

	@Override
	public void select() {
		super.select();
		ds.select();
		s.select();
		current = s;
		
	}
	
	@Override
	public void run() {
		current.run();
	}
	
	@Override
	public boolean process() {
		return current.process();
	}
	
	@Override
	public void setSelected(Car c) {
		super.setSelected(c);
		s.setSelected(c);
		ds.setSelected(c);
		
	}

	public void load(Map<String, String> data) {
		Map<String, String> mds = new HashMap<String, String>();
		Map<String, String> ms = new HashMap<String, String>();
		
		
		Set<Entry<String, String>> ts = data.entrySet();
		
		String dsi = data.get("_D");
		String si = data.get("_S");
		try {
			s = Factories.controllers.createInstance(si);
			ds = Factories.controllers.createInstance(dsi);	
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		current = ds;
		
		

		
		Iterator<Entry<String, String>> its = ts.iterator();
		
		while (its.hasNext()) {
			Entry<String, String> es = its.next();
			if (es.getKey().charAt(0) == 'D') {
				mds.put(es.getKey().substring(1), es.getValue());
			} else {
				ms.put(es.getKey().substring(1), es.getValue());
			}
			
		}
		
		s.load(ms);
		ds.load(mds);
		
		
		
		
	}

	@Override
	public String toString() {
		
		return "MixControl";
	}
	
	public Map<String, String> save() {
		Map<String, String> mds = ds.save();
		Map<String, String> ms = s.save();
		
		Map<String, String> both = new HashMap<String, String>();
		
		both.put("_D", ds.toString());
		both.put("_S", s.toString());
		
		Set<Entry<String, String>> ts;
		Iterator<Entry<String, String>> its;
		
		if (mds != null) {
			ts = mds.entrySet();
			its = ts.iterator();
		
			while (its.hasNext()) {
				Entry<String, String> es = its.next();
				both.put("D"+es.getKey(), es.getValue());
			
			}
		}
		
		if (ms != null) {
		
			ts = ms.entrySet();
			its = ts.iterator();
		
			while (its.hasNext()) {
				Entry<String, String> es = its.next();
				both.put("S"+es.getKey(), es.getValue());
			
			}
		
		}
		
		return both;
	}
	
	@Override
	public void load() {
		current.load();	
	}
	
	@Override
	public void unload() {
		current.unload();	
	}
	
	
	

}

