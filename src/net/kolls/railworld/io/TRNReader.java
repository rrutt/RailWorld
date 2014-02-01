package net.kolls.railworld.io;

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


import net.kolls.railworld.*;
import net.kolls.railworld.car.*;

import java.io.*;
import java.util.*;


/**
 * Reads Yard Duty TRN files to create a train.  All information is available except which entrance to use.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TRNReader {
	
	static final int SWITCHER = 1; // engine
	static final int ROADPOWER = 2; // engine
	static final int CABOOSE = 3;
	static final int PASSENGER = 4;
	static final int WELLCAR = 5;
	static final int SPINECAR = 6;
	static final int AUTORACK = 7;
	static final int WOODCHIP = 8;
	static final int BOXCAR = 9;
	static final int REEFER = 10;
	static final int COALCAR = 11;
	static final int TANKCAR = 12;
	static final int GRAINHOPPER = 13;
	static final int OPENHOPPER = 14;
	static final int GONDOLA = 15;
	static final int OREJENNIE = 16;
	static final int FLATCAR = 17;
	static final int CENTERBEAM = 18;
	static final int INTERMODAL = 19;
	static final int STOCKCAR = 20;
	static final int STEELCOIL = 21;
	static final int BULKHEADFLAT = 22;
	static final int HEAVYFLAT = 23;
	static final int COVEREDHOPPER = 24;
	static final int ROADRAILER = 25; // ??
	static final int LARGETANK = 26;
	static final int SHORTCOVEREDHOPPER = 27;
	static final int HCABOXCAR = 28;
	
	
	private static Car createCar(int typeID, boolean loaded) 
	{
		
		Car c;
		switch (typeID) {
		case SWITCHER:
		case ROADPOWER:
			c = new Engine();
			break;
		case CABOOSE:
			c = new Caboose();
			break;
		case PASSENGER:
			c = new Passenger();
			break;
		case WELLCAR:
		case SPINECAR:
		case INTERMODAL:
			c = new Intermodal();
			break;
		case AUTORACK:
			c = new Autorack();
			break;
		case WOODCHIP:
		case COALCAR:
		case OPENHOPPER:
		case GONDOLA:
		case OREJENNIE:
			c = new Openhopper();
			break;
		case BOXCAR:
		case REEFER:
		case HCABOXCAR:
			c = new Boxcar();
			break;
		case TANKCAR:
		case LARGETANK:
			c = new Tankcar();
			break;
		case GRAINHOPPER:
		case COVEREDHOPPER:
		case SHORTCOVEREDHOPPER:
		case STEELCOIL:
			c = new Coveredhopper();
			break;
		case FLATCAR:
		case CENTERBEAM:
		case BULKHEADFLAT:
		case HEAVYFLAT:
			c = new Flatcar();
			break;
		case STOCKCAR:
			c = new Stockcar();
			break;
		default:
			c = new Boxcar();
			System.out.println("Unknown car type: "+typeID);
	
		
		}
		
		if (c.isLoadable()) {
			if (loaded) c.load(); else c.unload();
		}
		return c;
	}

	/**
	 * Read a train from a Yard Duty train file
	 * 
	 * @param f The file to read from
	 * @return The train created.
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 * 
	 */
	public static Train read(File f) throws FileNotFoundException, IOException {
		Vector<Car> cs = new Vector<Car>();
		
		Train t;
		
		
		
		BufferedReader br; 
		StringTokenizer st;
		String l, t0, t1;
		
		boolean reversed = false;
		double spd = 20;
		int throttle = Train.MAX_THROTTLE;
		boolean brake = false;
		int ti = -1, lu;
		
		br = new BufferedReader(new FileReader(f));
		
		while (br.ready()) {

			l = br.readLine();
			if (l.startsWith("[")) continue;
			
			st = new StringTokenizer(l,"=",false);

			if (st.countTokens() != 2) continue;
			t0 = st.nextToken();
			t1 = st.nextToken();
			
			if (t0.equals("UseExit")) {
				if (Integer.parseInt(t1) < 0) reversed = true;
			}
			if (t0.equals("Speed")) {
				st = new StringTokenizer(t1,":",false);

				spd = Double.parseDouble(st.nextToken());
			
			}
			if (t0.equals("ThrottleSetting")) {
				throttle = Integer.parseInt(t1);
			}
			if (t0.equals("BrakeSetting")) {
				if (Integer.parseInt(t1) > 4) brake = true;
			}
			
			if (t0.equals("TypeID")) {
				st = new StringTokenizer(t1,":",false);
				ti = Integer.parseInt(st.nextToken());
			}
			if (t0.equals("IsLoaded")) {
				lu = Integer.parseInt(t1);
				// loaded if lu odd
				Car mc = createCar(ti, lu % 2 == 1);
				if (reversed) cs.add(0, mc); else cs.add(mc);
				ti = -1;
			}

			
		}
		
		br.close();
		
		t = new Train(cs.toArray(new Car[0]));
		t.setVel(spd);
		if (t.hasEngine())
			t.setThrottle(throttle);
		t.setBrake(brake);

		return t;
		
	}
}
