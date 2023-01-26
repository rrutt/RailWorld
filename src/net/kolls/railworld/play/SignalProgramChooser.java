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



import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.kolls.railworld.Factories;
import net.kolls.railworld.SignalProgram;
import net.kolls.railworld.segment.Signal;


/**
 * Provides an undecorated, popup frame for the user to select a signal program.
 * Disposes when a program is selected or when the mouse leaves the frame.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class SignalProgramChooser extends JFrame implements AWTEventListener {


	private ButtonGroup spsg;
	

	
	private JToggleButton createButton(final SignalProgram s, final Signal signal) {
		JToggleButton tb = new JToggleButton();
		tb.setIcon(s.icon());
		if (s.getClass().equals(signal.sp.getClass())) tb.setSelected(true);
		tb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				signal.sp = s;
				close();
			}
			
		});
		tb.setToolTipText(s.toString());
		tb.setPreferredSize(new Dimension(s.icon().getIconWidth()+8, s.icon().getIconHeight()+8));
		spsg.add(tb);
		return tb;
		
	}
	
	
	/**
	 * Create a program chooser for a signal.
	 * 
	 * @param signal The signal to select the program for.
	 */
	public SignalProgramChooser(Signal signal) {
		
		setUndecorated(true);
		
		JPanel sig = new JPanel();
		sig.setLayout(new GridLayout(0,3));
		
		spsg = new ButtonGroup();
		
		ArrayList<SignalProgram> sps = Factories.sps.allTypes();
		for (int i = 0; i < sps.size(); i++) {
			sig.add(createButton(sps.get(i), signal));
		}
		
		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);

		add(sig);
		pack();
	}
	
	private void close() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		dispose();
	}
	
	private boolean wentInside = false;

	/**
	 * Close the window when the mouse enters then leaves it.  
	 */
	public void eventDispatched(AWTEvent event) { 
	        if (event instanceof MouseEvent) { 
	            MouseEvent me = (MouseEvent) event;
	            
	            if (!SwingUtilities.isDescendingFrom(me.getComponent(), this)) {
	            	if (wentInside) {
	            		close();
	            	}
	                
	            	return; 
	            }  else wentInside = true;
	              
	            repaint();
	        } 
	    }


	

	
}
