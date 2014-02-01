package net.kolls.railworld.opening;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.kolls.railworld.play.script.Mission;
import net.kolls.railworld.play.script.ScriptManager;

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
 * A panel which allows user to browse through the missions
 * and see their briefings.
 * 
 * @author Steve Kollmansberger
 */
public class MissionPanel extends JPanel implements ListSelectionListener {

	private JList missions;
	private JPanel briefing, _not_sel_brief;
	private ScriptManager sm;
	
	public MissionPanel() {
		super();
		
		setLayout(new BorderLayout());
	
		sm = ScriptManager.allMissions();
	
		missions = new JList(sm.toArray());
		_not_sel_brief = new JPanel();
		_not_sel_brief.add(new JLabel("Select a mission to see a briefing"));
		briefing = _not_sel_brief;
		
		
		add(new JScrollPane(missions), BorderLayout.WEST);
		add(briefing, BorderLayout.CENTER);
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
	
		
		revalidate();
		
		missions.addListSelectionListener(this);
	}


	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		Mission m = (Mission)sm.get(missions.getSelectedIndex());
		remove(briefing);
		briefing = m.briefing();
		add(briefing, BorderLayout.CENTER);
		
		revalidate();
		
	}
	
	/**
	 * Returns the currently selected mission, or null if none.
	 * 
	 * @return Currently selected mission
	 */
	public Mission getSelectedMission() {
		if (missions.getSelectedIndex() == -1)
			return null;
		
		return (Mission)sm.get(missions.getSelectedIndex());
	}
}
