package net.kolls.railworld.play;

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

import net.kolls.railworld.CLoc;
import net.kolls.railworld.Car;
import net.kolls.railworld.Distance;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.Sound;
import net.kolls.railworld.Sounds;
import net.kolls.railworld.Train;
import net.kolls.railworld.TrainControl;
import net.kolls.railworld.segment.*;
import net.kolls.railworld.tuic.*;
import net.kolls.railworld.play.ra.*;

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import java.util.Iterator;


/**
 * The list of trains. Includes behavior for trains and train processing.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("rawtypes")
public class Trains implements ListModel, Iterable<Train> {
	
	

	private static final double MAX_JOIN_FT = 6;
	private JPanel tch;
	private Train selectedT;
	private Sound ls;
	private JList trainList;

	/**
	 * Create a train list with a particular controller panel and a visual list.
	 * 
	 * @param tchr A {@link JPanel} to display the selected train's controller.
	 * @param tl A {@link JList} to display the list of trains.
	 */
	public Trains(JPanel tchr, JList tl) {
		tch = tchr;
		ls = null;
		ldls = new Vector<ListDataListener>();
		
		
		
		tv = new ArrayList<Train>();
		trainList = tl;
		
		
		
	}

	
	
	/**
	 * 
	 * @return The selected {@link Train}, if any.
	 */
	public Train getSelectedTrain() {
		return selectedT;
	}
	/**
	 * 
	 * @return The selected {@link Car} within the selected train, if any.
	 */
	public Car getSelectedCar() {
		if (selectedT == null) return null;
		return selectedT.getController().getSelected();
	}
	
	
	/**
	 * Select a particular train, and optionally, a particular car. 
	 * If both are null, this means "deselect" whatever is selected.
	 * 
	 * @param t The {@link Train} to select.  May be null.
	 * @param c The {@link Car} to select.  If null, the controller may select the first car in the train by default.
	 */
	public void select(Train t, Car c) {
		// you can't select a train not on the map
		if (t != null && !tv.contains(t)) return;
		
		if (selectedT != null && selectedT != t) {
			
			selectedT.followMe = false;
			selectedT.getController().deselect();
		}
		
		if (selectedT != null) selectedT.getController().setSelected(null);
		
		tch.removeAll();
		

		
		// change train selection
		selectedT = t;
		
		if (c == null && t != null) c = t.array()[0];
		
		if (selectedT != null) {
			
			tch.add(selectedT.getController());
			
			selectedT.getController().setSelected(c);
			selectedT.getController().select();
			
			// update list selection
			// since our tv vector models the list
			// the indices should be the same
			
			trainList.setSelectedIndex(tv.indexOf(t));
			trainList.ensureIndexIsVisible(tv.indexOf(t));
			 
		} else {
			
			trainList.clearSelection();
			
			if (ls != null) {
				ls.stop();
				ls = null;
			}
			
		}
		
		
		tch.repaint();
	}

	/**
	 * Reverse the direction of a given train.  Modifies the trains list AND returns the new train.
	 * 
	 * @param t The train to reverse.  This train will be removed from the list
	 * @return The new train added to the list.
	 */
	public Train reverse(Train t) {
		Car[] train = t.array();
		Car[] cars = new Car[train.length];
		int j = 0;
		int i;
		for ( i = cars.length-1 ; i >= 0; i--, j++)
			cars[i] = train[j];
		Train t2 = new Train(cars);
		t2.setController( (TrainControl)t.getController().newInstance());
		t2.getController().setTrainActionScriptNotify(t.getController().getTrainActionScriptNotify());

		TrainEndPointFinder tp = new TrainEndPointFinder();
		tp.act(t);
		CLoc pos = tp.p;
		
		
		t2.pos = pos;

		t2.setThrottle(t.getThrottle());
		t2.setBrake(t.getBrake());
		t2.followMe = t.followMe;

		int idx = tv.indexOf(t);
		remove(t);
		add(idx, t2);	

		return t2;
		
	}


	/**
	 * Splits a given train based on the selected car.  All cars before the selected car go in one train,
	 * all remaining cars in the other. 
	 * 
	 * @param t The train to split.
	 * @return The primary (selected) train of the two resultant trains.
	 */
	public Train split(Train t) {	
		// creates 2 new trains and adds them to the train array
		// removes the old train
		// returns the first of the new trains
		
		
		Car[] carray = t.array();		
		Car[] ca1, ca2;

		int c = 0;

		for (int i = 0; i < carray.length; i++)
			if (carray[i] == getSelectedCar()) break; else c++;

		ca1 = new Car[c];
		ca2 = new Car[carray.length-c];

		System.arraycopy(carray, 0, ca1, 0, c);
		System.arraycopy(carray, c, ca2, 0, carray.length-c);

		
		Train t2, t3;
		
		t2 = new Train(ca1);
		t2.pos = t.pos;
		t2.setController( (TrainControl)t.getController().newInstance());
		t2.getController().setTrainActionScriptNotify(t.getController().getTrainActionScriptNotify());
		
		
		int idx = tv.indexOf(t);
		add(idx, t2);

		t3 = new Train(ca2);
		t3.setController( (TrainControl)t.getController().newInstance());
		t3.getController().setTrainActionScriptNotify(t.getController().getTrainActionScriptNotify());
		
		TrainEndPointFinder tp = new TrainEndPointFinder();
		tp.act(t2);
		
		CLoc t3orig = tp.p;
		
		// starts out reversed, so we go away from the other train
		t3.pos = t3orig.segFwd(new Distance(15,Distance.Measure.FEET)).newLoc.reverse();
		
		// if we set that position, does that push t3 off the end of the track?
		try {
			tp.act(t3);
		} catch (NullPointerException ex) {
			// yes!  so don't move t3, instead move t2
			t3.pos = t3orig.reverse();
			t2.pos = t.pos.segFwd(new Distance(15, Distance.Measure.FEET)).newLoc;
		}
		
		t2.setBrake(t.getBrake());
		t3.setBrake(t.getBrake());
		
		t3.followMe = t.followMe;
	
		
		
		add(idx, t3);
		remove(t);
		
		return t3;

	}
	
	/**
	 * Joins two trains.  The two trains are removed from the list and a new train is added.
	 * 
	 * @param t1
	 * @param t2
	 * @return The new train consisting of all cars of the both trains.
	 */
	public Train join(Train t1, Train t2) {


		Train nt;
		Car[] carray;
		Car[] ca1 = t1.array(), ca2 = t2.array();
		carray = new Car[ca1.length + ca2.length];
		
		System.arraycopy(ca1, 0, carray, 0, ca1.length);
		System.arraycopy(ca2, 0, carray, ca1.length, ca2.length);
		
		nt = new Train(carray);
		nt.setController( (TrainControl)t1.getController().newInstance()); // which controller? arbitrary
		nt.getController().setTrainActionScriptNotify(t1.getController().getTrainActionScriptNotify());
		
		nt.pos = t1.pos;
		nt.setBrake(t1.getBrake() || t2.getBrake());
		nt.followMe = t1.followMe || t2.followMe;
		nt.setThrottle(Math.max(t1.getThrottle(), t2.getThrottle()));
				
		int idx = tv.indexOf(t1);
		add(idx, nt);
		remove(t1);
		remove(t2);
		return nt;

	}
	
	private double momentum(Train t) {
		return t.vel()*t.weight();
	}
	
	/**
	 * Process all train behavior for one step.  Perform joins, splits, and reverses as requested.
	 * Move trains.  Detect accidents.
	 * 
	 * @throws RailAccident If an accident occurs.
	 */
	public void step() throws RailAccident {

		RailSegment l;
		Train t, t2;
		int i;
		Train nt;
		
		
		
		TrainEndPointFinder tp = new TrainEndPointFinder();
		int joinDist = new Distance(10, Distance.Measure.FEET).iPixels();
		
		for (i = 0; i < size(); i++) {
			t = get(i);
			
//			 check if need to be reversed
			if (t.reverse) {
				if (t == selectedT)
					select(reverse(t), null);
				else
					reverse(t);
				return;
			}
			// check if need to be split
			if (t.split) {
				if (t == selectedT)
					select(split(t), null);
				else
					split(t);
				Sounds.uncouple.play();
				return;
			}
			
			// motionless trains can't initiate any action
			if (t.vel() == 0 && t.getThrottle() == 0) continue;
			
			
			// check if train is off the map
			// this method will break if the
			// train is completely on a hidden line segment
			// which directly abuts an end segment
			
			
			
			
			
			tp.act(t);
			if (t.pos.r == tp.p.r && t.pos.r instanceof HiddenSegment && t.pos.r.dest(null) instanceof EESegment) {
				remove(i);
				t.pos.r.trains().clear();
				if (selectedT == t) select(null, null);
				return;
			}

			
			
			
			// TODO: this could cause a crash because
			// if the train is aligned right on the edge of the next segment
			// then the other train won't see it
			Iterator<Train> it = t.pos.r.trains().iterator();
			
			

			
			while (it.hasNext()) {
				t2 = it.next();
				
				if (t2 == t) continue;




				// find forward segments to joinDist
				// this allows to join if they happen to be right on a segment boundary
				// don't join on zero length segments, skip to the next "real" segment
				ArrayList<RailSegment> fsegs = t.pos.r.destNZ(t.pos.orig);
				RailSegment nextseg = fsegs.get(fsegs.size() - 1);
				
				
				
				// for collision: is front of train intersecting 
				// another train?
				if ( (t2.pos.r == t.pos.r || t2.pos.r == nextseg) && t2.pos.getPoint().distance(t.pos.getPoint()) < joinDist) {
					// front of my train hits front of other train
					
					// if total speed is 5 MPH or less, we join (1 mph grace)
					double rv = t.vel() + t2.vel();
					
					if (rv > MAX_JOIN_FT) {
						/* remove(t);
						remove(t2);
						if (selectedT == t || selectedT == t2) select(null, null);
						*/
						throw new HeadOn(t, t2, t2.pos.getPoint());
					}
					
					double nv;
					
					if (momentum(t) > momentum(t2)) {
						nt = join(reverse(t2), t);
						nv = (t.vel()*t.weight() - t2.vel()*t2.weight()) / ((t.weight()+t2.weight()));
						
						
					} else {
						nt = join(reverse(t), t2);
						nv = (t2.vel()*t2.weight() - t.vel()*t.weight()) / ((t.weight()+t2.weight()));
						
						
					}
					if (nt.getBrake() == false) nt.setVel(nv);
					if (selectedT == t || selectedT == t2) select(nt, null);
					
					Sounds.couple.play();
					return;
				}
				

				
				
				
				
				
				tp.act(t2);

				
				if ( (tp.p.r == t.pos.r || tp.p.r == nextseg) && tp.p.getPoint().distance(t.pos.getPoint()) < joinDist) {
					// front of my train hits end of other train
					
					// if total speed is 5 MPH or less, we join (1 mph grace)
					double rv = Math.abs(t.vel() - t2.vel()); 
					if (rv > MAX_JOIN_FT) {
						/*remove(t);
						remove(t2);
						if (selectedT == t || selectedT == t2) select(null, null);
						*/
						throw new RearEnd(t, t2, t.pos.getPoint());
					}

					
					nt = join(t2, t);
					double nv = (t.vel()*t.weight() + t2.vel()*t2.weight()) / ((t.weight()+t2.weight()));
					if (nt.getBrake() == false) nt.setVel(nv);
					if (selectedT == t || selectedT == t2) select(nt, null);
					
					Sounds.couple.play();
					return;
				}
				
				
				  

				
			}

			
			

			
			
			// can't replace with destNZ because of the call to enter
			while (t.pos.r.length().feet() == 0) { // jump over 0 length items
				
				t.pos.r.enter(t);
				

				
				l = t.pos.r;  // makes less jumpy
				t.pos.r = t.pos.r.dest(t.pos.orig);
				t.pos.orig = l;
			}
			t.pos.per = t.pos.r.step(t.pos.per, t.vel());
		
			// this can only happen if a train enters on top of another train
			if (t.pos.r != null && t.pos.r.singleton() && t.pos.r.trains().size() > 1) {
				t2 = t.pos.r.trains().toArray(new Train[0])[1];
				if (t == t2)
					t2 = t.pos.r.trains().toArray(new Train[0])[0];
				
		
				
				t.pos.r.trains().clear();
				t2.pos.r.trains().clear();
				
				/*
				if (t == selectedT || t2 == selectedT) select(null, null);
				
				remove(t);
				remove(t2);
				*/
				throw new SideOn(t, t2, t.pos.getPoint());
				
			}

			if (t.pos.per == 1) {
				
				
				t.pos.per = 0;
				l = t.pos.r;
				t.pos.r = t.pos.r.dest(t.pos.orig);
				
				
				// we repeat this here with one important change
				// if the new segment the train is entering is singleton and will be accident
				// the train does not actually enter the segment (no enter is recorded, and the pos is retained at the old segment)
				// this prevents a switch from flipping in favor of a crash-causing train, for instance
				// note we check > 0 because this train isn't there yet
				if (t.pos.r != null && t.pos.r.singleton() && t.pos.r.trains().size() > 0) {
					t2 = t.pos.r.trains().toArray(new Train[0])[0];
					
					
					
					t.pos.r = l;
					t.pos.per = 1 - t.pos.r.pixelStep(0);
					
					
					
					/*
					if (t == selectedT || t2 == selectedT) select(null, null);
					remove(t);
					remove(t2);
					*/
					throw new SideOn(t, t2, t.pos.getPoint());
					
				}
				
				if (t.pos.r == null) {
					// edge overrun
					

					
					t.pos.r = l;
					t.pos.per = 1 - t.pos.r.pixelStep(0); 
					//0.999;
					
					// if total speed is more than 5 MPH, it's an accident  (1 mph grace)
					
					if (t.vel() > MAX_JOIN_FT) {
						//remove(t);
						//if (selectedT == t) select(null, null);
						throw new OverRun(t, t.pos.getPoint());
					}

					
					t.setThrottle(0);
					t.setVel(0);
				} else t.pos.orig = l;

				
				
				t.pos.r.enter(t);

				
			}
			

			// move train
			t.adjust();
			
			
		}
		
		
		// play correct engine sound
		Sound ds = null;
		
		if (selectedT != null) {
			if (selectedT.hasEngine()) {
				ds = Sounds.engine[selectedT.getThrottle()];
			}
		}
		if (ds != ls) {
			if (ls != null) ls.stop();
			ls = ds;
			if (ls != null) ls.loop();
		}

		
		
		
	}	
	
	
	
	
	// *** for list updating
	
	private Vector<ListDataListener> ldls;
	
	public void addListDataListener(ListDataListener l) {
		ldls.add(l);
		

	}

	public Object getElementAt(int index) {
		if (index < 0 || index >= size()) return null;
		return get(index);
		
	}

	public int getSize() {
		return size();
		
	}

	public void removeListDataListener(ListDataListener l) {
		ldls.remove(l);
	}
	private void notifyLDLS(ListDataEvent lde) {
		
		Iterator<ListDataListener> i = ldls.iterator();
		while (i.hasNext()) {
			ListDataListener l = i.next();
			
			switch (lde.getType()) {
			case ListDataEvent.CONTENTS_CHANGED:
				l.contentsChanged(lde);
				break;
			case ListDataEvent.INTERVAL_ADDED:
				l.intervalAdded(lde);
				break;
			case ListDataEvent.INTERVAL_REMOVED:
				l.intervalRemoved(lde);
				break;
			}
		}
	}
	
	// ** in order to notify the list data listeners of changes
	// we must control access to the vector
	// this is why it is an element and not an extends
	
	private ArrayList<Train> tv;
	
	/**
	 * Add a new a train to the collection.
	 * 
	 * @param t The train to add.
	 */
	public synchronized void add(Train t) {
		tv.add(t);
		notifyLDLS(new ListDataEvent(tv,ListDataEvent.INTERVAL_ADDED,tv.size()-1,tv.size()-1));
	}
	
	/**
	 * Add a new train to the collection at the specified index.
	 * 
	 * @param index Index to add train at
	 * @param t The train to add
	 */
	public synchronized void add(int index, Train t) {
		tv.add(index, t);
		notifyLDLS(new ListDataEvent(tv,ListDataEvent.INTERVAL_ADDED,index,index));
	}
	
	
	/**
	 * Get a train
	 * 
	 * @param index The index of the train to get
	 * @return The train at that index
	 */
	public Train get(final int index) {
		return tv.get(index);
		
	}
	
	/**
	 * Remove a train
	 * 
	 * @param index The index of the train to remove
	 * 
	 */
	public synchronized void remove(final int index) {
		if (index == -1) return;
		
		tv.remove(index);
		notifyLDLS(new ListDataEvent(tv,ListDataEvent.INTERVAL_REMOVED,index,index));
		
		
	}
	
	/**
	 * Remove a given train from the map.
	 * 
	 * @param t Train to remove.
	 */
	public synchronized void remove(Train t) {
		int i;
		
		
		i = tv.indexOf(t);
		
		remove(i);
	}
	/**
	 * How many trains are being managed.
	 * 
	 * @return The number of trains
	 */
	public int size() {
		return tv.size();
	}
	
	public Iterator<Train> iterator() {
		return tv.iterator();
	}
	
	/**
	 * Inform the list listeners that the entire list may have changed.
	 */
	public void refreshList() {
		// tell list to refresh all items
		notifyLDLS(new ListDataEvent(tv,ListDataEvent.CONTENTS_CHANGED,0,tv.size()-1));
		
	
	}
	
	
}

