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

import net.kolls.railworld.*;
import net.kolls.railworld.edit.SegmentEditPoint;

import java.awt.geom.*;
import java.awt.*;

import javax.swing.JPanel;


/**
 * A regular, straight-line segment of track.
 * 
 * @author Steve Kollmansberger
 *
 */
public class TrackSegment extends RailSegment {
	
	
	private Distance len;
	
	
	
	/**
	 * How the segment is ended.  By default, segments end with a round cap,
	 * although in some cases (like with crossing or hidden segments) this is not desirable.
	 */
	protected int cap;
	
	/**
	 * The line connecting two points, on the map image.
	 */
	protected Line2D coords;
	
	/**
	 * The rail bed pen
	 */
	protected Stroke railBedStroke;
	
	/**
	 * The rail line pen
	 */
	protected Stroke railStroke;
	
	
	// segments on both sides (or null), plus coordinates
	/**
	 * Create a track segment.  The coordinates will be immediately converted
	 * to feet, so the distance information must be correct
	 * by the time this is called.
	 * 
	 * @param bg Begin segment
	 * @param en End segment
	 * @param crds Line coords
	 */
	public TrackSegment(RailSegment bg, RailSegment en, Line2D crds) {
		dests = new RailSegment[2];
		dests[0] = bg; dests[1] = en;
		pts = new Point2D[2];
		pts[0] = crds.getP1();
		pts[1] = crds.getP2();

		
		
		recomp();
	}
	
	/**
	 * The begin segment
	 */
	public static final int POINT_BEGIN = 0;
	
	/**
	 * The end segment
	 */
	public static final int POINT_END = 1;

	
	/**
	 * 
	 * @return The coordinates of this line
	 */
	public Line2D getCoords() { return coords; }
	
	@Override
	public String mouseOver(Point2D pos) { return null; }
	
	private static TrackSegment gpManager = null;
	private static TrackSegment leader = null;
	
	@Override
	public void recomp() {
		coords = new Line2D.Double(pts[0], pts[1]);
		
		// calculate length using cartesian distance on the line
		double dx = coords.getX1() - coords.getX2();
        double dy = coords.getY1() - coords.getY2();
        double d = Math.sqrt(dx*dx + dy*dy);
        
		len = new Distance(d, Distance.Measure.PIXELS);

		setCap(); 
		
		railBedStroke = new BasicStroke(RAIL_BED_WIDTH.iPixels(), cap, BasicStroke.JOIN_BEVEL);
		railStroke = new BasicStroke(RAIL_WIDTH.iPixels(), cap, BasicStroke.JOIN_BEVEL);
		
		/*
		 * For performance reasons, we draw all the tracksegments
		 * in ONE batch, using a general path.
		 * To handle this, one tracksegment must be promoted
		 * to the manager, that is, the one who actually
		 * draws them.
		 * 
		 * The first actual track segment recomp'd will be promoted.
		 * The leader is the first child or track segment.
		 * It will herald a recomp and clear the path.
		 *  
		 */
		if (gp == null || leader == this) {
			gp = new GeneralPath();
			leader = this;
		}
		
		if (gpManager == null && this.getClass().getName().contains("TrackSegment"))
			gpManager = this;
			
		
		if (this.getClass().getName().contains("TrackSegment"))
			gp.append(coords, false);
	}
	
	/**
	 * Set the line end cap based on next segment... Could be round or flat.
	 */
	protected void setCap() {
		cap = BasicStroke.CAP_ROUND;
		if (dests[0] != null && (dests[0].carHidden() || dests[0] instanceof Crossing)) cap = BasicStroke.CAP_BUTT;
		if (dests[1] != null && (dests[1].carHidden() || dests[1] instanceof Crossing)) cap = BasicStroke.CAP_BUTT;
	}
	
	
	

	@Override
	public RailSegment dest(RailSegment source) {
		// a simple line has two ends.  whichever end is the source,
		// the other is the dest	
		// however, the ends may be null

		if (source == null) {
			if (dests[0] == null) return dests[1];
			if (dests[1] == null) return dests[0];
			if (dests[0] == dests[1]) return dests[0];
			return null;
		}
		

		if (dests[0] != null)
			if (dests[0] == source) return dests[1];
		

		if (dests[1] != null)
			if (dests[1] == source) return dests[0];
		

		return null;

	}
	@Override
	public Distance length() {
		
		return len;
	}

	

	

	@Override
	public Point2D getPoint(RailSegment start, double myPos) {
		Line2D c2 = coords;
		
		double x1 = c2.getX1();
		double y1 = c2.getY1();
		double x2 = c2.getX2();
		double y2 = c2.getY2();
		double p = myPos;
		if (dests[1] != null && start != null && dests[1] == start) {
			p = 1.0 - myPos;
		}
		if (dests[1] == null && start == null) {
			p = 1.0 - myPos;
		}
		return new Point2D.Double(x1+(x2-x1)*p, y1+(y2-y1)*p);

	}
	
	private static GeneralPath gp;

	@Override
	public void draw(int z, Graphics2D gc) {
		
		Paint p = gc.getPaint();




		if (z == 1) {
			
			
			gc.setPaint(Color.gray);
			
			gc.setStroke(railBedStroke);
			gc.draw(coords);
			/*if (gpManager == this) {
				
				gc.setStroke(railBedStroke);
				gc.draw(gp);
			}*/
			
			
			
		}
		if (z == 2) {
			

			gc.setPaint(Color.black);
			
			gc.setStroke(railStroke);
			gc.draw(coords);
			/*if (gpManager == this) {
				
				
				gc.setStroke(railStroke);
				gc.draw(gp);
			}*/
			
		}
		
		gc.setPaint(p);
	}

	private boolean canAnchor(RailSegment r, RailSegment r2) {
		// can we connect r to r2?
		// r = our current connection
		// r2 = the incoming (new) segment

		// all switch and 4-way manipulation must be done at the switch level
		if (r instanceof Switch) return false;
		if (r instanceof FourWay) return false;
		
		// a switch forming connection may only be performed with all track segments
		if (r != null && r2 != null) return r instanceof TrackSegment && r2 instanceof TrackSegment;
				
		// should be fine
		
		return true;
	}
	
	@Override
	public SegmentEditPoint createSEP(int ptIdx, RailSegment anchor) {
		if (canAnchor(dests[ptIdx], anchor)) return new TSEP(this, ptIdx);
		return null;
		
	}
	
	
	@Override
	public boolean canErase() {
		if (dests[0] != null && dests[0] instanceof Switch) return false;
		if (dests[1] != null && dests[1] instanceof Switch) return false;
		return true;
		
	}
	@Override
	public JPanel editPanel() {
		
		return new JPanel();
	}
	
	
	 
	
	
	// track segment edit point class
	
	/**
	 * Track segment edit point.
	 * Public because the edit canvas wants the ability to call a special
	 * disconnect method here.
	 */
	public class TSEP extends SegmentEditPoint {
		private TrackSegment mt;
		private RailSegment ot;
		private int ptIdx;
		
		// special behaviors for TSEP
		/**
		 * @return true if the segment was connected at this end and has been disconnected. 
		 */
		public boolean disconnect() {
			if (ot == null) return false;
			
			ot.update(mt, null);
			setDest(ptIdx, false, null);
			
			return true;
		}
		
		/**
		 * Create a track segment edit point.  Called from {@link TrackSegment#createSEP(int, RailSegment)}
		 * 
		 * @param t The track segment
		 * @param pt The point index
		 */
		public TSEP(TrackSegment t, int pt) {
			super(new RailSegment[] {t, t.dests[pt]}, pt, t.dests[pt] == null ? Color.red : Color.green);
			mt = t;
			ot = t.dests[pt];
			if (t instanceof EESegment) {
				EESegment et = (EESegment)t;
				if (ot == et.HES) ot = null;
			}
			ptIdx = pt;
		}
		
		@Override
		public boolean isAnchorSource() {
			return (ot == null);
			
			
		}
		@Override
		public RailSegment anchor(RailSegment r) {
			
			
			if (ot == null) setDest(ptIdx, false, r);
			else {
				// make switch
				// now we can do mt as base and te as first leg
				// or vice versa
				// find the angle between r and mt
				// if less than 90, te is the base otherwise mt is the base
				// simplier way (since these aren't right triangles)
				// check to see which end is closer to the other
				// end of the segments
				RailSegment a,b;
				
				// note that we can assume all segments are tracksegments
				// due to the anchoring requirements
				
				// starting point
				Point2D tp = pts[ptIdx]; 
				
				// mt other point
				Point2D mtop = pts[1 - ptIdx]; 
				
				// ot other point
				Point2D otp1 = ((TrackSegment)ot).coords.getP1();
				Point2D otp2 = ((TrackSegment)ot).coords.getP2();
				Point2D otop = otp1.equals(tp) ? otp2 : otp1;
				
				// r other point
				Point2D rp1 = ((TrackSegment)r).coords.getP1();
				Point2D rp2 = ((TrackSegment)r).coords.getP2();
				Point2D rop = rp1.equals(tp) ? rp2 : rp1;
				
				// is the r other point closer to ot op or mt op?
				if (rop.distance(otop) < rop.distance(mtop)) {
					a = mt;
					b = ot;
				} else {
					a = ot;
					b = mt;
				}
				
				Switch s = new Switch(a, b, r, tp);
				ot.update(mt, s);
				
				setDest(ptIdx, false, s);
				
					
				return s;
			}
			return null;
			
			
		}
		
	
	}
}
