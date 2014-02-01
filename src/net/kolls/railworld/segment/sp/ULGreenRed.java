package net.kolls.railworld.segment.sp;

import java.awt.Color;

import javax.swing.Icon;

import net.kolls.railworld.Images;
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
 * Unloads, loads, then continues.  Resets to red
 * after train leaves for next train.
 */
public class ULGreenRed extends AbstractUL {

	private Color col = Color.red;
	
	@Override
	protected void FinishedUL(Train t) {
		col = Color.green;
	}
	
	@Override
	public void enter(Train t) { col = Color.red; }

	@Override
	public Icon icon() {
		return Images.sp_ulgreenred;
	}

	@Override
	public Color status() {
		return col;
	}

	@Override
	public Object newInstance() {
		return new ULGreenRed();
	}

	@Override
	public String toString() {
		return "Unload/Load Proceed";
	}
}
