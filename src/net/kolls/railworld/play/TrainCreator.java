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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.Iterator;

import net.kolls.railworld.*;
import net.kolls.railworld.car.Caboose;
import net.kolls.railworld.car.Engine;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.segment.EESegment;
import net.kolls.railworld.segment.LUSegment;
import net.kolls.railworld.tuic.TrainPainter;



/**
 * Provides a window allowing the user to create a train to enter the map.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class TrainCreator extends JFrame {

	
	private JPanel loaded, unloaded, nonload;
	private MultiLineTrainPanel ctrain;
	private Vector<EESegment> ees;
	private PlayFrame pf;
	@SuppressWarnings("rawtypes")
	private JComboBox enters, speeds;
	private final int incr = Train.MAX_SPEED_MPH / Train.MAX_THROTTLE;
	private Map<String,Car> sc;
	
	/**
	 * Generate a series of engines, cargo cars, and possibly a caboose
	 * based on possible cargo cars.  
	 * 
	 * @param r Random number source
	 * @param engine The template car for the engine, e.g. to allow derived engine types
	 * @param sources Array of allowable cargo cars
	 * @return Train cars
	 */
	public static Car[] generate(Random r, Car engine, Car[] sources) {
		int leng = r.nextInt(20)+2;
		int occurs;
		int i, j;
		ArrayList<Car> results = new ArrayList<Car>();
		
		
		int l2 = leng;
		while (l2 > 1 && results.size() < 3) {
			results.add( (Car)engine.newInstance());
			
			l2 /= r.nextInt(5)+1;
		}
		
		if (sources.length == 0) return results.toArray(new Car[0]);
		
		while (results.size() < leng) {
			occurs = r.nextInt(4)+1;
			i = r.nextInt(sources.length);
			
			for (j = 0; j < occurs; j++) {
				try {
					Car c2 = (Car)sources[i].newInstance();
					if (sources[i].loaded()) c2.load(); else c2.unload();
					results.add(c2);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			
		}
		
		if ((double)leng / 20 > r.nextDouble())
			results.add(new Caboose());
		
		return results.toArray(new Car[0]);
	}
	
	private void generate() {
		Random r = new Random();
		Car[] cars = sc.values().toArray(new Car[0]);
		
		Car[] resultCars = generate(r, new Engine(), cars);
		
		for (Car c : resultCars)
			ctrain.myVC.add(c);
	}
	
	
	private JButton carButton(final Car c) {
		
		String d = c.show();
		Icon i = new ImageIcon(TrainPainter.image(c, this));
		JButton j = new JButton(d,i);
		j.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// because of the way the cars are added to the train
				// we have to have separate instances for each car
				// because a referencer is just copied
				// so it's actually many of the same car otherwise
				// this does create a problem for selected cars
				try {
					Car c2 = (Car)c.newInstance();
					if (c.loaded()) c2.load(); else c2.unload();
					ctrain.myVC.add(c2);	
				} catch (Exception ex) {
					ex.printStackTrace();
					
				}
				
				ctrain.revalidate();
				ctrain.repaint();
				
			}
		});
		return j;
		
	
	}
	
	private void addLUButton(Car c) {
		c.load();
		loaded.add(carButton(c));
		
		// in order to have a loaded and unloaded car
		// separately, we need to have two instances
		try {
			Car c2 = (Car)c.newInstance();
			c2.unload();
			unloaded.add(carButton(c2));
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addWidgets() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JTabbedPane tabbedPane = new JTabbedPane();
		
		nonload = new JPanel();
		loaded = new JPanel();
		unloaded = new JPanel();
		nonload.setLayout(new GridLayout(0,2));
		loaded.setLayout(new GridLayout(0,2));
		unloaded.setLayout(new GridLayout(0,2));
		
		
		
		ArrayList<Car> at = Factories.cars.allTypes();
		for (int i = 0; i < at.size(); i++) {
			Car c = at.get(i);
			if (c.canUserCreate() == false) continue;
			
			if (c.isLoadable()) {
				addLUButton(c);
			} else {
				nonload.add(carButton(c));
			}
			
			
			
		}
		
		tabbedPane.add("Special", nonload);
		tabbedPane.add("Loaded", loaded);
		tabbedPane.add("Unloaded", unloaded);
		
		
		
		
		
		
		getContentPane().add(tabbedPane);
		
		ctrain = new MultiLineTrainPanel(); 
		
		getContentPane().add(ctrain);
		
		// create the combo boxes
		// for selecting entrance and speed
		JPanel hp = new JPanel();
		hp.setLayout(new BoxLayout(hp, BoxLayout.X_AXIS));
		hp.add(new JLabel("Entering At"));
		hp.add(Box.createHorizontalGlue());
		enters = new JComboBox();
		Iterator<EESegment> ei = ees.iterator();
		while (ei.hasNext()) {
			EESegment ee = ei.next();
			enters.addItem(ee.label);
			
		}
		hp.add(enters);
		getContentPane().add(hp);
		
		
		hp = new JPanel();
		hp.setLayout(new BoxLayout(hp, BoxLayout.X_AXIS));
		hp.add(new JLabel("Speed"));
		hp.add(Box.createHorizontalGlue());
		speeds = new JComboBox();
		
		int i;
		for (i = 1; i <= Train.MAX_THROTTLE; i++) {
			speeds.addItem(Integer.toString(i*incr) + " MPH");
		}
		speeds.setSelectedIndex(i-2); // due to starting at 1
		
		hp.add(speeds);
		getContentPane().add(hp);
		
		hp = new JPanel();
		hp.setLayout(new BoxLayout(hp, BoxLayout.X_AXIS));
		JButton b;
		b = new JButton("OK");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (ctrain.myVC.isEmpty()) {
					// cancel, no cars
					dispose();
					return;
				}
				
				Car[] cr = (ctrain.myVC.toArray(new Car[0]));
				Train t = new Train(cr);
				
				if (t.hasEngine())
					t.setThrottle(speeds.getSelectedIndex()+1);
				
				t.setVel(incr*(speeds.getSelectedIndex()+1)); // avoid acceleration wait
				// also if there are no engines, the train
				// would ever be stuck on the border
				
				EESegment ee = ees.get(enters.getSelectedIndex());
				
				t.pos.r = ee;
				t.pos.orig = ee.HES;
				t.followMeOnce = true;
				
				t.getController().setTrainActionScriptNotify(sm);
				pf.addTrain(t, true);
				
				dispose(); // close window when done
			}
		});
		hp.add(Box.createHorizontalGlue());
		hp.add(b);
		hp.add(Box.createHorizontalGlue());


		
		b = new JButton("Clear");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrain.myVC.clear();
				ctrain.revalidate();
				getContentPane().repaint();
				
				
			}
		});
		hp.add(b);
		hp.add(Box.createHorizontalGlue());
		
		b = new JButton("Generate");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ctrain.myVC.clear();
				generate();
				ctrain.revalidate();
				getContentPane().repaint();
				
			}
		});
		hp.add(b);
		hp.add(Box.createHorizontalGlue());
		
		b = new JButton("Cancel");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		hp.add(b);
		hp.add(Box.createHorizontalGlue());
		getContentPane().add(hp);
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
		
		
		
	}
	
	private ScriptManager sm;
	
	/**
	 * Shows the train creator window.
	 * The rail segments are needed for the generator to know what load/unload cars are supported.
	 * When created, the train will be added to the given trains collection automatically.
	 * 
	 * @param lines An array of {@link RailSegment}s used for the train generator.
	 * @param pf PlayFrame by which the {@link PlayFrame#addTrain(Train, boolean)} method will be called.
	 * @param trainNotify The script manager to attach to the traincontroller for notification.
	 */
	public TrainCreator(RailSegment[] lines, PlayFrame pf, ScriptManager trainNotify) {
		super("New Train");
		
		setIconImage(Images.frameIcon);
		
		// need to find all EE segments
		// and lu segments for generator
		ees = new Vector<EESegment>();
		
		sc = new HashMap<String,Car>();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] instanceof EESegment) 
				ees.add((EESegment)lines[i]);
			
			if (lines[i] instanceof LUSegment) {
				Car[] cl = ((LUSegment)lines[i]).lu();
				for (int j = 0; j < cl.length; j++)
					sc.put(cl[j].show() + (cl[j].loaded() ? "/L" : "/U"), cl[j]);
					
			}
				
		}
		this.pf = pf;
		sm = trainNotify;
		
		addWidgets();
		
		
		
	}
}
