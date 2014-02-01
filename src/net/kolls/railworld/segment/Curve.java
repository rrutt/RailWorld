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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import net.kolls.railworld.Distance;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.edit.SegmentEditPoint;

/**
 * A curved track segment represented using a cubic curve.  
 * The system finds sub-lines using a flatness of 5.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Curve extends RailSegment {

	private static final double FLATNESS = 1;
	
	/**
	 * The curve itself
	 */
	protected QuadCurve2D cc;
	
	/**
	 * Line begin point
	 */
	public static final int POINT_BEGIN = 0;
	/**
	 * Line end point
	 */
	public static final int POINT_END = 1;
	/**
	 * Control point 1
	 */
	public static final int POINT_CP1 = 2;
	/**
	 * Control point 2
	 */
	//public static final int POINT_CP2 = 3;
	
	/**
	 * Length of this curve, as calculated by sub lines
	 */
	protected Distance len;
	
	/**
	 * The sequence of points in the sublines
	 */
	protected Point2D[] seq;
	/**
	 * The length of each subline
	 */
	protected double[] lens;
	
	/**
	 * The rail bed pen
	 */
	protected Stroke railBedStroke;
	
	/**
	 * The rail line pen
	 */
	protected Stroke railStroke;

	
	/**
	 * Create a new curve segment. The beginning and ending segment must
	 * both be {@link TrackSegment}s.
	 * 
	 * @param bg Beginning rail segment
	 * @param en Ending rail segment
	 * @param crds Curve
	 */
	public Curve(RailSegment bg, RailSegment en, QuadCurve2D crds) {
		dests = new RailSegment[2];
		dests[0] = bg; 
		dests[1] = en;
		
		pts = new Point2D[3];
		pts[0] = crds.getP1();
		pts[1] = crds.getP2();
		pts[2] = crds.getCtrlPt();
		//pts[3] = crds.getCtrlP2();
		
		recomp();
	}
	
	@Override
	public boolean canErase() {
		return true;
	}

	
	
	@Override
	protected SegmentEditPoint createSEP(int ptIdx, RailSegment attach) {
		if (attach != null && ptIdx > 1) return null;
		
		if (attach == null || (dests[ptIdx] == null && attach instanceof TrackSegment))
				return new CSEP(this, attach, ptIdx);
		return null;
	}

	@Override
	public RailSegment dest(RailSegment source) {
		if (source == dests[0]) return dests[1];
		if (source == dests[1]) return dests[0];
		return null;
	}

	@Override
	public void draw(int z, Graphics2D gc) {
		Paint p = gc.getPaint();




		if (z == 1) {
			
			
			gc.setPaint(Color.gray);
			
			gc.setStroke(railBedStroke);
			gc.draw(cc);
			
			
		}
		if (z == 2) {
			
			
			gc.setPaint(Color.black);
			
			gc.setStroke(railStroke);
			gc.draw(cc);
			
		}
		
		gc.setPaint(p);

	}

	@Override
	public JPanel editPanel() {
		return new JPanel(); // empty
	}

	@Override
	public Point2D getPoint(RailSegment start, double myPos) {
		// find which sub-segment we are on
		if (start == dests[1]) myPos = 1.0 - myPos;
	
		
		// cp will be the pos (0-1) of the start of the subsegment
		// np will be the pos at the end
		// this should "wrap around" myPos
		// e.g. subsegment starts at 0.2, ends at 0.25
		// mypos is 0.22
		
		// although we using actual length values here!
		
		double cp = 0;
		double np = 0;
		double mp = myPos * len.pixels();
		int i;
		for (i = 0; i < lens.length; i++) {
			np += lens[i];
			if (np > mp) break;
			cp = np;
		}
		
		if (i >= lens.length) {
			// at the end
			return seq[seq.length - 1];
		}
		
		// then we find where in the subsegment we are
		// so if above example
		// we're about halfway through
		// (0.22 - 0.2) / (0.25 - 0.2) = 0.02 / 0.05 = about half
		// these values are actual distances not %s
		// but the concept is the same
		double subPos = (mp - cp) / (np - cp);
		
		
		// use linear interpolation
		double x1 = seq[i-1].getX();
		double y1 = seq[i-1].getY();
		double x2 = seq[i].getX();
		double y2 = seq[i].getY();
		return new Point2D.Double(x1+(x2-x1)*subPos, y1+(y2-y1)*subPos);
		
		
	}

	@Override
	public Distance length() {
		return len;
	}

	private double linearDist(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
        double dy = y1 - y2;
        double d = Math.sqrt(dx*dx + dy*dy);
        return d;
	}
	
	@Override
	public void recomp() {
		cc = new QuadCurve2D.Double(pts[POINT_BEGIN].getX(), pts[POINT_BEGIN].getY(),
				pts[POINT_CP1].getX(), pts[POINT_CP1].getY(),
				//pts[POINT_CP2].getX(), pts[POINT_CP2].getY(),
				pts[POINT_END].getX(), pts[POINT_END].getY());
				

		PathIterator p = cc.getPathIterator(null, FLATNESS);
		ArrayList<Point2D> lps = new ArrayList<Point2D>();
		
		double d = 0;
		
		double av[] = new double[6];
		while (!p.isDone()) {
			int pt = p.currentSegment(av);
			
			switch (pt) {
			case PathIterator.SEG_CLOSE:
				break;
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_MOVETO:
				lps.add(new Point2D.Double(av[0], av[1]));
			
				break;
			default:
				throw new RuntimeException("Curve returned non-linear points");
			}
			
			p.next();
			
		}
		
		seq = lps.toArray(new Point2D[0]);
		
		lens = new double[seq.length];
		lens[0] = 0;
		for (int i = 1; i < seq.length; i++) {
			double px = seq[i-1].getX();
			double py = seq[i-1].getY();
			double dist = linearDist(px, py, seq[i].getX(), seq[i].getY());
			lens[i] = dist;
			d += dist;
		}
		
		len = new Distance(d, Distance.Measure.PIXELS);
		
		railBedStroke = new BasicStroke(RAIL_BED_WIDTH.iPixels());
		railStroke = new BasicStroke(RAIL_WIDTH.iPixels());
		
	
	}

	/**
	 * Edit point for setting up cubic curves
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class CSEP extends SegmentEditPoint {
		private Curve mt;
		private RailSegment ot;
		private int ptIdx;
		
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
		 * Construct a curve segment edit point.  These are only
		 * constructed by {@link Curve#createSEP(int, RailSegment)}.
		 * @param t Curve segment
		 * @param an Railsegment connecting
		 * @param pt Point index
		 */
		public CSEP(Curve t, RailSegment an, int pt) {
			super(new RailSegment[] {t, pt > 1 ? null : t.dests[pt]}, pt, pt > 1 ? Color.green : (t.dests[pt] == null ? Color.red : Color.green));
			mt = t;
			if (pt <= 1)
				ot = t.dests[pt];
			else 
				ot = null;
			ptIdx = pt;
			
			
		}
		
		@Override
		public boolean isAnchorSource() {
			System.out.println(ptIdx);
			return (ptIdx <= 1 && ot == null);
			
			
		}
		@Override
		public RailSegment anchor(RailSegment r) {
			
			
			if (ot == null && ptIdx <= 1) setDest(ptIdx, false, r);
			
			return null;
			
			
		}
		
	
	}
	
}
