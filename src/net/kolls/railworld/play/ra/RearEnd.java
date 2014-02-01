package net.kolls.railworld.play.ra;

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


import java.awt.geom.Point2D;

import net.kolls.railworld.Train;
import net.kolls.railworld.play.RailAccident;

/**
 * One train smashes into the end of another train.
 * 
 * @author Steve Kollmansberger
 *
 */
public class RearEnd extends RailAccident {

	/**
	 * Simply calls super constructor.
	 * 
	 * @param first First train
	 * @param second Second train
	 * @param p Location of accident
	 */
	public RearEnd(Train first, Train second, Point2D p) {
		super(first, second, p);
	}

	@Override
	public String midbody() {
		return "rear-ended a";
	}

	@Override
	public String title() {
		return "REAR-END COLLISION";
	}

}
