package net.kolls.railworld.scripts;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Map;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Sounds;
import net.kolls.railworld.Train;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.segment.Switch;

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
 * Modified all switches in the game to be "spring" switches.
 * These return to their original position after being over-run by a train.
 */
public class SpringSwitches implements Script {

	private PlayFrame pf;
	
	
	public RailSegment[] modifySegments(RailSegment[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] instanceof Switch) {
				System.out.println(lines[i].trains().size());
				lines[i] = new SpringSwitch( (Switch)lines[i]);
				System.out.println(lines[i].trains().size());
				System.out.println("**");
			}
		}
		return lines;
	
	}
	
	@Override
	public void init(PlayFrame pf) { 
		this.pf = pf;	
	
		
		

	}

	@Override
	public boolean playFrameAction(String action) {
		return false;
	}

	@Override
	public void load(Map<String, String> data) {
		
		for (int i = 0; i < pf.jdb.la.length; i++) {
			if (pf.jdb.la[i] instanceof SpringSwitch) {
				
				if (!data.get(Integer.toString(i)).equals("null")) {
				
				
					((SpringSwitch)pf.jdb.la[i]).orig_flipped =
						Boolean.parseBoolean(data.get(Integer.toString(i)));
					
					
				}
				
			}
		}
		
		
		
	}

	@Override
	public Object newInstance() {
		return new SpringSwitches();
	}

	@Override
	public Map<String, String> save() {
		Hashtable<String, String> s = new Hashtable<String, String>();
		
		for (int i = 0; i < pf.jdb.la.length; i++) {
			if (pf.jdb.la[i] instanceof SpringSwitch) {
				Boolean b = ((SpringSwitch)pf.jdb.la[i]).orig_flipped;
				s.put(Integer.toString(i), 
						b == null ? "null" : Boolean.toString(b));
			}
		}
		
		return s;
	}
	
	@Override
	public String toString() {
		return "Spring Switches";
	}
	
	@Override
	public boolean onByDefault() {
		return true;
	}
	
	/**
	 * A switch overriding to "spring back" to its original
	 * setting after use.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class SpringSwitch extends Switch {
		
		/**
		 * While a train is passing over, this value records
		 * the original position of the switch
		 * null means no train is on the switch
		 */
		public Boolean orig_flipped;
		
		public SpringSwitch(Switch s) {
			super(s.getDest(Switch.POINT_BEGIN), s.getDest(Switch.POINT_END1), s.getDest(Switch.POINT_END2), s.getPoint(0));
			
			getDest(Switch.POINT_BEGIN).update(s, this);
			getDest(Switch.POINT_END1).update(s, this);
			getDest(Switch.POINT_END2).update(s, this);
			
			orig_flipped = null;
		}
		
		@Override
		public void enter(Train t) {
			if (orig_flipped == null)
				orig_flipped = flipped;
			
			super.enter(t);
			
		}
		
		@Override
		public void draw(int z, Graphics2D gc) {
			if (trains.size() == 0 && 
					orig_flipped != null && 
					flipped != orig_flipped) {
				
				
				
				flipped = orig_flipped;
				orig_flipped = null;
				Sounds.switchd.play();
			}
				
			if (trains.size() == 0)
				orig_flipped = null;
			
			super.draw(z, gc);
		}
		
		
	}

}
