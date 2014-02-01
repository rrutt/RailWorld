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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.play.Trains;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.Signal;
import net.kolls.railworld.segment.Switch;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves an in-progress game (position, zoom, selected train, and all trains).
 * It requires the map to go with it
 * 
 * @author Steve Kollmansberger
 *
 */
public class RWGWriter {
	private static int index(RailSegment[] lines, RailSegment r) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == r) return i;
		}
		
	
		return -1;
	}
	
	private static Element buildData(Document d, SaveLoad s) {
		Map<String, String> data = s.save();
		
		Element dt = d.createElement("Data");
		dt.setAttribute("Type", s.toString());
		if (data == null) return dt;
		
		Set<Entry<String,String>> kvps = data.entrySet();
		
		
		Iterator<Entry<String,String>> kvs = kvps.iterator();
		
		while (kvs.hasNext()) {
			Element e;
			Entry<String,String> kv = kvs.next();
			e = d.createElement("Item");
			
			e.setAttribute("Key", kv.getKey());
			//e.setAttribute("Value", kv.getValue());
			e.setTextContent(kv.getValue());
			dt.appendChild(e);
			
		}
		
		return dt;
	}
	
	private static Document buildDom(RailSegment[] lines, Trains trs, ScriptManager sm, MetaData md) throws Exception {
		Document document;
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		

		Element root;
		Element e;
		Element q;
		Element intr, cr;
		
		root = document.createElement("RailGame");
		root.setAttribute("Version", "1.3");
		document.appendChild(root);

		e = document.createElement("Meta");
		
		e.setAttribute("Elapsed", Long.toString(md.elapsed));
		
		q = document.createElement("Map");
		
		q.setAttribute("Name", md.ourFile.getName());
		e.appendChild(q);
		
		q = document.createElement("Visual");
		
		q.setAttribute("Zoom", Double.toString(md.zoom));
		q.setAttribute("CenterX", Double.toString(md.centerX));
		q.setAttribute("CenterY", Double.toString(md.centerY));
		
		e.appendChild(q);
		root.appendChild(e);
		

		

		e = document.createElement("Segments");
		for (int j = 0; j < lines.length; j++) {
			if (lines[j] instanceof Switch) {
				q = document.createElement("Switch");
				q.setAttribute("ID", Integer.toString(j));
				q.setAttribute("Flipped", ((Switch)lines[j]).flipped ? "Yes" : "No");
				e.appendChild(q);
			}
			if (lines[j] instanceof Signal) {
				q = document.createElement("Signal");
				q.setAttribute("ID", Integer.toString(j));
		
				
				q.appendChild(buildData( document, ((Signal)lines[j]).sp  ));
				//q.setTextContent(Base64.encodeObject(((Signal)lines[j]).sp));
				//q.appendChild(newChild)
				
				e.appendChild(q);
			}
		}
		root.appendChild(e);
		
		
		e = document.createElement("Trains");
		
		Iterator<Train> it = trs.iterator();
		
		try {
			while (it.hasNext()) {
				q = document.createElement("Train");
				Train t = it.next();
				q.setAttribute("Length", Integer.toString(t.array().length));
				
				
				int rid = index(lines, t.pos.r);
				int origid = index(lines, t.pos.orig);
				
				intr = document.createElement("Pos");
				intr.setAttribute("RID", Integer.toString(rid));
				intr.setAttribute("OrigID", Integer.toString(origid));
				intr.setAttribute("Per", Double.toString(t.pos.per));
				
				q.appendChild(intr);
				
				intr = document.createElement("Control");
				intr.setAttribute("Throttle", Integer.toString(t.getThrottle()));
				intr.setAttribute("Brake", t.getBrake() ? "Yes" : "No");
				intr.setAttribute("Velocity", Double.toString(t.vel()));
				intr.setAttribute("Selected", trs.getSelectedTrain() == t ? "Yes" : "No");
				
				q.appendChild(intr);

				intr = document.createElement("Cars");
				
				
				for (int i = 0; i < t.array().length; i++) {
					cr = document.createElement("Car");
					cr.setAttribute("ID", Integer.toString(i));
					cr.setAttribute("Selected", trs.getSelectedCar() == t.array()[i] ? "Yes" : "No");
					
					cr.appendChild(buildData(document, t.array()[i]));
					/*cr.setAttribute("Type", t.array()[i].show());
					if (t.array()[i].isLoadable())
						cr.setAttribute("Loaded", t.array()[i].loaded() ? "Yes" : "No");
					
					
					*/
					intr.appendChild(cr);
				}
				
				q.appendChild(intr);
				
				
				intr = document.createElement("Controller");
				intr.appendChild(buildData( document, t.getController()  ));
				//intr.setTextContent(Base64.encodeObject(t.getController()));
				q.appendChild(intr);
				
				e.appendChild(q);
				
			}
		} catch (ConcurrentModificationException ex) {
			throw new Exception("Train list changed while attempting to save; try again.");
			
		}
		
		root.appendChild(e);
		
		e = document.createElement("Scripts");
		
		Iterator<Script> si = sm.iterator();
		while (si.hasNext()) {
			Script s = si.next();
			
			e.appendChild(buildData(document, s));
		}
		root.appendChild(e);
		
		return document;
		
		
	}
	
	private static void save(Document d, File f) throws TransformerException {
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer trans = factory.newTransformer();

		
		StreamResult sr = new StreamResult(f);

		trans.transform(new DOMSource(d), sr);		
		
	
	}

	/**
	 * Saves the current game state (trains, dynamic track configuration, visual orientation)
	 * to a Rail World Game file.
	 * 
	 * @param la Array of rail segments to find both position IDs and track settings
	 * @param trs Trains to save
	 * @param sm The ScriptManager containing all scripts in use.
	 * @param md MetaData with center X and Y, and zoom
	 * @param f File to save to
	 * @throws Exception If save is unable to proceed, especially if trains is modified while saving
	 */
	public static void write(RailSegment[] la, Trains trs, ScriptManager sm, MetaData md, File f) throws Exception {
		
		save(buildDom(la, trs, sm, md), f);
	}
	
}
