package net.kolls.railworld.scripts.missions;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.kolls.railworld.CLoc;
import net.kolls.railworld.Car;
import net.kolls.railworld.DLoc;
import net.kolls.railworld.Distance;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.car.Boxcar;
import net.kolls.railworld.car.Engine;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.RailAccident;
import net.kolls.railworld.play.script.DrawListener;
import net.kolls.railworld.play.script.Mission;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.scripts.missions.support.StatusLine;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.LUSegment;

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
 * A mission where the player picks up a bunch of cars
 * and takes them away as quickly as possible.
 * 
 * @author Steve Kollmansberger
 */
public class Brewsky implements Mission {

	private PlayFrame mpf;
	private StatusLine sp;
	
	private net.kolls.railworld.scripts.Completer comp_scr;
	
	
	@Override
	public JPanel briefing() {
		JPanel br = new JPanel();
		br.setLayout(new BorderLayout());
		
		JEditorPane jl = new JEditorPane();
		jl.setContentType("text/html");
		jl.setText("<html><style>p { margin-bottom: 10px; margin-top: 0; }</style><p>A Superbowl beer shortage is looming.  The Tumwater brewery has lots of beer ready to move, but it needs to get out of the packing center and on the road quickly.</p> <b>Goal:</b><p>Collect all cars into a single train and leave the map as quickly as possible.</p><b>Scoring:</b><p>100 points max; points decrease as time goes on.</p></html>");
		jl.setEditable(false);
		jl.setCaretPosition(0);
		
		br.add(new JScrollPane(jl));
		return br;
		
	}

	@Override
	public ScriptManager createScriptManager() {
		ScriptManager sm = new ScriptManager();
		sm.add(new net.kolls.railworld.scripts.Timer());
		sm.add(new net.kolls.railworld.scripts.SpringSwitches());
		comp_scr = new net.kolls.railworld.scripts.Completer();
		sm.add(comp_scr);
		sm.add(this);
		
		sm.mission = this;
		return sm;
	}

	@Override
	public void railAccident(RailAccident ra) {
		JOptionPane.showMessageDialog(mpf, "Mission failed due to rail accident", "Mission Failure", JOptionPane.INFORMATION_MESSAGE);
		
		mpf.stop(null);

	}

	@Override
	public String rwmFilename() {
		return "tumwater.rwm";
	}
	
	private int points() {
		int points = (int)( 200.0 / ((double)mpf.gl.elapsed / 1000.0 / 60.0));
		return Math.min(100, points);
	
	}

	@Override
	public void init(PlayFrame pf) {
		sp = new StatusLine();
		pf.jdb.sm.addDrawListener(sp);

		pf.jdb.sm.addDrawListener(new DrawListener() {

			@Override
			public void draw(Graphics2D gc, Rectangle onScreen) {
				// don't care about drawing, just want to update the score regularly
				
				if (points() < 1) {
					JOptionPane.showMessageDialog(mpf, "Mission failed: you took too long to get the beer delivered.", "Mission Failure", JOptionPane.INFORMATION_MESSAGE);
					
					mpf.stop(null);
				}
				
				sp.status = Integer.toString(points()) + " point" + (points() == 1 ? "" : "s");
				
			}
			
		});
		
		
		pf.jdb.trains.addListDataListener(new ListDataListener() {

			
			ArrayList<Train> trs = new ArrayList<Train>();
			
			public void reload() {
				trs.clear();
				for (Train t : mpf.jdb.trains)
					trs.add(t);
			}
			
			@Override
			public void contentsChanged(ListDataEvent arg0) {
				reload();
				
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				reload();
				
			}

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
				// check if all trains have left
				Train t = trs.get(arg0.getIndex0()); // can't retrieve from old list, must use backup list
				
				if (t.pos.orig instanceof EESegment) {
					if (mpf.jdb.trains.size() == 0) {
						int pts = points();
						JOptionPane.showMessageDialog(mpf, "Mission complete!\nThe beer is on its way to delivery, earning "+Integer.toString(pts)+" points.");
						mpf.stop(pts);
				
					} else {
						// 	if only one train leaves, that's bad, because there's only one engine!
						JOptionPane.showMessageDialog(mpf, "Mission failed: All the beer needs to be on one train.");
						mpf.stop(null);
					}
				
				} else reload();
			}
			
		});
		
		mpf = pf;
		
		// only add these trains if we are fresh, not load game
		
		if (mpf.gl.elapsed == 0) {
		
		for (int i = 0; i < mpf.jdb.la.length; i++) {
			// consider only empty LUsegments
			if (mpf.jdb.la[i] instanceof LUSegment) {
				if ( ((LUSegment)mpf.jdb.la[i]).canLU(new Boxcar())) {
					Train t = new Train(new Car[] {
							// weird syntax ever
							comp_scr.new CompletedCar(new Boxcar()),
							comp_scr.new CompletedCar(new Boxcar()),
							comp_scr.new CompletedCar(new Boxcar()),
							comp_scr.new CompletedCar(new Boxcar()),
							comp_scr.new CompletedCar(new Boxcar())
					});
					t.pos = new CLoc(mpf.jdb.la[i], null, 0.98);
					t.setBrake(true);
					t.getController().setTrainActionScriptNotify(mpf.jdb.sm);
					mpf.addTrain(t, false);
				
				}
			
			}
			
			if (mpf.jdb.la[i] instanceof EESegment) {
				
				if ( ((EESegment)mpf.jdb.la[i]).label.equals("Southeast")) {
					CLoc st = new CLoc(mpf.jdb.la[i], ((EESegment)mpf.jdb.la[i]).HES, 0);
					DLoc stp = st.segFwd(new Distance(1000, Distance.Measure.FEET));
					Train t = new Train(new Car[] { new Engine()});
					t.pos = stp.newLoc;
					t.setBrake(true);
					t.getController().setTrainActionScriptNotify(mpf.jdb.sm);
					t.followMeOnce = true;
					mpf.addTrain(t, true);
					
				}
					
			}
		}
		}
		
		
		mpf.hideTrainButtons(null);
		
		
		
		
	}

	@Override
	public RailSegment[] modifySegments(RailSegment[] lines) {
		// none
		
		
		
		return lines;
	}

	@Override
	public boolean onByDefault() {
		
		return false;
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
		
		return new Brewsky();
	}

	@Override
	public Map<String, String> save() {
		
		// nothing to add to the save queue
		return null;
	}
	
	@Override
	public String toString() {
		return "Brewsky Run";
	}

}
