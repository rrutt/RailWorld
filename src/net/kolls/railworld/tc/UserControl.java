package net.kolls.railworld.tc;

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
import net.kolls.railworld.segment.LUSegment;

import javax.swing.*;

import java.util.*;
import java.awt.event.*;
import java.awt.*;

import javax.swing.event.*;

/**
 * The standard user control.  Provides a panel with throttle, brake, etc.
 * 
 * @author Steve Kollmansberger
 *
 */
public class UserControl extends TrainControl implements ItemListener, ChangeListener, ActionListener {
	
	private JSlider throttle;
	private JToggleButton brake, follow;
	private JButton reverse, split, unload, load, horn;


	
	
	
	@Override
	public void attach(Train t) {
		super.attach(t);
		
		
		throttle = new JSlider(SwingConstants.VERTICAL, 0, Train.MAX_THROTTLE, 0);
		// create slider labels
		Hashtable<Integer,JLabel> h = new Hashtable<Integer,JLabel>();
		h.put (new Integer (0), new JLabel("IDLE"));
		
		for (int i = 1; i <= Train.MAX_THROTTLE; i++) 
			h.put (new Integer (i), new JLabel(Integer.toString(i)+" ("+Integer.toString(i*(Train.MAX_SPEED_MPH/Train.MAX_THROTTLE))+" MPH)"));
		throttle.setLabelTable(h);
		throttle.setPaintLabels(true);
		throttle.setSnapToTicks(true);
		//throttle.setEnabled(false);
		throttle.addChangeListener(this);
		add(throttle);

		add(Box.createRigidArea(new Dimension(0,5)));
		add(Box.createVerticalGlue());
		
		JPanel cp = new JPanel();
		cp.setLayout(new GridLayout(4,2));
		
		brake = new JToggleButton("BRAKE");
		//brake.setEnabled(false);
		brake.addItemListener(this);
		
		//add(center(brake));
		cp.add(brake);

		follow = new JToggleButton("FOLLOW");
		follow.addItemListener(this);
		cp.add(follow);

		reverse = new JButton("REVERSE");
		reverse.addActionListener(this);
		cp.add(reverse);

		split = new JButton("SPLIT");
		split.addActionListener(this);
		cp.add(split);

		unload = new JButton("UNLOAD");
		unload.addActionListener(this);
		cp.add(unload);
		
		load = new JButton("LOAD");
		load.addActionListener(this);
		cp.add(load);
		
		horn = new JButton("HORN");
		horn.addActionListener(this);
		cp.add(horn);
		
		add(center(cp));
		add(Box.createRigidArea(new Dimension(0,5)));

		add(Box.createVerticalGlue());
		add(Box.createVerticalGlue());
		
		
				
		

	}	
	

	private JPanel center(JComponent x) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createHorizontalGlue());
		p.add(x);
		p.add(Box.createHorizontalGlue());
		return p;
	}
	
	
	@Override
	public boolean process() { super.process(); return true; }
	
	
	@Override
	public void run() {
		//try {
		
		
		if (myT == null) return;
		
		super.run();
		

		if (myT.hasEngine() == false) {
			throttle.setEnabled(false);
			horn.setEnabled(false);
		} else {
			throttle.setEnabled(true);
			horn.setEnabled(true);

		}

		
		throttle.setValue(myT.getThrottle());

		brake.setSelected(myT.getBrake());
		follow.setSelected(myT.followMe);
		
		
		
		
		
		if (myT.vel() > 0) {
			reverse.setEnabled(false); 
			split.setEnabled(false);
			unload.setEnabled(false);
			load.setEnabled(false);
		} else {
			reverse.setEnabled(true);
			
			if (myT.array()[0] != selected) split.setEnabled(true);
				else split.setEnabled(false);
			
			if (lable.length > 0) load.setEnabled(true);
				else load.setEnabled(false);
			if (ulable.length > 0) unload.setEnabled(true);
				else unload.setEnabled(false);
			
		}
		
		
		revalidate();
		setVisible(true);
		//} catch (Exception e) { 
//			e.printStackTrace();
	//	}	
	}

	public void itemStateChanged(ItemEvent e) {
    
    	Object source = e.getItemSelectable();

    	if (source == brake) { 
			
    		myT.setBrake( e.getStateChange() == ItemEvent.SELECTED);
    	}

		if (source == follow) { myT.followMe = ( e.getStateChange() == ItemEvent.SELECTED); }
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();

    	if (source == throttle) {
			myT.setThrottle(throttle.getValue());
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == reverse) {
			if (_s.trainAction(myT, "reverse")) return;
			myT.reverse = true;
		}
		if (source == split) {
			if (_s.trainAction(myT, "split")) return;
			myT.split = true;
		}
		if (source == load) {
			
			load();
		}
		
		if (source == unload) {
			
			unload();
		}
		if (source == horn) {
			
			horn();
		}
	}
	public void load(Map<String, String> data) {	}
	public Map<String, String> save() {
		return null;
	}
	
	@Override
	public String toString() { return "UserControl"; }
	
	
}

