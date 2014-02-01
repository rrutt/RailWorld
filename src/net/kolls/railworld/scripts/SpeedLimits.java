package net.kolls.railworld.scripts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;

import net.kolls.railworld.Distance;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.edit.SegmentEditPoint;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.RailAccident;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.Crossing;
import net.kolls.railworld.segment.Curve;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.TrackSegment;
import net.kolls.railworld.segment.sp.Green;

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
 * Provides for speed limits based on turns. Creates
 * an accident if speed limit exceeded.
 * 
 * @author Steve Kollmansberger
 */
public class SpeedLimits implements Script {

	private PlayFrame mpf;
	
	@Override
	public String toString() {
		return "Speed Limits";
	}
	
	@Override
	public void init(PlayFrame pf) {
		mpf = pf;
	}

	
	// the story is
	// I want to break angles down into 180 degree bits
	// and find out how different they are
	// so two lines facing direcly opposite should be 0
	// the "most" we should be able ot get is 90 degrees
	private double ror(RailSegment a) {
		double x = RailCanvas.lineAngle(new Line2D.Double(a.getPoint(0), a.getPoint(1)));
		if (x < 0)
			x += Math.PI;
		if (x > Math.PI)
			x -= Math.PI;
		
		return x;
		
	}
	
	
	
	private SpeedLimitSegment trackSegments(RailSegment a, RailSegment b) {
		double ror_a, ror_b;
		SpeedLimitSegment n;
		
		ror_a = ror(a);
		ror_b = ror(b);
		
		
		ror_a = Math.min(ror_a, Math.PI - ror_a);
		ror_b = Math.min(Math.PI - ror_b, ror_b);
		
		double c = Math.abs(ror_a - ror_b);
		n = null;
		
		if (c >= 0.4) {
			n = new SpeedLimitSegment(a, b, 15);
		} else if (c >= 0.2) {
			n = new SpeedLimitSegment(a, b, 25);
		} else if (c >= 0.1) {
			n = new SpeedLimitSegment(a, b, 35);
		}

	
		return n;
	}
	
	private int curveSegment(Curve a) {
		
		int limit = 0;
		
		
		double len = a.length().pixels();
		
		Point2D p1 = a.getPoint(0);
		Point2D p2 = a.getPoint(1);
		double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        double d = Math.sqrt(dx*dx + dy*dy);
        
        double c = len / d;
		
		
		if (c >= 1.1) {
			limit = 15;
		} else if (c >= 1.05) {
			limit = 25;
		} else if (c >= 1) {
			limit = 35;
		}
System.out.println(c);

	
		return limit;
	}
	
	@Override
	public RailSegment[] modifySegments(RailSegment[] lines) {
		// install speed limits
		ArrayList<RailSegment> vl = new ArrayList<RailSegment>();
		RailSegment a, b;
		SpeedLimitSegment n;
		
		
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] instanceof Curve) {
				Curve ca = (Curve)lines[i];
				int limit = curveSegment(ca);
				if (limit > 0) {
					a = lines[i].getDest(0);
					b = lines[i].getDest(1);
					n = new SpeedLimitSegment(ca, a, limit);
					a.update(ca, n);
					ca.update(a, n);
					vl.add(n);
					
					n = new SpeedLimitSegment(ca, b, limit);
					b.update(ca, n);
					ca.update(b, n);
					vl.add(n);
					
				}
				
			}
			if (lines[i] instanceof TrackSegment) {
				a = lines[i];
				b = lines[i].getDest(0);
				if (a instanceof EESegment || a instanceof Crossing)
					continue;
				
				if (b instanceof TrackSegment && !(b instanceof EESegment || b instanceof Crossing) ) {
					n = trackSegments(a, b);
					if (n != null) {
						vl.add(n);
						a.update(b, n);
						b.update(a, n);
					} 
					
				}
				
				b = lines[i].getDest(1);
				
				if (b instanceof TrackSegment && !(b instanceof EESegment || b instanceof Crossing)) {
					n = trackSegments(a, b);
					if (n != null) {
						vl.add(n);
						a.update(b, n);
						b.update(a, n);
					}
					
				}
			}
		}
		
		RailSegment[] nl = new RailSegment[lines.length + vl.size()];
		System.arraycopy(lines, 0, nl, 0, lines.length);
		for (int i = 0; i < vl.size(); i++)
			nl[lines.length + i] = vl.get(i);
		
		return nl;
	}

	@Override
	public boolean playFrameAction(String action) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load(Map<String, String> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object newInstance() {
		// TODO Auto-generated method stub
		return new SpeedLimits();
	}

	@Override
	public Map<String, String> save() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onByDefault() {
		return false;
	}
	
	/**
	 * A segment which restricts speed of trains on it.
	 * Throws a RailAccident if a train enter's with excess speed.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class SpeedLimitSegment extends RailSegment {

		
		private final Distance zd = new Distance(0, Distance.Measure.FEET);
		private final Distance size = new Distance(15, Distance.Measure.FEET);
		
		/**
		 * The speed limit, in MPH
		 */
		protected int limit;
		
		/**
		 * The begin segment.
		 */
		public static final int POINT_BEGIN = 0;
		
		/**
		 * The destination segment.
		 */
		public static final int POINT_END = 1;
		
		/**
		 * Create a speed limit point. 
		 *  Any train entering or remaining in the point must not exceed
		 *  the posted limit.
		 *  
		 * @param begin The begin (origin) segment
		 * @param end The destination segment
		 */
		public SpeedLimitSegment(RailSegment begin, RailSegment end, int limit) {
			dests = new RailSegment[2];
			this.dests[0] = begin;
			this.dests[1] = end;
			
			pts = new Point2D[1];
			pts[0] = begin.getPoint(end, 0);
			
			this.limit = limit;
			recomp();
			
				
			
		}
		
		// this will not appear in the editor
		// so don't worry about it
		@Override
		public boolean canErase() {
			return false;
		}

		@Override
		protected SegmentEditPoint createSEP(int ptIdx, RailSegment attach) {
			return null;
		}

		
		@Override
		public RailSegment dest(RailSegment source) {
			if (source == dests[0]) return dests[1]; else return dests[0];
		}

		@Override
		public void draw(int z, Graphics2D gc) {
			if (z == 4) {
				Point2D p2 = pts[0];
				
				RailCanvas.drawOutlineFont(gc, (int)p2.getX(), (int)p2.getY(), 
						Integer.toString(limit), size.iPixels(), Color.white, 0, true);
				
				
			}
			
		}

		@Override
		public JPanel editPanel() {
			return null;
		}

		@Override
		public Point2D getPoint(RailSegment start, double myPos) {
			return pts[0];
		}

		@Override
		public Distance length() {
			return zd;
		}

		@Override
		public void recomp() {	
		}
		
		@Override
		public boolean singleton() {
			return true;
		}
		
		@Override
		public boolean isDynamic() {
			return true;
		}
		
		private Train didThrow;
		
		@Override
		public void enter(final Train t) {
			if (didThrow == t)
				return;
				
			// need didThrow because enter occurs again
			// when preparing the "scene"
			if (t.vel() > limit + 2) { // found that actual speed up to 2 mph faster than selected, indicates 1 because of rounding
				didThrow = t;
				
				ScriptManager.DeferIntoStep(mpf, t, new Runnable() {
					@Override
					public void run() {
						throw new TooFast(t, pts[0]);
					}
				});
				
			}
		}
		
		@Override
		public String mouseOver(Point2D pos) {
			if (pts[0].distance(pos) <= MOUSE_NEAR.iPixels()) {
				return "Speed Limit "+Integer.toString(limit)+ " MPH";
			}
			return null;
		}
	}
	
	/**
	 * A speed limit over-run.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class TooFast extends RailAccident {

		/**
		 * Create a "too fast" accident.  Just calls the super constructor.
		 * 
		 * @param first First train
		 * @param p Location of accident
		 */
		public TooFast(Train first, Point2D p) {
			super(first, null, p);
		}
		
		@Override
		public String midbody() {
			return "exceeded track speed limit and derailed";
		}

		@Override
		public String title() {
			return "EXCESS SPEED DERAILMENT";
		}

	}
}
