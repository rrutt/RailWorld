package net.kolls.railworld.scripts;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.RailAccident;
import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.play.script.TrainActionListener;
import net.kolls.railworld.segment.Crossing;

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
 * Requires horn usage before moving and before entering a crossing.
 * 
 * @author Steve Kollmansberger
 */
public class HornRequired implements Script, TrainActionListener {

	/**
	 * Contains a list of all trains that have sounded horn, and when they did so.
	 */
	protected Map<Train, Date> whenHorn = new Hashtable<Train, Date>();
	
	/**
	 * Contains a list of all trains, and whether or not they are stopped (vel = 0)
	 */
	protected Map<Train, Boolean> isStopped = new Hashtable<Train, Boolean>();
	
	/**
	 * How many seconds can a train go
	 * since sounding horn
	 * before entering a crossing
	 * is unacceptable?
	 */
	protected static final int MAX_HORN_SECS = 5;
	
	private PlayFrame mpf;
	
	@Override
	public String toString() {
		return "Mandatory Horn Usage";
	}
	
	@Override
	public void init(PlayFrame pf) {
		pf.jdb.sm.addTrainActionListener(this, null, "horn");
		pf.jdb.sm.addTrainActionListener(this, null, "step");
		mpf = pf;

	}

	@Override
	public RailSegment[] modifySegments(RailSegment[] lines) {
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] instanceof Crossing) {
				lines[i] = new HornCrossing( (Crossing)lines[i]);
			}
		}
		return lines;
		
	}

	@Override
	public boolean playFrameAction(String action) {
		return false;
	}

	@Override
	public void load(Map<String, String> data) {
		

	}

	@Override
	public Object newInstance() {
		return new HornRequired();
	}

	@Override
	public Map<String, String> save() {
		// could be done, but too lazy
		// unlikely to make a huge difference
		return null;
	}
	
	/**
	 * Checks if the train has blown the horn within the alloted time.
	 * In some cases, a train might blow the horn and then be immediately
	 * replaced, like a reverse signal.
	 * So if this train is not on file and
	 * ANY train has blown the horn in the last second, that's also ok.
	 * 
	 * @param t
	 * @return true if the train is ok to proceed.
	 */
	protected boolean checkForHorn(Train t) {
		boolean ok = false;
		
	
		if (whenHorn.containsKey(t)) {
			Date d = whenHorn.get(t);
			long secs = ((new Date()).getTime() - d.getTime()) / 1000;
			if (secs <= MAX_HORN_SECS) {
				ok = true;
			}
			
		} else {
			for (Date d : whenHorn.values()) {
				long secs = ((new Date()).getTime() - d.getTime());
				if (secs <= 1000) {
					ok = true;
				}
			}
		}
		
		return ok;
	}
	
	@Override
	public boolean trainAction(final Train t, String action) {
		
		if (action.equals("horn")) {
			whenHorn.put(t, new Date());
		}
		if (action.equals("step")) {
			if (t.vel() > 0 && isStopped.containsKey(t) && isStopped.get(t)) {
				// just started moving, check for horn
				if (!checkForHorn(t)) {
					whenHorn.put(t, new Date()); // so it won't get thrown twice
					
					ScriptManager.DeferIntoStep(mpf, t, new Runnable() {
						@Override
						public void run() {
							throw new NoHornStart(t, t.pos.getPoint());
						}
					});
				
				}
			}
			isStopped.put(t, t.vel() == 0);
				
		}
		return false;
	
	}
	
	@Override
	public boolean onByDefault() {
		return false;
	}

	/**
	 * A crossing which throws a RailAccident if a train
	 * enters and hasn't blown the horn recently enough.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	public class HornCrossing extends Crossing {

		private HashSet<Train> okedTrains = new HashSet<Train>();
		
		
		public HornCrossing(Crossing c) {
			super(c.getDest(POINT_BEGIN), c.getDest(POINT_END), c.getCoords());
			
			getDest(Crossing.POINT_BEGIN).update(c, this);
			getDest(Crossing.POINT_END).update(c, this);
		}
		
		
		
		@Override
		public void enter(final Train t) {
			
			boolean ok = false;
			
			// train is already here, ignore
			if (okedTrains.contains(t) || t.vel() == 0) {
				ok = true;
			} else ok = checkForHorn(t);
			
			
			if (!ok) {
				whenHorn.put(t, new Date()); // so it won't get thrown twice
				
				ScriptManager.DeferIntoStep(mpf, t, new Runnable() {
					@Override
					public void run() {
						throw new NoHornCrossing(t, t.pos.getPoint());
					}
				});
				
			}
			
			
			super.enter(t);
		}
		
		@Override
		public void draw(int z, Graphics2D gc) {
			if (z == 4) {
				// one step delayed
				okedTrains.clear();
				okedTrains.addAll(trains());
			}
			
			super.draw(z, gc);
			
		}
	}
	
	
	/**
	 * An accident used when the train enters a crossing without having sounded
	 * the horn.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	@SuppressWarnings("serial")
	public class NoHornCrossing extends RailAccident {
		/**
		 * Create a "no horn" crossing accident.  Just calls the super constructor.
		 * 
		 * @param first First train
		 * @param p Location of accident
		 */
		public NoHornCrossing(Train first, Point2D p) {
			super(first, null, p);
		}
		
		@Override
		public String midbody() {
			return "failed to sound the horn before entering an at-grade crossing and struck a vehicle";
		}

		@Override
		public String title() {
			return "TRAIN/VEHICLE COLLISION";
		}
	}
	
	
	/**
	 * An accident used when the train begins moving without sounding the horn.
	 * 
	 * @author Steve Kollmansberger
	 *
	 */
	@SuppressWarnings("serial")
	public class NoHornStart extends RailAccident {
		/**
		 * Create a "no horn" start accident.  Just calls the super constructor.
		 * 
		 * @param first First train
		 * @param p Location of accident
		 */
		public NoHornStart(Train first, Point2D p) {
			super(first, null, p);
		}
		
		@Override
		public String midbody() {
			return "failed to sound the horn before starting movement and struck a person";
		}

		@Override
		public String title() {
			return "TRAIN/PERSON COLLISION";
		}
	}


	
	
}
