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


import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

import net.kolls.railworld.io.SaveLoad;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.LUSegment;


/**
 * A train controller provides a method for trains to be controlled (the throttle, brake, etc)
 * Controllers also present a visible appearance as a panel to the user.  The panel may allow
 * the user to request behavior from the controller.  The panel also includes a consist report
 * as well as the weight, speed, and length of the train.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public abstract class TrainControl extends JPanel implements Runnable, SaveLoad {

	/**
	 * Label displaying current weight 
	 */
	protected JLabel weight;
	
	/**
	 * Label displaying current speed
	 */
	protected JLabel speed;
	
	/**
	 * Label displaying current length
	 */
	protected JLabel length;
	
	/**
	 * Label containing the "consist"
	 * Collection of cars
	 */
	protected JPanel consist;
	// don't need to serialize train and car
	// because we record them separately
	/**
	 * The train being controlled
	 */
	protected Train myT;
	
	/**
	 * The selected car in the train, or null.
	 */
	protected Car selected;

	/**
	 * Cars that can be loaded (are empty and on an appropriate LUSegment)
	 */
	protected Car[] lable;
	
	/**
	 * Cars that can be unloaded (are full and on an appropriate LUSegment)
	 */
	protected Car[] ulable;
	
	
	/**
	 * The consist report, weight, length, and speed are added to the panel,
	 * which is laid out using {@link BoxLayout}.  Deriving controllers may added additional controls
	 * to the panel. 
	 * 
	 * @param t The initial train.
	 */
	public void attach(Train t) {
		removeAll();
		
		myT = t;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		consist = new JPanel();
		consist.setBorder(BorderFactory.createTitledBorder("Consist"));
		
		
		weight = new JLabel();
		speed = new JLabel();
		length = new JLabel();
		add(consist);
		fillConsist();
		
		
		
		

		
		add(lbl("Weight: ", weight));
		add(lbl("Length: ", length));
		add(lbl("Speed: ", speed));
		add(Box.createVerticalGlue());
	}
		
	
	/**
	 * The script manager to notify about events.
	 */
	protected ScriptManager _s;
	
	/**
	 * Get the script manager
	 *  
	 * @return Script manager currently notified
	 */
	public ScriptManager getTrainActionScriptNotify() { return _s; }
	
	/**
	 * Update the script manager notified about events.
	 * 
	 * @param s The script manager to notify.
	 */
	public void setTrainActionScriptNotify(ScriptManager s) { _s = s; }
	
	/**
	 * Create a controller.  By default, the controller does nothing.  Call {@link #attach(Train)}
	 * to attach the controller to a train.
	 * 
	 * 
	 */
	public TrainControl() { }
	
	private JPanel lbl(String s, JLabel d) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel(s));
		p.add(Box.createHorizontalGlue());
		p.add(d);
		return p;

	}
	
	
	private void hIncr(Map<String,Integer> h, String v) {
		Integer n = h.get(v);
		if (n == null) h.put(v, new Integer(1)); else h.put(v, new Integer(n.intValue()+1));
	}
	
	/**
	 * Reset the consist.  This should be called whenever a change has happened to the train.
	 * For example, if any cars are loaded or unloaded, the consist must be reset.
	 *
	 */
	public void fillConsist() {
		
		if (consist == null || myT == null) return;
		
		consist.removeAll();
		Car[] a = myT.array();
		
		Map<String,Integer> l = new LinkedHashMap<String,Integer>(); // loaded
		Map<String,Integer> t = new LinkedHashMap<String,Integer>(); // total
		Map<String,Boolean> la = new LinkedHashMap<String,Boolean>(); // is it loadable?
		for (int i = 0; i < a.length; i++) {
		
			if (a[i].isLoadable() == false) {
				
				hIncr(t,a[i].show()); // total
				la.put(a[i].show(), new Boolean(false)); // not loadable
			} else {
				if (a[i].loaded()) hIncr(l,a[i].show()); // loaded
				hIncr(t,a[i].show()); // total
				la.put(a[i].show(), new Boolean(true)); // loadable
			}
		}	
		
		
		consist.setLayout(new BoxLayout(consist, BoxLayout.PAGE_AXIS));
		
		
	
		for (String k : t.keySet()) {

			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
			
			p.add(new JLabel(k));
			p.add(Box.createHorizontalGlue());
			
			String k2;
			if (l.get(k) == null) k2 = "0";
			else k2 = l.get(k).toString();
			
			String k3 = t.get(k).toString();
			
			// only display out of if loadable
			if (la.get(k).booleanValue())
				p.add(new JLabel(k2 + " / " + k3, SwingConstants.RIGHT));
			else
				p.add(new JLabel(k3, SwingConstants.RIGHT));
			
			consist.add(p);
			
		}

		consist.revalidate();
		consist.setPreferredSize(new Dimension(190,consist.getPreferredSize().height+5)); // plus 5 for border
		consist.revalidate();
		
		
		
	}

	/**
	 * 
	 * @return The selected {@link Car}, or <code>null</code>
	 */
	public Car getSelected() { return selected; }
	
	/**
	 * The user selected a car.
	 * @param c The selected {@link Car}.
	 */
	public void setSelected(Car c) { selected = c; }
    
    /**
     * 
     * @return This train
     */
    public Train getTrain() { return myT; }

    /**
     * Called when the train is deselected. May override but be sure to call super.
     *
     */
    public void deselect() {
    	_s.trainAction(myT, "deselect");
    }
    
    /**
     * Called when the train is selected. May override but be sure to call super.
     *
     */
    public void select() {
    	_s.trainAction(myT, "select");
    }

    /**
     * This method should be overriden, but ensure that super is called to update the
     * weight, speed, and length display.  This method should also update any other controls.
     * This method is called in the AWT Event loop when {@link #process} says so
     */
	public void run() {
		if (myT == null) return;
		
		int ispeed = new Double(myT.vel()).intValue();
		weight.setText(NumberFormat.getInstance().format(myT.weight()) + " Tons");
		speed.setText(Integer.toString(ispeed) + " MPH");
		length.setText(NumberFormat.getInstance().format(myT.length().feet()) + " Feet");
		
	}
	
	/**
	 * Perform any processing needed before the display is to be updated.
	 * Ensure that super is called.
	 * This method is called every step in the game loop
	 * No GUI manipulation in this one, but if you changed something
	 * return true so that {@link #run()} will be called to update the display
	 * 
	 *@return true if the GUI needs to be updated
	 */
	public boolean process() { 
		if (myT != null)
			carsLU(); 
		
		return false; 
	}
	
	
	/**
	 * Request to load all cars that can be loaded.
	 * Loads all cars listed in lable
	 */
	public void load() {
		if (_s.trainAction(myT, "load")) return;
		
		for (int i = 0; i < lable.length; i++) {
			
			lable[i].load();	
		}
		fillConsist();
		
		carsLU();
	}
	
	
	/**
	 * Request to unload all cars that can be loaded.
	 * Unloads all cars listed in ulable
	 */
	public void unload() {
		if (_s.trainAction(myT, "unload")) return;
		
		for (int i = 0; i < ulable.length; i++) {
			
			ulable[i].unload();	
		}
		fillConsist();
		carsLU();
		
	}
	
	/**
	 * Request to sound the horn
	 */
	public void horn() {
		if (_s.trainAction(myT, "horn") == false)
			Sounds.horn.play();
		
	}
	
	public Object newInstance() {
		try {
			return getClass().newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;	
	
	}
	
	
	
	
	private void carsLU() {
		
		
		// check each car to see if it
		// is on a LUSegment that can load/unload it
		Car[] a = myT.array();
		
		Iterator<RailSegment> rsi;
		
		LUSegment lr;		

		Car[] la = new Car[a.length];
		Car[] ula = new Car[a.length];
		int li = 0;
		int uli = 0;

		for (int i = 0; i < a.length; i++) {
			rsi = a[i].segs().iterator();
			
			while (rsi.hasNext()) {
				
				try {
					lr = (LUSegment)rsi.next();
					
					if (lr.canLU(a[i])) {
						if (a[i].loaded())	
							ula[uli++] = a[i];
						else
							la[li++] = a[i];
						break;
					}
					
				} catch (ClassCastException e) { }
				
			}
			
		}
		lable = new Car[li];
		System.arraycopy(la, 0, lable, 0, li);
		
		ulable = new Car[uli];
		System.arraycopy(ula, 0, ulable, 0, uli);

		
		
	}

}
