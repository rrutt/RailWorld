package net.kolls.railworld.segment;

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


import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;


import net.kolls.railworld.Distance;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.SignalProgram;
import net.kolls.railworld.Train;
import net.kolls.railworld.edit.SegmentEditPoint;
import net.kolls.railworld.play.SignalProgramChooser;
import net.kolls.railworld.segment.sp.Green;

/**
 * Provides a signal to indicate whether a train should proceed.
 * The signal is unidirectional -- it controls from begin to end only.
 * 
 * 
 * @author Steve Kollmansberger 
 *
 */
public class Signal extends RailSegment {

	
	
	private Line2D ind, base;
	
	
	
	
	// goes backward to look for end of track or another signal in the same direction
	private static double distanceToNext(RailSegment a, RailSegment orig, boolean checkSig) {
		double d = 0;
		RailSegment tmp;
		while (a != null) {
			
			if (checkSig && a instanceof Signal) 
				if (((Signal)a).getDest(Signal.POINT_END) == orig) break;
		
			
			d += a.length().feet();
			tmp = a;
			a = a.dest(orig);
			orig = tmp;
		}
		return d;
		
	}
	
	@Override
	public String mouseOver(Point2D pos) {
		if (ind.ptSegDist(pos) <= MOUSE_NEAR.iPixels()) {
					
			
			return sp.toString();
				

		}
		
		
		return null;
	}
	
	private static Signal condCreateSignal(RailSegment from, RailSegment to) {
		Signal c = null;
		
		// two signals backtoback is ok IFF
		// 1. The signals face opposite directions
		// 2. No more than 2 immediately adjacent signals
		
		
		
		if (to instanceof Signal && to.dest(from) instanceof Signal) return null;
		if (to instanceof Signal && ((Signal)to).getDest(Signal.POINT_BEGIN) == from) return null;
		
		if (from instanceof Signal && from.dest(to) instanceof Signal) return null;
		if (from instanceof Signal && ((Signal)from).getDest(Signal.POINT_END) == to) return null;
		
		if ((to instanceof TrackSegment || to instanceof Signal) && 
				(from instanceof TrackSegment || from instanceof Signal)) {
			c = new Signal(from,to);
			from.update(to, c);
			to.update(from, c);
			
			
			
			return c;
		}
		
		
		return null;
	}
	
	/**
	 * Generates signals at switches and four ways in a reasonable way.
	 * Both legs of the switch must be track segments.  
	 * If a switch is connected to another switch by one segment,
	 * that side will not have a signal.
	 * Signals will not be placed if a previous signal is within 300 feet.
	 * 
	 * @param lines The original rail segments.
	 * @return New rail segments with signals added.
	 */
	public static RailSegment[] createSignals(RailSegment[] lines) {
		Vector<RailSegment> vl = new Vector<RailSegment>();
		Vector<RailSegment> dl = new Vector<RailSegment>();
		
		
		RailSegment a1, a2, a3, a4, b;
		Signal c;
		int i;
		
		for (i = 0; i < lines.length; i++) {
			
			if (lines[i] instanceof Switch) {
				a1 = ((Switch)lines[i]).getDest(Switch.POINT_END1);
				a2 = ((Switch)lines[i]).getDest(Switch.POINT_END2);
				
					// place signals at on-coming legs
				
					b = a1.dest(lines[i]);
					c = condCreateSignal(b,a1);
					if (c != null) vl.add(c);
				
				
					b = a2.dest(lines[i]);
					c = condCreateSignal(b,a2);
					if (c != null) vl.add(c);

			}
			
			if (lines[i] instanceof FourWay) {
				a1 = ((FourWay)lines[i]).getDest(FourWay.POINT_ENDA);
				a2 = ((FourWay)lines[i]).getDest(FourWay.POINT_ENDB);
				a3 = ((FourWay)lines[i]).getDest(FourWay.POINT_BEGINA);
				a4 = ((FourWay)lines[i]).getDest(FourWay.POINT_BEGINB);
				
					// place signals at on-coming legs
					b = a1.dest(lines[i]);					
					c = condCreateSignal(b,a1);
					if (c != null) vl.add(c);
				
				
					b = a2.dest(lines[i]);					
					c = condCreateSignal(b,a2);
					if (c != null) vl.add(c);
					
					b = a3.dest(lines[i]);
					c = condCreateSignal(b,a3);
					if (c != null) vl.add(c);
					
					b = a4.dest(lines[i]);
					c = condCreateSignal(b,a4);
					if (c != null) vl.add(c);

			}
			if (lines[i] instanceof EESegment) {
				a1 = ((EESegment)lines[i]).getDest(TrackSegment.POINT_BEGIN);
				a2 = ((EESegment)lines[i]).getDest(TrackSegment.POINT_END);
				
				// place signals to block exits
				
				if (a1 == ((EESegment)lines[i]).HES)
					c = condCreateSignal(a2,lines[i]);
				else
					c = condCreateSignal(a1,lines[i]);
				if (c != null) vl.add(c);
			
				

			}
			if (lines[i] instanceof LUSegment) {
				a1 = ((LUSegment)lines[i]).getDest(TrackSegment.POINT_BEGIN);
				a2 = ((LUSegment)lines[i]).getDest(TrackSegment.POINT_END);
				
				// place signals to block l/u segments
				// note if the l/u segment continues don't block
				
				// on coming signals one step further
				// not anymore!
				
				

				if (!(a1 instanceof LUSegment)) {
					c = condCreateSignal(lines[i], a1);
					if (c != null) vl.add(c);
					
					/*if (!(b1 instanceof LUSegment)) {
						c = condCreateSignal(b1, a1);
						if (c != null) vl.add(c);
					}*/
					
					c = condCreateSignal(c == null ? a1 : c, lines[i]);
					if (c != null) vl.add(c);
				}
				if (!(a2 instanceof LUSegment)) {
					c = condCreateSignal(lines[i], a2);
					if (c != null) vl.add(c);
					/*if (!(b2 instanceof LUSegment)) {
						c = condCreateSignal(b2, a2);
						if (c != null) vl.add(c);
					}*/
					c = condCreateSignal(c == null ? a2 : c, lines[i]);
					if (c != null) vl.add(c);
				}
				
				
				
				
				
				
			
			}

			vl.add(lines[i]);
		}

		
		// find candidates for deletion
		Iterator<RailSegment> ri = vl.iterator();
		RailSegment cr, to, from;
		while (ri.hasNext()) {
			cr = ri.next();
			
			if (cr instanceof Signal) {
				c = (Signal)cr;
				// if this signal is supersumed by another withint some distance or is up against the end
				// for the coming from direction we ONLY check for end of track
				from = c.getDest(Signal.POINT_BEGIN);
				to = c.getDest(Signal.POINT_END);
				if (distanceToNext(  from, c, true) < 300 ||
						distanceToNext(  to, c, false) < 300) {					
					
					from.update(c, to);
					to.update(c, from);
					dl.add(cr);
					
					
					
				} 
			}
		}

		vl.removeAll(dl);
		
		// once the final set of signals is reached, recompute all signals
		ri = vl.iterator();
		
		while (ri.hasNext()) {
			cr = ri.next();
			if (cr instanceof Signal) ((Signal)cr).recomp();
		
			
		}
		
		return vl.toArray(new RailSegment[0]);
	}
	
	/**
	 * Create a signal.  Normally these are created automatically
	 * by {@link #createSignals(RailSegment[])}.  The
	 * signal operates in only one direction; from begin to end.
	 *  
	 * @param begin The begin (origin) segment
	 * @param end The destination segment
	 */
	public Signal(RailSegment begin, RailSegment end) {
		dests = new RailSegment[2];
		this.dests[0] = begin;
		this.dests[1] = end;
		
		pts = new Point2D[1];
		pts[0] = begin.getPoint(end, 0);
		
		
		
		sp = new Green();
		
		recomp();
		
		
		
		
	}
	
	@Override
	public void recomp() {
		Line2D bl;
		
		// from end
		ArrayList<RailSegment> nzs = destNZ(dests[1]);
		RailSegment d = nzs.get(nzs.size()-1);
		RailSegment orig = nzs.get(nzs.size() - 2);
		
		bl = new Line2D.Float(d.getPoint(orig,1), pts[0]);
		
		
		
		Line2D t;
		
		t = RailCanvas.angle(bl, pts[0], Math.PI / 2.0, db);
		
		base = RailCanvas.angle(bl, t.getP2(), Math.PI, dt);
		ind = RailCanvas.angle(bl, base.getP2(), Math.PI, MOUSE_NEAR);
		
		inds = new BasicStroke(dw.iPixels());
		
	}
	
	@Override
	public boolean canErase() {
		// n/a
		return true;
	}

	
	
	@Override
	public boolean click(Point2D pos, RailCanvas rc) {
		
		if (ind.ptSegDist(pos) <= MOUSE_NEAR.iPixels()) {
			
			
			JFrame sigf = new SignalProgramChooser(this);
			
			
			// set location relative to doesn't seem to be working
			// so I'm using this unfortunate hack!
			int x = (int)(pos.getX()), y = (int)(pos.getY());
			x -= (int)(rc.getVXY().getX());
			y -= (int)(rc.getVXY().getY());
			
			x = (int)(x * RailCanvas.zoom);
			y = (int)(y * RailCanvas.zoom);
			
			Component c = rc;
			while (c != null) {
				x += c.getX();
				y += c.getY();
				c = c.getParent();
			}
			sigf.setLocation(x, y);
			sigf.setVisible(true);
			
			return true;
		}
		return false;
	}

	@Override
	public RailSegment dest(RailSegment source) {
		if (source == dests[0]) return dests[1]; else return dests[0];
		
	}

	
	private Stroke inds;
	
	
	private static final Distance dt = new Distance(10.0, Distance.Measure.FEET);
	private static final Distance db = new Distance(5.0, Distance.Measure.FEET);
	
	private static final Distance dw = new Distance(RAIL_WIDTH.feet()*2.0, Distance.Measure.FEET);
	
	@Override
	public void draw(int z, Graphics2D gc) {
		if (z != 4) return;
	
		
		gc.setStroke(inds);
		gc.setPaint(Color.black);
		gc.draw(base);
		
		gc.setPaint(sp.status());
		gc.draw(ind);
		

	}

	@Override
	public JPanel editPanel() {
		// not in edit mode
		return null;
	}

	@Override
	public void enter(Train t) {
		// check if train enters from facing side
		if (t.pos.r == getDest(POINT_END))
			sp.enter(t); 
		
	}

	@Override
	public Point2D getPoint(RailSegment start, double myPos) {
		return pts[0];
		//Point2D np = new Point2D.Double(myPoint.getX()*zoom, myPoint.getY()*zoom );
		//return np;
		
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
	private static final Distance zd = new Distance(0, Distance.Measure.FEET);
	
	@Override
	public Distance length() {
		return zd;
	}

	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment attach) {
		// not in edit mode
		return null;
	}

	/**
	 * The begin segment of the signal.  The signal controls traffic
	 * from this segment.
	 */
	public static final int POINT_BEGIN = 0;
	
	/**
	 * The destination segment.
	 */
	public static final int POINT_END = 1;
	

	@Override
	public boolean singleton() {
		return true;
	}

	



	/**
	 * The {@link SignalProgram} controlling this signal.
	 */
	public SignalProgram sp;

}
