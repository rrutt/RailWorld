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


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.kolls.railworld.Car;
import net.kolls.railworld.ExtensibleFactory;
import net.kolls.railworld.Factories;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.SignalProgram;
import net.kolls.railworld.Train;
import net.kolls.railworld.TrainControl;
import net.kolls.railworld.play.PlayCanvas;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.Mission;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.Signal;
import net.kolls.railworld.segment.Switch;
import net.kolls.railworld.tuic.TrainEndPointFinder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Reads Rail World Games (RWG) files and updates segments, visual appearance, and trains
 * 
 * @author Steve Kollmansberger
 *
 */
public class RWGReader {

	
	
	private static class XMLReader {
		private Document document;
		private Element root;
		private PlayFrame pf;
		private List<Script> avs;
		

		/**
		 * Create a reader for the XML source.
		 * 
		 * @param r XML source
		 * @param pf The play frame to populate
		 * @param avail The list of available scripts.
		 * @throws IOException 
		 * @throws SAXException
		 */
		public XMLReader(InputSource r, PlayFrame pf, List<Script> avail) throws IOException, SAXException  {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				document = builder.parse(r);
			} catch (ParserConfigurationException ex) {
				throw new SAXException(ex);
			}
			
			root = document.getDocumentElement();
			
			
			this.pf = pf;
			this.avs = avail;
			String vers = root.getAttribute("Version");
			if (vers.equals("1.3") == false) throw new SAXException("Unsupported version: "+ vers);
		}
		
		/**
		 * 
		 * @return The name of the RWM file used by this saved game.
		 */
		public String rwmFile() {
			// railmap -> meta -> map
			return root.getFirstChild().getFirstChild().getAttributes().getNamedItem("Name").getTextContent();
		}
		
		/**
		 * Load this saved game into the playframe given.
		 * 
		 * @throws SAXException
		 */
		public void load() throws SAXException {
			meta();
			scripts();
			segments(pf.jdb.la);
			trains(pf.jdb.la);
			
		}
		
		/**
		 * Load the position data and update the playframe to center on the previous (saved) position.
		 */
		public void meta() {
			PlayCanvas prc = pf.jdb;
			Node n = root.getFirstChild().getChildNodes().item(1);
			NamedNodeMap atts = n.getAttributes();
			
			RailCanvas.zoom = Double.parseDouble(atts.getNamedItem("Zoom").getTextContent());
    		int cx = (int)Double.parseDouble(atts.getNamedItem("CenterX").getTextContent());
    		int cy = (int)Double.parseDouble(atts.getNamedItem("CenterY").getTextContent());
    		
    		prc.submitCenterCoords(cx, cy);
    		
    		pf.gl.elapsed = 
    			Long.parseLong(root.getFirstChild().getAttributes().getNamedItem("Elapsed").getTextContent());
    		
		}
		
		/**
		 * Configure switch and signal segments.
		 * 
		 * @param la The list of segments to read.
		 * @throws SAXException
		 */
		public void segments(RailSegment[] la) throws SAXException {
			Node n = root.getChildNodes().item(1);
			
			for (int i = 0; i < n.getChildNodes().getLength(); i++) {
				Node cs = n.getChildNodes().item(i);
				int id = Integer.parseInt(cs.getAttributes().getNamedItem("ID").getTextContent());
				if (cs.getNodeName().equals("Signal")) {
	    			if (id > la.length || la[id] instanceof Signal == false) throw new SAXException("Map/Game inconsistency on segment id "+id);
	    			((Signal)la[id]).sp = (SignalProgram)create(cs.getFirstChild(), Factories.sps);

				}
				
				if (cs.getNodeName().equals("Switch")) {
	    			if (id > la.length || la[id] instanceof Switch == false) throw new SAXException("Map/Game inconsistency on segment id "+id);
	    			((Switch)la[id]).flipped = cs.getAttributes().getNamedItem("Flipped").getTextContent().equals("Yes");

				}
			}
		}
		
		/**
		 * Load and add all trains.
		 * 
		 * @param la The segments to place the trains on.
		 * @throws SAXException
		 */
		public void trains(RailSegment[] la) throws SAXException {
			Node n = root.getChildNodes().item(2);
			for (int i = 0; i < n.getChildNodes().getLength(); i++) {
				Node cs = n.getChildNodes().item(i);
				int len = Integer.parseInt(cs.getAttributes().getNamedItem("Length").getTextContent());
				Car selc = null;
				Car[] cars = new Car[len];
				// 0. Pos
				// 1. Control
				// 2. Cars
				// 3. Controller
				
				// setup cars first
				Node ncars = cs.getChildNodes().item(2);
				for (int j = 0; j < ncars.getChildNodes().getLength(); j++) {
					Node car = ncars.getChildNodes().item(j);
					int idx = Integer.parseInt(car.getAttributes().getNamedItem("ID").getTextContent());
					cars[idx] = (Car)create(car.getFirstChild(), Factories.cars);
					if (car.getAttributes().getNamedItem("Selected").getTextContent().equals("Yes"))
						selc = cars[idx];
					
				}
				

				Train t = new Train(cars);
				// now position
				Node npos = cs.getFirstChild();
				int id;
				
    			
    			id = Integer.parseInt(npos.getAttributes().getNamedItem("OrigID").getTextContent());
    			if (id > la.length) throw new SAXException("Map/Game inconsistency on segment id "+id);
    			if (id != -1)
    				t.pos.orig = la[id];
    			
    			id = Integer.parseInt(npos.getAttributes().getNamedItem("RID").getTextContent());
    			if (id > la.length) throw new SAXException("Map/Game inconsistency on segment id "+id);
    			if (id == -1) t.pos.r = ((EESegment)t.pos.orig).HES;
    			else t.pos.r = la[id];
    			
    			t.pos.per = Double.parseDouble(npos.getAttributes().getNamedItem("Per").getTextContent());

    			// now controller
    			TrainControl tc = (TrainControl)create(cs.getLastChild().getFirstChild(), Factories.controllers);
    			tc.setTrainActionScriptNotify(pf.jdb.sm);
    			t.setController(tc);
    			
    			t.getController().fillConsist();

    			
    			// now control
    			Node nctl = cs.getChildNodes().item(1);
    			t.setThrottle(Integer.parseInt(nctl.getAttributes().getNamedItem("Throttle").getTextContent()));
    			t.setBrake(nctl.getAttributes().getNamedItem("Brake").getTextContent().equals("Yes"));
    			t.setVel(Double.parseDouble(nctl.getAttributes().getNamedItem("Velocity").getTextContent()));
    			if (nctl.getAttributes().getNamedItem("Selected").getTextContent().equals("Yes")) {
    				pf.jdb.trains.select(t, selc);
    			}
    			
    			pf.jdb.trains.add(t);
    			
    			// establish the trains set on all segments this train sits on
    			TrainEndPointFinder tepf = new TrainEndPointFinder();
    			tepf.act(t);
    			
			}
		}
		
		
		/**
		 * Load and configure the scripts.
		 */
		public void scripts() {
			
			Node scripts = root.getLastChild();

			// take advantage of the alignment because
			// we made the script manager
			// so the scripts should be in index order
			for (int i = 0; i < scripts.getChildNodes().getLength(); i++) {
				Node scn = scripts.getChildNodes().item(i);
				
				
				pf.jdb.sm.get(i).init(pf);
				pf.jdb.sm.get(i).load(items(scn));
			}
			
				
			
			
		}
		
		/**
		 * Determine which scripts are in use and create a script manager.
		 * @return
		 */
		public ScriptManager scriptsInUse() {
			ScriptManager sm = new ScriptManager();
			
			Node scripts = root.getLastChild();
			//List<Script> ls = new ArrayList<Script>();
			for (int i = 0; i < scripts.getChildNodes().getLength(); i++) {
				Node scn = scripts.getChildNodes().item(i);
				String _type = scn.getAttributes().getNamedItem("Type").getTextContent();
				
				
				Iterator<Script> its = avs.iterator();
	    		while (its.hasNext()) {
	    			Script s = its.next();
	    			if (s.toString().equals(_type)) {
	    				sm.add(s);
	    				if (s instanceof Mission)
	    					sm.mission = (Mission)s;
	    			}
	    			
	    		}
				
			}
			return sm;
		}
		
		private SaveLoad create(Node n, ExtensibleFactory<?> ef) throws SAXException {
			SaveLoad thing = null;
	    	String _type = n.getAttributes().getNamedItem("Type").getTextContent();
	    	
	    	try {
	    		thing = ef.createInstance(_type);
	    		thing.load(items(n));
	    		
	    	} catch (ClassNotFoundException ex) {
	    		throw new SAXException(ex);
	    	}
	    	
	    	return thing;
		}
		
		
		private Map<String, String> items(Node data) {
			Map<String, String> it = new Hashtable<String, String>();
			for (int i = 0; i < data.getChildNodes().getLength(); i++) {
				Node c = data.getChildNodes().item(i);
				it.put(c.getAttributes().getNamedItem("Key").getTextContent(), 
						c.getTextContent());
			}
			return it;
		}
	}
	
	
	
	/**
	 * Reads a Rail World Game (RWG) file just to find out the map name associated with it.
	 * No other data is acquired.
	 * 
	 * @param f The {@link File} containing the RWG data
	 * @return A String indicating the file name (without path) of the associated RWM file
	 * @throws SAXException If the parser encounters an error.
	 * @throws IOException If the file cannot be read.
	 */
	public static String readForRWM(File f) throws SAXException, IOException {
		
		FileReader r = new FileReader(f);
		
		XMLReader handler = new XMLReader(new InputSource(r), null, null);
		
	    return handler.rwmFile();
	}
	

	/**
	 * Create a script manager of the scripts for this game.
	 * Every scripts used in the save game
	 * must be in the available list.
	 * 
	 * @param f The {@link File} containing the RWG data
	 * @param scripts A list of available {@link Script}s.  Every script used in the saved game must be available.
	 * @return A ScriptManager containing un-initialized scripts in this game.
	 * @throws SAXException If the parser encounters an error.
	 * @throws IOException If the file cannot be read.
	 */
	public static ScriptManager readForScripts(File f, List<Script> scripts) throws SAXException, IOException {

		FileReader r = new FileReader(f);
		
		XMLReader handler = new XMLReader(new InputSource(r), null, scripts);
		
	    return handler.scriptsInUse();
	}
	
	
	/**
	 * Reads a Rail World Game (RWG) file and populates/updates a given rail canvas and train list.
	 * The rail canvas must already be initialized with the appropriate map.  The train list
	 * is assumed to be empty.  In order to have the canvas initialized, you should use
	 * {@link #readForRWM(File)} first and load the given RWM file.  
	 * Next, prepare the PlayFrame with the appropriate scripts from {@link #readForScripts(File, List)}.
	 * 
	 * 
	 * 
	 * @param f The File containing the RWG data
	 * @param rf The {@link PlayFrame} to update.
	 * 
	 * @throws SAXException If the parser encounters an error, or if there is a problem with the map/game line-up.
	 * @throws IOException If the file cannot be read.
	 */
	public static void read(File f, PlayFrame rf) throws SAXException, IOException {
		
		
		
		
		FileReader r = new FileReader(f);
		
		XMLReader handler = new XMLReader(new InputSource(r), rf, null);
		
	    handler.load();
	    
	    
	    

	}
}
