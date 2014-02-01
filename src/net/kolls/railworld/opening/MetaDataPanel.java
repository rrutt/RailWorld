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
import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.kolls.railworld.io.MetaData;
import net.kolls.railworld.play.script.Script;

/**
 * A {@link JPanel} which displays and optionally allows editing of {@link MetaData} information.
 * This panel is embedded in the new map dialog, as well as used for the edit information
 * in the map editor and the display information in the play module.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class MetaDataPanel extends JPanel {
	private JTextField scale, author,  title, track;
	private JTextArea comment;
	private MetaData md;
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void construct(boolean editable, boolean hasSummary, Script[] scrs) {
		
		
		OptionPanel s = new OptionPanel();
		
		title = new JTextField(20);
		title.setEditable(editable);
		title.setText(md.title);
		s.addLabeledControl("Title", title);
		
		scale = new JTextField(3);
		scale.setEditable(editable);
		scale.setText(Double.toString(md.feetPerPixel));
		s.addLabeledControl("Scale (ft/px)", scale);
		
		
		
		
		author = new JTextField(20);
		author.setEditable(editable);
		author.setText(md.author);
		s.addLabeledControl("Author", author);
		
		comment = new JTextArea(5,20);
		comment.setEditable(editable);
		comment.setText(md.comment);
		comment.setLineWrap(true);
		JScrollPane sp = new JScrollPane(comment);
		s.addLabeledControl("Comment", sp);
		
		if (hasSummary) {
		
			track = new JTextField(5);
			track.setEditable(false);
			double miles = md.track / 5280.0;
			track.setText(NumberFormat.getInstance().format(miles));
			s.addLabeledControl("Track (miles)", track);
			
			
			
		
		}
		
		
		if (scrs != null) {
			JList lscr = new JList(scrs);

			s.addLabeledControl("Active Scripts", lscr);
		
		}
		
		
		s.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		JPanel p = new JPanel();
		
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		
		p.add(Box.createVerticalGlue());
		p.add(s);
		p.add(Box.createVerticalGlue());
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createRigidArea(new Dimension(5,0)));
		add(p);
		add(Box.createRigidArea(new Dimension(5,0)));
	
		
		
	}
	
	/**
	 * Create a blank meta data panel ready for user entry.
	 *
	 */
	public MetaDataPanel() {
		super();
		md = new MetaData();
		
		md.centerX = 0;
		md.centerY = 0;
		md.zoom = 1;
		
		construct(true, false, null);
		scale.setText("");
		
		//track.setText("N/A");
		
	}
	
	/**
	 * Displays a panel with given information.  Optionally allows the data to be edited.
	 * 
	 * @param m The {@link MetaData} to fill the panel with.
	 * @param scrs Optionally, an array of scripts to display.
	 * @param editable Indicates if the data should be editable.
	 */
	public MetaDataPanel(MetaData m, Script[] scrs, boolean editable) {
		super();
		
		md = m;
		
		
		construct(editable, true, scrs);
			
		
		
	}
	
	/**
	 * If the panel is editable, this can be used to get the current information on the panel.
	 * 
	 * @return Current data on the panel.
	 */
	public MetaData getMD() {
		
		md.title = title.getText();
		md.author = author.getText();
		
		md.comment = comment.getText();
		try {
			double d = Double.parseDouble(scale.getText());
			md.feetPerPixel = d > 0 ? d : 1;
		} catch (Exception e) {
			md.feetPerPixel = 1;
		}
		
		return md;
		
	}
	
}
