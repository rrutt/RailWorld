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


import net.kolls.railworld.Car;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.segment.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import java.awt.geom.Point2D;
import java.io.*;

/**
 * Writes a Rail World map as an XML file in the RWM format.
 * 
 * @author Steve Kollmansberger
 *
 */
public class RWMWriter {
	
	private static int index(RailSegment[] lines, RailSegment r) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] == r) return i;
		}
		return -1;
	}
	
	private static Document buildDom(RailSegment[] lines, MetaData md) throws ParserConfigurationException {
		Document document;
		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		

		Element root;
		Element e;
		Element q;
		Element intr;
		
		root = document.createElement("RailMap");
		// 1.1 adds fourway
		root.setAttribute("Version", "1.2");
		document.appendChild(root);

		e = document.createElement("Meta");
		
		q = document.createElement("Distance");
		
		q.setAttribute("FeetPerPixel", Double.toString(md.feetPerPixel));
		q.setAttribute("Zoom", Double.toString(md.zoom));
		
		
		e.appendChild(q);
		
		q = document.createElement("Image");
		q.setAttribute("File", md.imgfile);
		e.appendChild(q);
		
		
		q = document.createElement("Map");
		q.setAttribute("Author", md.author);
		q.setAttribute("Title", md.title);
		q.appendChild(document.createCDATASection(md.comment));
		
		e.appendChild(q);
		
		q = document.createElement("Center");
		q.setAttribute("X", Integer.toString(md.centerX));
		q.setAttribute("Y", Integer.toString(md.centerY));
		e.appendChild(q);
		
		
		
		root.appendChild(e);

		
		
		
		
		e = document.createElement("Segments");
		root.appendChild(e);
		
		for (int i = 0; i < lines.length; i++) {
			RailSegment r = lines[i];
			if (r == null) continue;
			String cn = r.getClass().getSimpleName();
			q = document.createElement(cn);
			q.setAttribute("ID", Integer.toString(i));
			q.setIdAttribute("ID", true);
			
			
			if (r instanceof Label) {
				Label lbl = (Label)r;
				
				
				q.setAttribute("X", Double.toString(lbl.getPoint(0).getX()));
				q.setAttribute("Y", Double.toString(lbl.getPoint(0).getY()));
				
				q.setAttribute("Label", lbl.value);
				
				q.setAttribute("Angle", Double.toString(lbl.angle));
				
				q.setAttribute("Size", Double.toString(lbl.size.feet()));
				
				
				
				
				q.setAttribute("Red", Integer.toString(lbl.c.getRed()));
				q.setAttribute("Green", Integer.toString(lbl.c.getGreen()));
				q.setAttribute("Blue", Integer.toString(lbl.c.getBlue()));
				
				
				
				
				
			}
			
			if (r instanceof Curve) {
				// curve
				Curve c = (Curve)r;
				q.setAttribute("Begin", Integer.toString(index(lines, c.getDest(Curve.POINT_BEGIN))));
				q.setAttribute("End", Integer.toString(index(lines, c.getDest(Curve.POINT_END))));
				Point2D p1 = c.getPoint(Curve.POINT_BEGIN);
				Point2D p2 = c.getPoint(Curve.POINT_END);
				Point2D cp1 = c.getPoint(Curve.POINT_CP1);
				//Point2D cp2 = c.getPoint(Curve.POINT_CP2);
				q.setAttribute("X1", Double.toString(p1.getX()));
				q.setAttribute("Y1", Double.toString(p1.getY()));
				q.setAttribute("X2", Double.toString(p2.getX()));
				q.setAttribute("Y2", Double.toString(p2.getY()));
				
				q.setAttribute("CX1", Double.toString(cp1.getX()));
				q.setAttribute("CY1", Double.toString(cp1.getY()));
				//q.setAttribute("CX2", Double.toString(cp2.getX()));
				//q.setAttribute("CY2", Double.toString(cp2.getY()));
				
				
				
			}
			if (r instanceof TrackSegment) {
				// any tracksegment or child
				TrackSegment t = (TrackSegment)r;
				q.setAttribute("Begin", Integer.toString(index(lines, t.getDest(TrackSegment.POINT_BEGIN))));
				q.setAttribute("End", Integer.toString(index(lines, t.getDest(TrackSegment.POINT_END))));
				Point2D p1 = t.getPoint(TrackSegment.POINT_BEGIN);
				Point2D p2 = t.getPoint(TrackSegment.POINT_END);
				q.setAttribute("X1", Double.toString(p1.getX()));
				q.setAttribute("Y1", Double.toString(p1.getY()));
				q.setAttribute("X2", Double.toString(p2.getX()));
				q.setAttribute("Y2", Double.toString(p2.getY()));
			}
			if (r instanceof EESegment) {
				q.setAttribute("Label", ((EESegment)r).label);
			}
			if (r instanceof LUSegment) {
				LUSegment l = (LUSegment)r;
				Car[] lucars = l.lu();
				q.setAttribute("DrawAccept", l.doesDrawAccept() ? "Yes" : "No");
				for (int j = 0; j < lucars.length; j++) {
					Car c = lucars[j];
					// these are cars the segment accepts
					// if it accepts a loaded car, it is to be unloaded
					// and vice verse
					intr = document.createElement(c.loaded() ? "Unload" : "Load");
					intr.setAttribute("Type", c.show());
					q.appendChild(intr);
					
				}
			}
			if (r instanceof Switch) {
				Switch s = (Switch)r;
				Point2D p = s.getPoint(0);
				
				
				q.setAttribute("Begin", Integer.toString(index(lines, s.getDest(Switch.POINT_BEGIN))));
				q.setAttribute("End1", Integer.toString(index(lines, s.getDest(Switch.POINT_END1))));
				q.setAttribute("End2", Integer.toString(index(lines, s.getDest(Switch.POINT_END2))));
				
				q.setAttribute("X", Double.toString(p.getX()));
				q.setAttribute("Y", Double.toString(p.getY()));
						
			}
			
			if (r instanceof FourWay) {
				FourWay s = (FourWay)r;
				Point2D p = s.getPoint(0);
				
				
				q.setAttribute("BeginA", Integer.toString(index(lines, s.getDest(FourWay.POINT_BEGINA))));
				q.setAttribute("BeginB", Integer.toString(index(lines, s.getDest(FourWay.POINT_BEGINB))));
				q.setAttribute("EndA", Integer.toString(index(lines, s.getDest(FourWay.POINT_ENDA))));
				q.setAttribute("EndB", Integer.toString(index(lines, s.getDest(FourWay.POINT_ENDB))));
				
				q.setAttribute("X", Double.toString(p.getX()));
				q.setAttribute("Y", Double.toString(p.getY()));
						
			}
			e.appendChild(q);
			
			
		}
		
		

		return document;
	}	

	private static void save(Document d, File f) throws TransformerException {
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer trans = factory.newTransformer();

		

		
		
		StreamResult sr = new StreamResult(f);

		trans.transform(new DOMSource(d), sr);

	
		
		
		
	
	}

	/**
	 * Saves rail segments and associated metadata in the RWM format.
	 * 
	 * @param la Array of rail segments.
	 * @param md MetaData to write.
	 * @param f File to write to.
	 * @throws TransformerException If the file cannot be written.
	 * @throws ParserConfigurationException If the XML DOM builder breaks.
	 */
	public static void write(RailSegment[] la, MetaData md, File f) throws TransformerException, ParserConfigurationException {
		
		save(buildDom(la, md), f);
	}
	
}
