package net.kolls.railworld.opening;

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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The option panel provides way for applications to display a series
 * of items, each item with a label on the left and the control on the right.
 * The controls will be aligned so they all start on the same boundary,
 * and a small margin will be added.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class OptionPanel extends JPanel {

	private GridBagConstraints c;
	private GridBagConstraints cl;
	private GridBagLayout gridbag;
	
	/**
	 * Construct a new option panel with default margins.
	 *
	 */
	public OptionPanel() {
		super();
		
		gridbag = new GridBagLayout();
		setLayout(gridbag);
		
		c = new GridBagConstraints();
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(3,3,3,3);
		
		cl = new GridBagConstraints();
		cl.anchor = GridBagConstraints.EAST;
	}
	
	/**
	 * Adds a new entry to this option panel.  The label will appear on the left,
	 * and the given control on the right.
	 * 
	 * @param label A label for this control
	 * @param control A control to display
	 */
	public void addLabeledControl(String label, Component control) {
		
		
		JLabel l;
		l = new JLabel(label);
		gridbag.setConstraints(l, cl);
		add(l);
		
		gridbag.setConstraints(control, c);
		add(control);
	}
	
}
