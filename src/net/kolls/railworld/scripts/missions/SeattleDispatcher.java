package net.kolls.railworld.scripts.missions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.kolls.railworld.CLoc;
import net.kolls.railworld.Car;
import net.kolls.railworld.Factories;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Train;
import net.kolls.railworld.car.Engine;
import net.kolls.railworld.car.Passenger;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.RailAccident;
import net.kolls.railworld.play.TrainCreator;
import net.kolls.railworld.play.script.DrawListener;
import net.kolls.railworld.play.script.Mission;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.scripts.Completer.CompletedCar;
import net.kolls.railworld.scripts.missions.support.CountDownTimer;
import net.kolls.railworld.scripts.missions.support.FadePrinter;
import net.kolls.railworld.scripts.missions.support.StatusLine;
import net.kolls.railworld.scripts.missions.support.TimeQueue;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.LUSegment;
import net.kolls.railworld.segment.TrackSegment;
import net.kolls.railworld.tc.AutoControl;

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
 * A mission where the player acts as a dispatcher
 * in Seattle.  In this mission,  they use signals
 * to control passenger and freight trains.
 * 
 * @author Steve Kollmansberger
 */
public class SeattleDispatcher implements Mission {

	public final long TOTAL_MISSION_TIME =  15 * 60 * 1000; // 15 min
	
	private Random r = new Random();
	private PlayFrame mpf;
	private DispatchWindow dw;
	private FadePrinter fp;
	private StatusLine sp;
	private int points = 0;
	private int suc_pas = 0, suc_frei = 0;
	
	private CountDownTimer cdt;
	
	private RailSegment WESTPLATFORM, EASTPLATFORM;
	
	private EESegment SOUTHWEST;
	private EESegment SOUTHEAST;
	private EESegment NORTHWEST;
	private EESegment NORTHEAST;
	
	private Map<Train, EESegment> destinations = new Hashtable<Train, EESegment>();
	
	
	@Override
	public JPanel briefing() {
		JPanel br = new JPanel();
		br.setLayout(new BorderLayout());
		
		JEditorPane jl = new JEditorPane();
		jl.setContentType("text/html");
		jl.setText("<html><style>p { margin-bottom: 10px; margin-top: 0; }</style><p>Seattle King Street Station is a busy terminal for passenger travel, both Amtrak and local commuter rail.  The tracks also handle mainline freight traffic.  Can you keep the trains moving?</p> <b>Goal:</b><p>Route as many trains as possible in 15 minutes.  Passenger trains must be unloaded and re-loaded before exiting.</p><b>Scoring:</b><p>Passenger trains: 2 points<br>Freight trains: 1 pt<br>Wrong exit: -1 point<br>Non-reloaded passenger train: -1 point</p></html>");
		jl.setEditable(false);
		jl.setCaretPosition(0);
		
		br.add(new JScrollPane(jl));
		return br;
		
	}

	@Override
	public ScriptManager createScriptManager() {

		cdt = new CountDownTimer(TOTAL_MISSION_TIME);
		cdt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dw.setVisible(false);
				JOptionPane.showMessageDialog(mpf, "Mission complete!\nYou routed " + Integer.toString(suc_pas) + " passenger trains and " + Integer.toString(suc_frei) + " freight trains, earning "+Integer.toString(points)+" points.");
				mpf.stop(points);
				
			}
			
		});
		
		ScriptManager sm = new ScriptManager();
		sm.add(cdt);
		sm.add(new net.kolls.railworld.scripts.SpringSwitches());
		sm.add(new net.kolls.railworld.scripts.Completer());
		sm.add(this);
		
		sm.mission = this;
		return sm;
	}

	@Override
	public void railAccident(RailAccident ra) {

		JOptionPane.showMessageDialog(mpf, "Mission failed due to rail accident", "Mission Failure", JOptionPane.INFORMATION_MESSAGE);
		dw.setVisible(false);
		mpf.stop(null);
		

	}

	
	
	private EESegment selectDestSegment(EESegment origin, boolean canReverse) {
		ArrayList<EESegment> ees = new ArrayList<EESegment>();
		ees.add(SOUTHWEST);
		ees.add(SOUTHEAST);
		ees.add(NORTHWEST);
		ees.add(NORTHEAST);
		
		ees.remove(origin);
		if (!canReverse) {
			if (origin == SOUTHWEST)
				ees.remove(SOUTHEAST);
			
			if (origin == SOUTHEAST)
				ees.remove(SOUTHWEST);
			
			if (origin == NORTHWEST)
				ees.remove(NORTHEAST);
			
			if (origin == NORTHEAST)
				ees.remove(NORTHWEST);
		}
		
		return ees.get(r.nextInt(ees.size()));
		
	}
	
	protected class SeattleDispatchLDL implements ListDataListener {

		ArrayList<Train> trs = new ArrayList<Train>();
		int saveIdx = -1;
		Train saveTrn;
		
		public void reload() {
			trs.clear();
			for (Train t : mpf.jdb.trains)
				trs.add(t);
		}
		
		@Override
		public void contentsChanged(ListDataEvent e) {
			reload();
			
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			
			if (saveIdx == e.getIndex0() && saveIdx > -1) {
				EESegment dest = destinations.get(saveTrn);
				if (dest != null) {
					destinations.remove(saveTrn);
					reload();
									
					destinations.put(trs.get(e.getIndex0()), dest);
				}
				saveIdx = -1;
			} 
			
			reload();
			
			
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			// check if departing train correct
			Train t = trs.get(e.getIndex0());
			if (t.pos.orig instanceof EESegment) {
				// otherwise it could be a reverse, etc.
				// check both dest and if loaded (pass. only)
				boolean loaded = true;
				boolean passenger = false;
				for (Car c : t.array()) {
					if (c instanceof Passenger) {
						loaded = false;
						passenger = true;
					}
					if (c instanceof CompletedCar) {
						// only passenger cars get completed
						passenger = true;
					}
				}
				int pt = 0;
				
				if (t.pos.orig == destinations.get(t) && loaded) {
					String msg = ((EESegment)t.pos.orig).label + " correct ";
					if (passenger) {
						msg += "+2 pts";
						pt = 2;
						suc_pas++;
					} else {
						msg += "+1 pt";
						pt = 1;
						suc_frei++;
					}
						
					fp.add(msg, Color.green);
				} else {
					String msg = ((EESegment)t.pos.orig).label + " incorrect ";
					 
					if (!loaded) {
						msg += "(no station stop) ";
						pt--;
					}
					
					if (destinations.get(t) != t.pos.orig) {
						msg += "(should have exited " + destinations.get(t).label + ") ";
						pt--;
					}
					msg += Integer.toString(pt) + "pt" + (pt == -1 ? "" : "s");
					
					fp.add(msg, Color.red);
				}
				
				points += pt;
				sp.status = Integer.toString(points) + " point" + (points == 1 ? "" : "s");
					
			} else {
				// why else would a train disappear?
				// maybe because it will be reversed
				// and replaced
				saveIdx = e.getIndex0();
				saveTrn = t;
			}
			
			
			
			reload();
			
			
			
		}
		
	}
	
	
	@Override
	public void init(PlayFrame pf) {
		mpf = pf;
		
		
		fp = new FadePrinter(pf.gl);
		pf.jdb.sm.addDrawListener(fp);
		
		sp = new StatusLine();
		pf.jdb.sm.addDrawListener(sp);
		
		pf.jdb.trains.addListDataListener(new SeattleDispatchLDL());
		
		// find key segments
		for (RailSegment rs : mpf.jdb.la) {
			if (rs instanceof EESegment) {
				EESegment ee = (EESegment)rs;
				if (ee.label.equals("Northwest"))
					NORTHWEST = ee;
				if (ee.label.equals("Southwest"))
					SOUTHWEST = ee;
				if (ee.label.equals("Northeast"))
					NORTHEAST = ee;
				if (ee.label.equals("Southeast"))
					SOUTHEAST = ee;
				
			}
			if (rs instanceof LUSegment) {
				
				if (rs.getDest(0) == null || rs.getDest(1) == null) {
					// it's one of the terminal (end line) loadable segment
					Train t;
					
					// make a slightly longer train on the longer extended segment
					// it's the one with a hidden adjoining segment
					// due to the bridge
					if (rs.dest(null).carHidden()) {
						t = new Train(new Car[] {
								new Passenger(), new Passenger(), new Passenger(), new Passenger(), new Passenger(), new Engine()
						});
						
						EASTPLATFORM = rs;
					} else {
						t = new Train(new Car[] {
								new Passenger(), new Passenger(), new Passenger(), new Engine()
						});
						WESTPLATFORM = rs;
					}
					
					CLoc ep = new CLoc(rs, null, 0.15);
					t.pos = ep.reverse();
					t.reverse = true;
					
					mpf.addTrain(t, false);

					AutoControl ac = new AutoControl();

					ac.setMyInfo("Waiting to Load");
					ac.setSyncToClick(false);
					t.setController(ac);
					t.getController().setTrainActionScriptNotify(mpf.jdb.sm);

				}
			} else if (rs instanceof TrackSegment) {
				// there is also a terminal (end) non lu segment
				if (rs.getDest(0) == null || rs.getDest(1) == null) {

					Train t;
					
					t = new Train(new Car[] {
							new Passenger(), new Passenger()
					});
					
					
					CLoc ep = new CLoc(rs, null, 0.25);
					t.pos = ep.reverse();
					
					mpf.addTrain(t, false);

					AutoControl ac = new AutoControl();

					ac.setSyncToClick(false);
					t.setController(ac);
					t.getController().setTrainActionScriptNotify(mpf.jdb.sm);

				}
			}
			
		} 
		
		
		
		
		dw = new DispatchWindow();
		mpf.jdb.sm.addDrawListener(dw);
		mpf.hideTrainButtons(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dw.setVisible(true);
				
				// restore from minimization
				if (dw.getExtendedState() == JFrame.ICONIFIED)
					dw.setExtendedState(JFrame.NORMAL);
				
			}
			
		});
		pf.gl.runInLoop(new Runnable() {

			@Override
			public void run() {
				dw.setVisible(true);
				dw.start();
			}
			
		});
		
		
		
		

	}

	@Override
	public RailSegment[] modifySegments(RailSegment[] lines) {
		return lines;
	}

	@Override
	public boolean onByDefault() {
		
		return false;
	}

	@Override
	public boolean playFrameAction(String action) {

		if (action.equals("Quit")) {
			dw.setVisible(false);
		}
		if (action.equals("Save")) {
			JOptionPane.showMessageDialog(mpf, "Save not supported with this mission yet.");
			return true;
		}
		return false;
	}

	@Override
	public void load(Map<String, String> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object newInstance() {
		return new SeattleDispatcher();
	}

	@Override
	public Map<String, String> save() {
		// just need to save current and pending dispatchable trains
		// and dest for each train
		// and points
	
		throw new RuntimeException("not implement");
		//return null;
	}

	@Override
	public String rwmFilename() {
		return "seattle.rwm";
	}
	
	@Override
	public String toString() {
		return "Seattle Dispatcher";
	}


	
	
	@SuppressWarnings("serial")
	protected class DispatchWindow extends JFrame implements DrawListener {
		
		public DispatchPanel nw, ne, sw, se, pw, pe;
		
		public TimeQueue<DispatchPanel> dps = new TimeQueue<DispatchPanel>();
		
		public boolean hasShownOneMinuteWarning = false;
		
		@Override
		public void draw(Graphics2D gc, Rectangle onScreen) {
			// no drawing ,just check action queue
			
			TimeQueue<DispatchPanel>.TimeValue tv = dps.poll(mpf.gl.elapsed);
			if (tv != null) {
				DispatchPanel next = tv.value;
				
				next.predeploy();
				
				
				fp.add("Train at " + tv.value.title + " now ready", Color.white);
			}
			
			
			if (cdt.countDownFrom - mpf.gl.elapsed <= 60000 && !hasShownOneMinuteWarning) {
				hasShownOneMinuteWarning = true;
				fp.add("One minute left! Move those trains!", Color.yellow);
			
			}
		}
		
		private long delay(int factor) {
			return (long)r.nextInt(factor) + factor + mpf.gl.elapsed;
		}
		
		public void start() {
			dps.add(delay(10000), nw);
			dps.add(delay(30000), se);
			dps.add(delay(50000), ne);
			dps.add(delay(70000), sw);
			
			// deploy first platform at 5 min, second at 10 min
			// randomize +/- 1 min
			dps.add( (5 * 60 * 1000) + r.nextInt(120000) - 60000, pw);
			dps.add( (10 * 60 * 1000) + r.nextInt(120000) - 60000, pe);
		}
		
		public DispatchPanel createRepeatDispatchPanel(String title, final EESegment orig_seg) {
			final DispatchPanel dp = new DispatchPanel(title) {
				public EESegment _orig_seg;
				public void deployed() {
					dps.add(delay(50000), this);
					
					_t.getController().setTrainActionScriptNotify(mpf.jdb.sm);
					mpf.addTrain(_t, true);
					
					
				}
				public void predeploy() {
					
						_orig_seg = orig_seg;
						
						boolean ispas = r.nextBoolean();
						Car[] trn;
						if (ispas) {
							// passenger only
							// may or may not have engine on both ends
							
							// 3 - 6 pass cars
							// 1 - 2 engines
							trn = new Car[r.nextInt(5) + 4];
							trn[0] = new Engine();
							for (int i = 1; i < trn.length; i++) {
								trn[i] = new Passenger();
								if (r.nextDouble() < 0.8) // mostly loaded
									trn[i].load();
							}
							if (trn.length > 5)
								trn[trn.length - 1] = new Engine();
								
							
						} else {
							// random assorted freight
							ArrayList<Car> sources = new ArrayList<Car>();
							ArrayList<Car> at = Factories.cars.allTypes();
							for (int i = 0; i < at.size(); i++) {
								Car c = at.get(i);
								
								if (c.isLoadable() && c.canUserCreate() && !(c instanceof Passenger)) {
									Car c2 = (Car)c.newInstance();
									c2.load();
									sources.add(c2);
									
									c2 = (Car)c.newInstance();
									c2.unload();
									sources.add(c2);
								
								} 
							}
							trn = TrainCreator.generate(r, new Engine(), sources.toArray(new Car[0]));
						}
						
						EESegment dest = selectDestSegment(_orig_seg, ispas && trn[trn.length - 1].isEngine());  // only double-headed passenger trains would reasonable reverse out
						
						Train t = new Train(trn);
						destinations.put(t, dest);
						
						AutoControl ac = new AutoControl();

						ac.setMyInfo("Destination "+dest.label);
						ac.setSyncToClick(false);
						t.setController(ac);

						int vel;

						if (t.weight() < 1000 || ispas)
							vel = 25;
						else if (t.weight() < 1500)
							vel = 20;
						else
							vel = 15;
						
						t.setVel(vel);
						t.setThrottle(vel / 5);
						EESegment ee = _orig_seg; 
						t.pos.r = ee;
						t.pos.orig = ee.HES;
						setTrain(t, dest, vel);
					}
				
			};
			return dp;
		}
		
		public DispatchPanel createStartMovingDispatchPanel(String title, final RailSegment rs) {
			final DispatchPanel dp = new DispatchPanel(title) {
				public void predeploy() {
					
					EESegment dest = selectDestSegment(NORTHWEST, false); // either south segment is fine

					// you'd think I'd just store the trains themselves
					// since we DO make them above
					// and that would be easier than storing the segments
					// and then extracting the train
					// BUT remember the reverse = true
					// that means the trains actually on the segment
					// are not the same train as originally created
					Train t = rs.trains().toArray(new Train[0])[0];
					destinations.put(t, dest);
					
					setTrain(t, dest, 25);
					
					t.getController().load();
					
					((AutoControl)t.getController()).setMyInfo("Destination "+dest.label);
					

					
					
				}
				public void deployed() {
					
					_t.getController().horn();
					_t.setBrake(false);
					_t.setThrottle(5); /// 25 mph
				}
			};
			return dp;
		}
		
		public DispatchWindow() {
			super();
			getContentPane().setLayout(new GridLayout(3, 2));
			nw = createRepeatDispatchPanel("Northwest", NORTHWEST);
			add(nw);
			
			ne = createRepeatDispatchPanel("Northeast", NORTHEAST); 
			add(ne);
			
			pw = createStartMovingDispatchPanel("Platform West", WESTPLATFORM); 
			add(pw);
			
			pe = createStartMovingDispatchPanel("Platform East", EASTPLATFORM); 
			add(pe);
			
			sw = createRepeatDispatchPanel("Southwest", SOUTHWEST); 
			add(sw);
			
			se = createRepeatDispatchPanel("Southeast", SOUTHEAST); 
			add(se);
			
			
			
			setTitle("Dispatcher");
			pack();
		}
	}
	
	
	@SuppressWarnings("serial")
	protected class DispatchPanel extends JPanel implements ActionListener {
		
		protected Train _t;
		private JLabel _type, _weig, _len, _spd, _dest;
		private EESegment _dest_seg;
		private JButton _deploy;
		
		
		public String title;
		
		
		public void setTrain(Train t, EESegment dest, int vel) {
			_t = t;
			_dest_seg = dest;
			
			boolean ispas = false;
			for (Car c : t.array())
				if (c instanceof Passenger)
					ispas = true;
			
			_type.setText(ispas ? "Passenger" : "Freight");
			_weig.setText(NumberFormat.getInstance().format(t.weight()) + " Tons");
			_spd.setText(Integer.toString(vel) + " MPH");
			_len.setText(NumberFormat.getInstance().format(t.length().feet()) + " Feet");	
			_dest.setText(_dest_seg.label);
			
			_deploy.setEnabled(true);
		}
		
		public DispatchPanel(String title) {
			super();
			
			this.title = title;
			
			setLayout(new GridLayout(0, 2));
			add(new JLabel("Type"));
			_type = new JLabel();
			add(_type);
			
			add(new JLabel("Weight"));
			_weig = new JLabel();
			add(_weig);
			
			add(new JLabel("Length"));
			_len = new JLabel();
			add(_len);
			
			add(new JLabel("Speed"));
			_spd = new JLabel();
			add(_spd);
			
			add(new JLabel("Dest."));
			_dest = new JLabel();
			add(_dest);
		
			_deploy = new JButton("Deploy");
			_deploy.setEnabled(false);
			_deploy.addActionListener(this);
			add(_deploy);
			
			setBorder(BorderFactory.createTitledBorder(title));
		}

		/**
		 * Over ride me to provide extra functionality.
		 */
		public void deployed() {
			//over ride me
		}
	
		/**
		 * Over ride me to prepare/create the train prior to deployment
		 */
		public void predeploy() {
			// over ride me
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			
			_t.followMeOnce = true;
			
			
			
						
			
			_type.setText("");
			_weig.setText("");
			_spd.setText("");
			_len.setText("");	
			_dest.setText("");
			
			_deploy.setEnabled(false);
			
			deployed();
			
			mpf.jdb.trains.select(_t, null);
			
			_t = null;
		}
		
	}

	
	
}
