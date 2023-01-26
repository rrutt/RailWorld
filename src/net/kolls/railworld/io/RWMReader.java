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


import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import net.kolls.railworld.Car;
import net.kolls.railworld.Distance;
import net.kolls.railworld.Factories;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.segment.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads RWM (Rail World map) files in the XML format.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("deprecation")
public class RWMReader {
	
	/**
	 * The workhorse.  Uses SAX to parse the XML and construct the rail segments.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	private static class XMLHandler extends DefaultHandler {
		
		private Vector<RailSegment> la = new Vector<RailSegment>();
		private boolean expectSegment = true;
		private boolean expectCars = false;
		private boolean linkUp = false;
		private String luType;
		private Line2D luLine;
		private int luIdx;
		private boolean luDa;
		private Vector<Car> cars;
		private MetaData _tbf;
		private boolean expectComment = false;
		
		
		@Override
		public void characters(char [] c, int st, int length){
			   
				String str = new String(c, st, length);
		       if(expectComment){
		    	   	
		            _tbf.comment += str;
		            
		       }

		   }
		
		@Override
		public void startDocument ()
	    {
			
	    }

	    @Override
		public void endDocument ()
	    {
	    	// what if last element is a lu?
	    	// then it will not be commited
	    	// check here
	    	if (expectCars) commitLU();
	    	
	    	// if we completed the first phase, move to second
	    	expectSegment = false;
	    	linkUp = true;
	    	
	    }

	    private void commitLU() {
	    	expectCars = false;
			if (luType.equals("HiddenLUSegment"))
				la.add(luIdx, new HiddenLUSegment(null, null, luLine, cars.toArray(new Car[0]), luDa ));
			else
				la.add(luIdx, new LUSegment(null, null, luLine, cars.toArray(new Car[0]) , luDa ));
	    }
	    
	    @Override
		public void startElement (String uri, String name, String qName, Attributes atts) throws SAXException {
	    	int idx, begin, end1, end2;
	    	double x1,y1,x2,y2;
	    	
	    	if (qName.equals("RailMap")) {
	    		if (atts.getValue("Version").equals("1.0") == false
	    				&& atts.getValue("Version").equals("1.1") == false
	    				&& atts.getValue("Version").equals("1.2") == false) throw new SAXException("Unsupported version: "+atts.getValue("Version"));
	    	}
	    	
	    	
	    	// handle meta elements anytime
	    	if (qName.equals("Distance")) {
	    		// MUST load Distance fpp because otherwise
	    		// the tracksegments will be incorrect
	    		// the pixels are converted into feet at "compile time" e.g. when it is loaded
	    		Distance.feetPerPixels = _tbf.feetPerPixel = Double.parseDouble(atts.getValue("FeetPerPixel"));
	    		
	    		_tbf.zoom = Double.parseDouble(atts.getValue("Zoom"));
	    		
	    	}
	    	
	    	if (qName.equals("Center")) {
	    		_tbf.centerX = Integer.parseInt(atts.getValue("X"));
	    		_tbf.centerY = Integer.parseInt(atts.getValue("Y"));
	    		
	    	}
	    	
	    	if (qName.equals("Image")) {
	    		_tbf.imgfile = atts.getValue("File");
	    	}
	    	
	    	if (qName.equals("Map")) {
	    		_tbf.author = atts.getValue("Author");
	    		_tbf.title = atts.getValue("Title");
	    		_tbf.comment = "";
	    		
	    		expectComment = true;
	    		return;
	    	}
	    	
	    	
	    	
	    	
	    	if (expectCars) {
	    		if (qName.equals("Load") || qName.equals("Unload")) {
	    			Car c = null;
	    			
	    			String cn = atts.getValue("Type");
	    			
	    			try {
	    				c = Factories.cars.createInstance(cn);	
	    			} catch (ClassNotFoundException e) {
	    				throw new SAXException("Unknown car type: "+cn);
	    			}
	    			
	    			
	    			
	    			
	    			// load means accepts unloaded, unload means accept loade
	    			if (qName.equals("Load")) c.unload();
	    			if (qName.equals("Unload")) c.load();
	    			
	    			cars.add(c);
	    			
	    			return;
	    			
	    			
	    			
	    		} else {
	    			// not a car
	    			// commit lu line
	    			// and slide into expectsegment
	    			commitLU();
	    			
	    		}
	    	}
	    	
	    	if (expectSegment) {

	    		if (atts.getValue("ID") == null) return;
	    		
	    		idx = Integer.parseInt(atts.getValue("ID"));
	    		
	    		if (idx > la.size()) la.setSize(idx);

				
	    		
	    		if (qName.equals("TrackSegment") || qName.equals("EESegment") ||
	    				qName.equals("HiddenSegment") || qName.equals("LUSegment") ||
	    				qName.equals("HiddenLUSegment") || qName.equals("Crossing")) {
	    			x1 = Double.parseDouble(atts.getValue("X1"));
	    			y1 = Double.parseDouble(atts.getValue("Y1"));
	    			x2 = Double.parseDouble(atts.getValue("X2"));
	    			y2 = Double.parseDouble(atts.getValue("Y2"));
	    			
	    			Line2D myline = new Line2D.Double(x1,y1,x2,y2);
	    			
	    			if (qName.equals("TrackSegment"))
	    				la.add(idx, new TrackSegment(null, null, myline));
	    			
	    			if (qName.equals("Crossing"))
	    				la.add(idx, new Crossing(null, null, myline));
	    			
	    			if (qName.equals("HiddenSegment"))
    					la.add(idx, new HiddenSegment(null, null, myline));
	    			
	    			if (qName.equals("EESegment")) {
	    				String lbl = atts.getValue("Label");
    					la.add(idx, new EESegment(null, null, myline, lbl));
	    			}
	    			
	    			if (qName.equals("LUSegment") || qName.equals("HiddenLUSegment")) {
	    				luType = qName;
	    				luIdx = idx;
	    				expectCars = true;
	    				luLine = myline;
	    				cars = new Vector<Car>();
	    				
	    				luDa = atts.getValue("DrawAccept").equals("Yes");
	    				
	    				// segment will be instantiated after cars are loaded
	    			}
	    			
	    			return;
	    		}
	    		
	    		if (qName.equals("Curve")) {
	    			x1 = Double.parseDouble(atts.getValue("X1"));
	    			y1 = Double.parseDouble(atts.getValue("Y1"));
	    			x2 = Double.parseDouble(atts.getValue("X2"));
	    			y2 = Double.parseDouble(atts.getValue("Y2"));
	    			double cx1,cy1;
	    			cx1 = Double.parseDouble(atts.getValue("CX1"));
	    			cy1 = Double.parseDouble(atts.getValue("CY1"));
	    			
	    			la.add(idx, new Curve(null, null, new QuadCurve2D.Double(x1, y1,
	    					cx1, cy1,
	    					//cx2, cy2,
	    					x2, y2)));
	    			
	    			return;
	    			
	    		}
	    		
	    		if (qName.equals("Switch")) { 			
	    			x1 = Double.parseDouble(atts.getValue("X"));
	    			y1 = Double.parseDouble(atts.getValue("Y"));
	    			
	    			Point2D mp = new Point2D.Double(x1,y1);
	    			
	    			la.add(idx, new Switch(null, null, null, mp));
	    			return;
	    		}
	    		
	    		if (qName.equals("FourWay")) { 			
	    			x1 = Double.parseDouble(atts.getValue("X"));
	    			y1 = Double.parseDouble(atts.getValue("Y"));
	    			
	    			Point2D mp = new Point2D.Double(x1,y1);
	    			
	    			la.add(idx, new FourWay(null, null, null, null, mp));
	    			return;
	    		}
	    		
	    		if (qName.equals("Label")) {
	    			x1 = Double.parseDouble(atts.getValue("X"));
	    			y1 = Double.parseDouble(atts.getValue("Y"));
	    			
	    			Point2D mp = new Point2D.Double(x1,y1);
	    			
	    			double angle = Double.parseDouble(atts.getValue("Angle"));
	    			String value = atts.getValue("Label");
	    			Distance size = new Distance(Double.parseDouble(atts.getValue("Size")), Distance.Measure.FEET);
	    			Color c = new Color(Integer.parseInt(atts.getValue("Red")),
	    					Integer.parseInt(atts.getValue("Green")),
	    					Integer.parseInt(atts.getValue("Blue")));
	    			
	    			
	    			
	    			
	    			la.add(idx, new Label(value, size, c, mp, angle));
	    			return;
	    			
	    		}
	    		
	    		throw new SAXException("Unknown segment type: "+qName);

	    		
	    		
	    	}
	    	
	    	if (linkUp) {
	    		// second phase
	    		
	    		
	    		if (qName.equals("TrackSegment") || qName.equals("EESegment") ||
	    				qName.equals("HiddenSegment") || qName.equals("LUSegment") ||
	    				qName.equals("HiddenLUSegment") || qName.equals("Crossing") ||
	    				qName.equals("Curve")) {
	    			
	    			idx = Integer.parseInt(atts.getValue("ID"));
	    			begin = Integer.parseInt(atts.getValue("Begin"));
	    			end1 = Integer.parseInt(atts.getValue("End"));
	    			
	    			RailSegment r = la.get(idx);
	    			
	    			if (begin > -1)
	    				r.setDest(TrackSegment.POINT_BEGIN, true, la.get(begin));
	    			if (end1 > -1)
	    				r.setDest(TrackSegment.POINT_END, true, la.get(end1));
	    			
	    			
	    		
	    			return;
	    		}
	    		
	    		if (qName.equals("Switch")) { 			
	    		
	    			idx = Integer.parseInt(atts.getValue("ID"));
	    			begin = Integer.parseInt(atts.getValue("Begin"));
	    			end1 = Integer.parseInt(atts.getValue("End1"));
	    			end2 = Integer.parseInt(atts.getValue("End2"));
	    			
	    			RailSegment r = la.get(idx);
	    			
	    			if (begin > -1)
	    				((Switch)r).setDest(Switch.POINT_BEGIN, true, la.get(begin));
	    			if (end1 > -1)
	    				((Switch)r).setDest(Switch.POINT_END1, true, la.get(end1));
	    			if (end2 > -1)
	    				((Switch)r).setDest(Switch.POINT_END2, true, la.get(end2));
	    			return;
	    		}
	    		
	    		if (qName.equals("FourWay")) { 			
		    		
	    			idx = Integer.parseInt(atts.getValue("ID"));
	    			int begina = Integer.parseInt(atts.getValue("BeginA"));
	    			int beginb = Integer.parseInt(atts.getValue("BeginB"));
	    			int enda = Integer.parseInt(atts.getValue("EndA"));
	    			int endb = Integer.parseInt(atts.getValue("EndB"));
	    			
	    			RailSegment r = la.get(idx);
	    			
	    			if (begina > -1)
	    				((FourWay)r).setDest(FourWay.POINT_BEGINA, true, la.get(begina));
	    			if (beginb > -1)
	    				((FourWay)r).setDest(FourWay.POINT_BEGINB, true, la.get(beginb));
	    			
	    			if (enda > -1)
	    				((FourWay)r).setDest(FourWay.POINT_ENDA, true, la.get(enda));
	    			if (endb > -1)
	    				((FourWay)r).setDest(FourWay.POINT_ENDB, true, la.get(endb));
	    			
	    			return;
	    		}
	    	}
	    	
	    }

	    @Override
		public void endElement (String uri, String name, String qName) { expectComment = false; }

	    /**
	     * 
	     * @return Convert the rail segments into an array without nulls
	     */
	    public RailSegment[] lines() {
	    	ArrayList<RailSegment> cmp = new ArrayList<RailSegment>();
	    	
	    	for (RailSegment r : la) 
	    		if (r != null) cmp.add(r);
	    		
	    	return cmp.toArray(new RailSegment[0]);
	    	
	    	
	    	
	    }
		
	    /**
	     * Constructor.
	     * 
	     * @param tbf Metadata to load
	     */
		public XMLHandler(MetaData tbf) {
			super();
			_tbf = tbf;
		}
	}
	
	/**
	 * Reads from an RWM file and returns the rail segments.  Uses two passes.
	 * 
	 * @param f URL of the RWM file
	 * @param toBeFilled An instance of {@link MetaData} to fill.
	 * @return An array of {@link RailSegment} representing all segments.
	 * @throws SAXException If the parser encounters an error.
	 * @throws IOException If the URL cannot be read.
	 */
	public static RailSegment[] read(URL f, MetaData toBeFilled) throws SAXException, IOException {
		
			org.xml.sax.XMLReader xr = XMLReaderFactory.createXMLReader();
			
			XMLHandler handler = new XMLHandler(toBeFilled);
			
			xr.setContentHandler(handler);
			xr.setErrorHandler(handler);
			

		    
		    xr.parse(new InputSource(f.openStream()));

		    // phase 2
		    
		    xr.parse(new InputSource(f.openStream()));
		    
		    
		    
		    RailSegment[] la = handler.lines();
		    
		    toBeFilled.ourFile = null;
		    
		    return la;
		    
		    
			
			
			
	}
	
	/**
	 * Reads from an RWM file and returns the rail segments and updates the given metadata.  Uses two passes.
	 * 
	 * @param f File of the RWM file
	 * @param toBeFilled An instance of {@link MetaData} to fill.
	 * @return An array of {@link RailSegment} representing all segments.
	 * @throws SAXException If the parser encounters an error.
	 * @throws IOException If the URL cannot be read.
	 */
	public static RailSegment[] read(File f, MetaData toBeFilled) throws SAXException, IOException {
		
		org.xml.sax.XMLReader xr = XMLReaderFactory.createXMLReader();
		
		XMLHandler handler = new XMLHandler(toBeFilled);
		
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		

	    FileReader r = new FileReader(f);
	    xr.parse(new InputSource(r));

	    // phase 2
	    r = new FileReader(f);
	    xr.parse(new InputSource(r));
	    
	    
	    
	    RailSegment[] la = handler.lines();
	    
	    toBeFilled.ourFile = f;
	    
	    return la;
	    
	}
    
	

}
