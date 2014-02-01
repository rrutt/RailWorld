package net.kolls.railworld.segment.sp;

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
import java.util.Map;

import javax.swing.Icon;

import net.kolls.railworld.Images;
import net.kolls.railworld.SignalProgram;
import net.kolls.railworld.Train;

/**
 * A signal which stops trains and sends them back (reversed) the way they came.
 * 
 * @author Steve Kollmansberger
 *
 */
public class RedUTurn implements SignalProgram {

	// needed for a split-second green to get train moving again
	private Color col = Color.red;
	
	public void enter(Train t) { }

	public void reacting(Train t) {	
		if (t.vel() == 0 && col == java.awt.Color.red) {
			t.reverse = true;
			col = java.awt.Color.green;
			t.getController().process();
			col = java.awt.Color.red;

		}
		
	}

	public Color status() {
		return col;
	}

	@Override
	public String toString() {
		return "Reverse";
	}
	
	public Icon icon() {
		
		return Images.sp_reduturn;
		
		
	}
	

	public void load(Map<String, String> data) { }

	public Map<String, String> save() {	return null; }
	
	public Object newInstance() { return new RedUTurn(); }

}
