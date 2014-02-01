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


import java.io.File;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.kolls.railworld.play.script.Script;
import net.kolls.railworld.play.script.ScriptManager;

/**
 * A panel which allows the user to select which scripts they want to use.
 * Includes internal scripts and optionally scripts found in a local directory.
 * 
 * @author Steve Kollmansberger
 *
 */
public class ScriptPanel extends JPanel {
	
	private CheckBoxList cbl;
	
	private ScriptManager curr;
	
	/**
	 * Return a list of all scripts selected by the user.
	 * 
	 * @return Array of scripts.
	 */
	public Script[] getScripts() {
		
		// wtf can't just cast cbl.getSelectedValues() into Script[]
		Script[] scrs = new Script[cbl.getSelectedValues().length];

		String selscrs = "";
		for (int i = 0; i < cbl.getSelectedValues().length; i++) {
			scrs[i] = (Script)cbl.getSelectedValues()[i];
			selscrs += scrs[i].toString();
			if (i+1 < cbl.getSelectedValues().length)
				selscrs += ",";
		}
		
		
		
		// first, save selected scripts if desired
		if (Options.getRemember()) {
			Preferences prefs = Preferences.userNodeForPackage(Options.class);
			prefs.put("Scripts", selscrs);
			
		}

		return scrs;
	}
	
	/**
	 * Create a script panel.
	 */
	public ScriptPanel() {
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		
		curr = ScriptManager.allScripts();
		prep();
		
		
	}
	
	
	@Override
	public void setEnabled(boolean value) {
		super.setEnabled(value);
		cbl.setEnabled(value);
	}
	
	private void prep() {
		removeAll();
		
		
		cbl = new CheckBoxList(curr.toArray());
		JScrollPane jsp = new JScrollPane(cbl);
		add(jsp);
		revalidate();
		
		Preferences prefs = Preferences.userNodeForPackage(Options.class);
		boolean userSpec;
		
		
		userSpec = Options.getRemember() && prefs.get("Scripts", null) != null;
			
		
		if (userSpec) {
			
			
			String[] selscrs = prefs.get("Scripts", "").split(",");
			
			
			int[] selidx = new int[selscrs.length];
			
			for (int j = 0; j < selscrs.length; j++) {
				for (int i = 0; i < curr.size(); i++) {
					if (curr.get(i).toString().equals(selscrs[j])) {
						selidx[j] = i;
					}
				}
			}

			// only set if there's something to set
			if (!(selscrs.length == 1 && selscrs[0].length() == 0))
				cbl.setSelectedIndices(selidx);
			
		} else {
			int sz = 0;
			
			for (int i = 0; i < curr.size(); i++)
				if (curr.get(i).onByDefault())
					sz++;
			int[] sels = new int[sz]; // I hate everything
			sz = 0;
			for (int i = 0; i < curr.size(); i++)
				if (curr.get(i).onByDefault())
					sels[sz++] = i;
			
			cbl.setSelectedIndices(sels);
		}
		
	}
	
	
	
	
	
	

}
