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
import net.kolls.railworld.segment.*;

import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.geom.*;


// ... d1 
// L10=702:1595:729:1622:0:9:61:11:10:2:0:0~ idx
// L11=729:1622:787:1680:0:10:11:12:11 d3
// L61=702:1595:679:1567:0:10:61:62:61:0 d2
// switch(begin=d1, end1=idx, end2=d2

/**
 * Reads Yard Duty (YRD) files.  Note that there is not an exact correspondence between Yard Duty
 * and Rail World, so some approximation is done.  Reads file format version 1.7
 */
public class YardReader {

	static final int PASSENGER = 16;
	static final int WELL = 32;
	static final int SPINE = 64;
	static final int AUTORACK = 128;
	static final int WOODCHIP = 256;
	static final int BOXCAR = 512;
	static final int REEFER = 1024;
	static final int COALCAR = 2048;
	static final int TANKCAR = 4096;
	static final int GRAINHOP = 8192;
	static final int OPENHOP = 16384;
	static final int GONDOLA = 32768;
	static final int OREJENNIE = 65536;
	static final int FLATCAR = 131072;
	static final int CFLATCAR = 262144;
	static final int INTERMODAL = 524288;
	static final int LIVESTOCK = 1048576;
	static final int COILCAR = 2097152;
	static final int BULKHEADFLAT = 4194304;
	static final int HEAVYFLAT = 8388608;
	static final int COVEREDHOP = 16777216;


	static final int HIDDEN = 1;
	static final int MAINLINE = 2;
	static final int UNLOADER = 4;
	static final int TURNTABLE = 8;
	static final int CROSSING = 16;
	static final int HUMP = 32;
	static final int LOADER = 64;
	static final int FUEL = 128;
	static final int WASH = 256;
	static final int REPAIR = 512;
	static final int INSPECT = 1024;
	static final int RETARDER = 2048;
	static final int VEHICLE = 4096;

	private static boolean flag(int val, int flag) {
		return (val & flag) == flag;
	}

	
	private static Car[] Cars(int luflags, boolean load, boolean unload) {
		
		Car[] oc = new Car[20];
		int c = 0;

		if (flag(luflags, AUTORACK)) {
			if (load) { oc[c] = new Autorack(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Autorack(); oc[c].unload(); c++; }
		}

		if (flag(luflags, COALCAR) || flag(luflags, OPENHOP) || flag(luflags, GONDOLA) || flag(luflags, WOODCHIP)  || flag(luflags, OREJENNIE)) {
			if (load) { oc[c] = new Openhopper(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Openhopper(); oc[c].unload(); c++; }
		}
		if (flag(luflags, COVEREDHOP) || flag(luflags, COILCAR) || flag(luflags, GRAINHOP)) {
			if (load) { oc[c] = new Coveredhopper(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Coveredhopper(); oc[c].unload(); c++; }
		}
		if (flag(luflags, FLATCAR)  || flag(luflags, CFLATCAR)  || flag(luflags, BULKHEADFLAT)  || flag(luflags, HEAVYFLAT)) {
			if (load) { oc[c] = new Flatcar(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Flatcar(); oc[c].unload(); c++; }
		}
		if (flag(luflags, INTERMODAL) || flag(luflags, SPINE)  || flag(luflags, WELL)) {
			if (load) { oc[c] = new Intermodal(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Intermodal(); oc[c].unload(); c++; }
		}
		if (flag(luflags, PASSENGER)) {
			if (load) { oc[c] = new Passenger(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Passenger(); oc[c].unload(); c++; }
		}
		if (flag(luflags, LIVESTOCK)) {
			if (load) { oc[c] = new Stockcar(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Stockcar(); oc[c].unload(); c++; }
		}
		
		if (flag(luflags, TANKCAR)) {
			if (load) { oc[c] = new Tankcar(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Tankcar(); oc[c].unload(); c++; }
		}
		
		
		
		// if we don't know what it is, we'll just make it a boxcar
		if (flag(luflags, BOXCAR) || flag(luflags, REEFER) || c == 0) {
			if (load) { oc[c] = new Boxcar(); oc[c].load(); c++; }
			if (unload) { oc[c] = new Boxcar(); oc[c].unload(); c++; }
			if (!(flag(luflags, BOXCAR) || flag(luflags, REEFER))) System.out.println("Unknown LU flags: "+luflags);
		}

		Car[] nc = new Car[c];
		System.arraycopy(oc, 0, nc, 0, c);

		return nc;

		

	}
	
	/**
	 * Reads a valid Yard Duty file.  Note that the format is not well enough understood to throw
	 * appropriate format errors.
	 * 
	 * @param f File to read from.
	 * @param toBeFilled MetaData to fill.
	 * @return An array of rail segments.
	 * @throws IOException If the file can't be opened.
	 */
	public static RailSegment[] read(File f, MetaData toBeFilled) throws IOException {
		Vector<RailSegment> la = new Vector<RailSegment>();
		RailSegment ridx, rd1, rd2, rd3, rd4, rsix;
		
		toBeFilled.comment = "Imported from Yard Duty";
		toBeFilled.feetPerPixel = 3;
		toBeFilled.zoom = 1;
		toBeFilled.imgfile = f.getName().substring(0, f.getName().length() - 3) + "jpg"; // same file name, different ext
		toBeFilled.ourFile = f;
//		 set default distances
		Distance.feetPerPixels = 3.0;
		RailCanvas.zoom = 1.0;
		
		
	
	
		BufferedReader br; 
            	

	

		String l;
		int x;
		float x1 = 0,y1 = 0,x2 = 0,y2 = 0;
		int cnt = 0;
		int idx = 0;
		int i;
		int d1 = 0,d2 = 0,d3 = 0,d4 = 0, flags = 0, es = 0;
		int cf = 0;		

		boolean go;

		StringTokenizer st;
		
		for (int z = 0; z < 3;z++) {
			br = new BufferedReader(new FileReader(f));
			go = false;
			while (br.ready()) {

			l = br.readLine();
			if (l.equals("[Lines]")) {go = true; continue; }

			if (go == false) {
				// in the header area
				if (l.startsWith("YardDuty=")) {
					toBeFilled.author = l.substring(9);
				}
				if (l.startsWith("YardName=")) {
					toBeFilled.title = l.substring(9);
				}
				continue;
			}
			
			
			
			i = l.indexOf('=');
			if (i < 1) continue;

			idx = Integer.parseInt(l.substring(1,i)); // L#=


			st = new StringTokenizer(l.substring(i+1),":",false);
	
		    while (st.hasMoreTokens()) {
				try {
					x = (int) Float.parseFloat(st.nextToken());
				} catch (Exception e) { cnt++; continue; }
				
				switch (cnt) {
				case 0:
					x1 = x;
					break;
				case 1:
					y1 = x;
					break;
				case 2:
					x2 = x;
					break;
				case 3:
					y2 = x;
					break;
				case 4:
					es = x;
				case 5:
					d1 = x;
					break;
				case 6:
					d2 = x;
					break;
				case 7:
					d3 = x;
					break;
				case 8:
					d4 = x;
					break;
				case 9:
					flags = x;
					break;
				case 10:
					cf = x;
					break;
				default:
				}	
				cnt++;
			}
			
		    cnt = 0;
		    

		    
		    // check to see if this one is a label
		    i = l.lastIndexOf(':');
		    String lbl = l.substring(i);
		    if (lbl.length() > 3) {
		    	// it is a label
		    	// woot

		    	
		    	
		    	
		    	if (z == 0) {
			    	if (idx > la.size()) la.setSize(idx);
			    	
		    		Distance sz = null;
		    		Color c = null;
		    		double angle = 0;
		    		
		    		
		    		
		    		
		    		
		    		
		    		
		    		int cls = Integer.parseInt(lbl.substring(1,2));
		    		if (cls >= 10) {
		    			cls -= 10;
		    			angle = Math.PI / 2.0;
		    		}
		    		switch (cls) {
		    		case 0:
		    			// building
		    			sz = new Distance(30, Distance.Measure.FEET);
		    			c = Color.yellow;
		    			break;
		    		case 1:
		    			// track
		    			sz = new Distance(30, Distance.Measure.FEET);
		    			c = Color.white;
		    			break;
		    		case 2:
		    			// street
		    			sz = new Distance(30, Distance.Measure.FEET);
		    			c = Color.cyan;
		    			break;
		    		case 3:
		    			// area
		    			sz = new Distance(50, Distance.Measure.FEET);
		    			c = Color.yellow;
		    			break;
		    		case 4:
		    			// city
		    			sz = new Distance(70, Distance.Measure.FEET);
		    			c = Color.white;
		    			break;
		    		case 5:
		    			// railroad
		    			sz = new Distance(30, Distance.Measure.FEET);
		    			c = Color.red;
		    			break;
		    		}
		    		
		    		
		    		
		    		Point2D pos = new Point2D.Double( (x1+x2)/2, (y1+y2)/2);
		    		
		    		Label ls = new Label(lbl.substring(3), sz, c, pos, angle);
		    		ls.centered = false;
		    		la.add(idx, ls);
		    		
		    	} 
		    	continue;
		    }
		    
		    
		    
		    
		    
			
		    // abort if road
		    if (flag(flags, VEHICLE)) continue;
		    
			
			
 
			if (z==0) {
				if (idx > la.size()) la.setSize(idx);

				Line2D myline = new Line2D.Float(x1,y1,x2,y2);
			
				if (flag(flags, HIDDEN) && ( flag(flags, LOADER) || flag(flags, UNLOADER) ) && cf != 0) {
					la.add(idx, new HiddenLUSegment(null, null, myline, Cars(cf, flag(flags, UNLOADER), flag(flags, LOADER)), false) );
				} else
				if (flag(flags, HIDDEN)) {
					la.add(idx, new HiddenSegment(null, null, myline));
				} else
				if ((flag(flags, LOADER) || flag(flags, UNLOADER)) && cf != 0) {
					// 4 = unloader
					// 64 = loader
					// an unloader accepts loaded cars
					// vice verse
					la.add(idx, new LUSegment(null, null, myline, Cars(cf, flag(flags, UNLOADER), flag(flags, LOADER) ), false ));
				} else
				if (flag(flags, CROSSING)) {
				
					la.add(idx, new Crossing(null, null, myline));
				} else
				if (flags == 0 || flags == 2) {

					if (es > 0)
						la.add(idx, new EESegment(null, null, myline, "#"+Integer.toString(es)));
					else
						la.add(idx, new TrackSegment(null, null, myline));
				} else {
					la.add(idx, new TrackSegment(null, null, myline));
					//System.out.println("Unknown flags: "+flags);
				}
				
			}
			
			if (z==1) {
				ridx = la.get(idx);
				rd1 = la.get(d1);
				rd3 = la.get(d3);
				

				if (idx != d1) {
					((TrackSegment)ridx).setDest(TrackSegment.POINT_BEGIN, true, rd1);
					if (ridx instanceof LUSegment) {
						((LUSegment)ridx).setDrawAccept((rd1 instanceof LUSegment) == false);
					}
				}

				if (idx != d3)
				((TrackSegment)ridx).setDest(TrackSegment.POINT_END, true, rd3);
			}
			if (z==2) {
				ridx = la.get(idx);
				rd1 = la.get(d1);
				rd2 = la.get(d2);
				rd3 = la.get(d3);
				rd4 = la.get(d4);

				if (d2 != idx) {
					// create switch
					int six = la.size() + 1;
					la.setSize(six);
					
					rsix = new Switch(ridx, rd1, rd2, new Point2D.Float(x1,y1));
					
					la.add(six, rsix);

					rd1.update(ridx, rsix);
					
					ridx.update(rd1, rsix);
		
					
					rd2.update(ridx, rsix); // weirdness
					
					
				}
				if (d4 != idx) {
					// create switch
					int six = la.size() + 1;
					la.setSize(six);
					
					rsix = new Switch(ridx, rd3, rd4, new Point2D.Float(x2,y2));
					
					la.add(six, rsix);

					rd3.update(ridx, rsix);
					
					ridx.update(rd3, rsix);
		
					
					rd4.update(ridx, rsix); 
					
					
				}
				
			}
			


			
			
			}
			br.close();
		}
        	
        	
	
	
	
	return (la.toArray(new RailSegment[1]));

	}

	

}
