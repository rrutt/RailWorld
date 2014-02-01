package net.kolls.railworld.segment.sp;

import java.util.Date;
import java.util.Map;

import net.kolls.railworld.SignalProgram;
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
 * Unloads, loads, and then performs some action
 * 
 * @author Steve Kollmansberger
 */
public abstract class AbstractUL implements SignalProgram {

	private int stage = 0;
	private Date current;
	
	@Override
	public void reacting(Train t) {
		if (t.vel() == 0) {
			switch (stage) {
			case 0:
				current = new Date();
				stage = 1;
				break;
			case 1:
				if ( (new Date()).getTime() - current.getTime() > 1000) {
					t.getController().unload();
					stage = 2;
					current = new Date();
				}
				break;
			case 2:
				if ( (new Date()).getTime() - current.getTime() > 1000) {
					t.getController().load();
					stage = 3;
					current = new Date();
				}
				break;
			case 3:
				if ( (new Date()).getTime() - current.getTime() > 500) {
					stage = 0;
					FinishedUL(t); // do whatever
				}
				break;
			
			};
			
		
		} else stage = 0;
		
		
	}
	
	protected abstract void FinishedUL(Train t);
	
	// default implementations
	
	@Override
	public void enter(Train t) { }
	
	
	@Override
	public void load(Map<String, String> data) { }
	
	@Override
	public Map<String, String> save() {	
		return null;
	}
}
