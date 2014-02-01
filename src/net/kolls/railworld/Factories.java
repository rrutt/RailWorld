package net.kolls.railworld;

import net.kolls.railworld.car.Autorack;
import net.kolls.railworld.car.Boxcar;
import net.kolls.railworld.car.Caboose;
import net.kolls.railworld.car.Coveredhopper;
import net.kolls.railworld.car.Engine;
import net.kolls.railworld.car.Flatcar;
import net.kolls.railworld.car.Intermodal;
import net.kolls.railworld.car.Openhopper;
import net.kolls.railworld.car.Passenger;
import net.kolls.railworld.car.Stockcar;
import net.kolls.railworld.car.Tankcar;
import net.kolls.railworld.segment.sp.Green;
import net.kolls.railworld.segment.sp.GreenRed;
import net.kolls.railworld.segment.sp.Red;
import net.kolls.railworld.segment.sp.RedUTurn;
import net.kolls.railworld.segment.sp.ULGreenRed;
import net.kolls.railworld.segment.sp.ULUTurn;
import net.kolls.railworld.segment.sp.Yellow;
import net.kolls.railworld.segment.sp.YellowRed;
import net.kolls.railworld.tc.AutoControl;
import net.kolls.railworld.tc.MixControl;
import net.kolls.railworld.tc.UserControl;

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
 * Factories for various types.
 * 
 * @author Steve Kollmansberger
 */
public class Factories {

	/**
	 * An extensible factory for all car types.
	 */
	public static ExtensibleFactory<Car> cars;
	
	/**
	 * An extensible factory for all signal programs.
	 */
	public static ExtensibleFactory<SignalProgram> sps;
	
	/**
	 * An extensible factory for all train controllers.
	 */
	public static ExtensibleFactory<TrainControl> controllers;
	
	
	
	/**
	 * Create the extensible factories.
	 * Add all built-in known types to the extensible factories.
	 */
	public static void init() {
		cars = new ExtensibleFactory<Car>();
		cars.addType( (new Engine()));
		cars.addType( (new Caboose()));
		cars.addType( (new Boxcar()));
		cars.addType( (new Openhopper()));
		cars.addType( (new Coveredhopper()));
		cars.addType( (new Passenger()));
		cars.addType( (new Tankcar()));
		cars.addType( (new Flatcar()));
		cars.addType( (new Intermodal()));
		cars.addType( (new Autorack()));
		cars.addType( (new Stockcar()));
		
		sps = new ExtensibleFactory<SignalProgram>();
		sps.addType(new Green());
		sps.addType(new Yellow());
		sps.addType(new Red());
		sps.addType(new GreenRed());
		sps.addType(new YellowRed());
		sps.addType(new RedUTurn());
		sps.addType(new ULUTurn());
		sps.addType(new ULGreenRed());
		
		
		controllers = new ExtensibleFactory<TrainControl>();
		controllers.addType(new UserControl());
		controllers.addType(new AutoControl());
		controllers.addType(new MixControl());
		
		
		
		
	}
	
	
}
