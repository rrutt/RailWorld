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



import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.kolls.railworld.*;
import net.kolls.railworld.segment.Crossing;
import net.kolls.railworld.segment.Signal;


import javax.swing.*;



/**
 * Automatic train control.  This control looks ahead for end of track or red or yellow signal
 * and applies the brakes when needed to stop in advance of it.  It also sounds
 * the horn when a crossing is ahead. If it is stopped at a signal, and the
 * signal is changed, it will resume desired speed.  If it is slowed by a yellow signal,
 * it will return to desired speed if it approaches a green signal.
 * 
 * @author Steve Kollmansberger
 */
@SuppressWarnings("serial")
public class AutoControl extends TrainControl {
		
	
	/**
	 * Set if train desired info should be sync'd on selection.  True by default.
	 * 
	 * @param syncToClick If true, when the train is selected, the desired throttle and brake will be read from the train's current status.
	 */
	public void setSyncToClick(boolean syncToClick) {
		sc = syncToClick;
	}
	
	/**
	 * Create a new Auto controller.
	 *  
	 */
	public AutoControl() {
		super();
	
		_myinfo = new JLabel();
	
		hasSoundedHorn = false;
		modify = false;
		sc = true;
		
	}
	
	private JLabel _myinfo;
	
	@Override
	public void attach(Train t) {
		super.attach(t);
		add(new JLabel("Auto Controlled"));
		add(_myinfo);
		add(Box.createVerticalGlue());
	}

	/**
	 * Update the custom entry to say something.
	 * 
	 * @param s
	 */
	public void setMyInfo(String s) {
		_myinfo.setText(s);
	}
    
	private boolean sc;
	private int desiredThrottle;
	private boolean desiredBrake;
	private boolean modify; // under auto modification, so don't sync throttle
	
    
    @Override
	public void select() { super.select(); if (sc) modify = false; selected = true; }

    @Override
	public void deselect() { selected = false; }
    
    private boolean selected;
    private boolean hasSoundedHorn;
    
    public AutoControl newInstance() {
    	AutoControl ac = new AutoControl();
    	ac.setMyInfo(_myinfo.getText());
    	return  ac;
    }
    
    @Override
	public boolean process() {
		if (myT == null) return false;
	
		
		
		
		super.process();
		
		

		if (modify == false) {
			desiredThrottle = myT.getThrottle();
			desiredBrake = myT.getBrake();
		}
		
		
		RailSegment r, orig;

		// speed to stop PLUS the amount we move forward per step 
		// otherwise we won't see the end in time
		double maxfeet = Math.round( myT.feetToSlow(0) + GameLoop.feetPerStepSpeed(myT.vel()));
		double maxyfeet = Math.round( myT.feetToSlow(5) + GameLoop.feetPerStepSpeed(myT.vel()));
		double feet;
		double yfeet = 0;
		
		orig = myT.pos.r; // current segment
		ArrayList<RailSegment> fsegs = myT.pos.r.destNZ(myT.pos.orig);
		r = fsegs.get(fsegs.size() - 1);


		
		
		if ((r instanceof Crossing && myT.pos.per > 0.1) || orig instanceof Crossing) {
			if (!hasSoundedHorn && myT.hasEngine()) {
				horn();
				
			}
			hasSoundedHorn = true;
		} else hasSoundedHorn = false;
		
		
		feet = myT.pos.r.length().feet() * (1.0 - myT.pos.per); // length remaining in segment
		feet -= 15; // stop 15 feet short of end
	
		// reset so r and orig go together
		r = myT.pos.r.dest(myT.pos.orig);
		// search for the end of the track
		while (r != null && feet <= maxfeet) {
	
			if (r instanceof Signal) {
				Signal s = ((Signal)r); 
				
				
				if (orig == s.getDest(Signal.POINT_BEGIN)) {
					
					((Signal)r).sp.reacting(myT);
				
					if (((Signal)r).sp.status().equals(Color.red)) break;
					if (((Signal)r).sp.status().equals(Color.yellow) && yfeet == 0) yfeet = feet;
				}
				
				
			}
			
			feet += r.length().feet();
			RailSegment tmp = r;
			r = r.dest(orig);
			orig = tmp;
			
			
		}

		
		
		if (feet <= maxfeet && myT.vel() > 0) {
			// reached STOP before reaching our max lookahead 
			modify = true;
			myT.setThrottle(0);
			myT.setBrake(true);
			
			
			
			return selected;
		}
		
		
		if (yfeet != 0 && yfeet < maxyfeet) {
			// approaching yellow signal
			modify = true;
			
			if (myT.vel() > 6) {
				myT.setThrottle(0);
				myT.setBrake(true);
				return selected;
			} 
				
			myT.setThrottle(1);
			myT.setBrake(desiredBrake);	
			
		}
		
		
		
		
		double d = myT.pos.r.length().feet() * (1.0 - myT.pos.per);
		r = myT.pos.r.dest(myT.pos.orig); // next segment
		orig = myT.pos.r;
		
		
		// when 40 feet to signal, check
		while (modify && d <= 40 && r != null) {
				
			
			if (r instanceof Signal) {
				Signal s = (Signal)r;
				if (s.getDest(Signal.POINT_BEGIN) == orig) {
					s.sp.reacting(myT);
					if (s.sp.status().equals(Color.yellow) && desiredThrottle > 0) {
						if (myT.vel() == 0) {
							horn();
						}
						modify = true;
						myT.setThrottle(1);
						myT.setBrake(desiredBrake);
						return selected;
					}
					if (s.sp.status().equals(Color.green)) {
						if (myT.vel() == 0 && desiredThrottle > 0) {
							horn();
						}
						myT.setThrottle(desiredThrottle);
						myT.setBrake(desiredBrake);
						modify = false;
						return selected;
					}
					break;
				}
			}
			
			d += r.length().feet();
			RailSegment tmp = r; 
			r = r.dest(orig); // next segment
			orig = tmp;
		}
		
		
		
		return selected;
		

	}

    
    public void load(Map<String, String> data) {
    	sc = Boolean.parseBoolean(data.get("sc"));
    	desiredThrottle = Integer.parseInt(data.get("desiredThrottle"));
    	desiredBrake = Boolean.parseBoolean(data.get("desiredBrake"));
    	modify = Boolean.parseBoolean(data.get("modify"));
    	_myinfo.setText(data.get("myinfo"));    	
		
	}
	public Map<String, String> save() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("sc", Boolean.toString(sc));
		data.put("desiredBrake", Boolean.toString(desiredBrake));
		data.put("modify", Boolean.toString(modify));
		data.put("desiredThrottle", Integer.toString(desiredThrottle));
		data.put("myinfo", _myinfo.getText());
		
		
		return data;
	}
    
	@Override
	public String toString() { return "AutoControl"; }
    

}
