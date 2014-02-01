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
 * A yellow (slow, 5 MPH) signal
 * 
 * @author Steve Kollmansberger
 *
 */
public class Yellow implements SignalProgram {

	public void enter(Train t) { }

	public void reacting(Train t) {	}

	public Color status() {
		return Color.yellow;
	}

	@Override
	public String toString() {
		return "5 MPH";
	}
	public Icon icon() {
		
		return Images.sp_yellow;
		
		
	}
	

	public void load(Map<String, String> data) { }

	public Map<String, String> save() {	return null; }
	
	public Object newInstance() { return new Yellow(); }
}
