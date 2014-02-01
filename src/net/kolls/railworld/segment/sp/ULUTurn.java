package net.kolls.railworld.segment.sp;


import java.awt.Color;
import java.util.Map;

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
 * Unload, load, and reverse
 * 
 * @author Steve Kollmansberger
 */
public class ULUTurn extends AbstractUL {

	private Color col = Color.red;
	
	@Override
	protected void FinishedUL(Train t) {
		t.reverse = true;
		col = Color.green;
		t.getController().process();
		col = Color.red;

	}

	

	@Override
	public Icon icon() {
		return Images.sp_uluturn;
	}

	

	@Override
	public String toString() {
		return "Unload/Load Reverse";
	}

	@Override
	public Object newInstance() {
		return new ULUTurn();
	}

	@Override
	public Color status() {
		return col;
	}

}
