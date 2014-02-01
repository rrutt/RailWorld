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
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Icon;

import net.kolls.railworld.Images;
import net.kolls.railworld.SignalProgram;
import net.kolls.railworld.Train;

/**
 * A signal which allows one train at full speed, then turns red.
 * 
 * @author Steve Kollmansberger
 *
 */
public class GreenRed implements SignalProgram {

	private Color col = Color.green;
	
	public void enter(Train t) { col = Color.red; }

	public void reacting(Train t) {	}

	public Color status() {
		return col;
	}

	@Override
	public String toString() {
		return "Proceed One Train";
	}
	
	public Icon icon() {
		
		return Images.sp_greenred;
		
		
	}
	

	public void load(Map<String, String> data) { 
		col = new Color(Integer.parseInt(data.get("color")));
	}

	public Map<String, String> save() {	
		Hashtable<String, String> h = new Hashtable<String, String>();
		h.put("color", Integer.toString(col.getRGB()));
		return h;
	}
	
	public Object newInstance() { return new GreenRed(); }

}
