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


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.kolls.railworld.Images;
import net.kolls.railworld.play.script.Mission;

/**
 * Displays a dialog for mission selection.  Includes ability
 * to select path to find map files.
 * 
 * 
 * @author Steve Kollmansberger
 *
 */
public class MissionDialog extends JDialog {

	private MissionPanel mp;
	private JTextField missionDir;
	private String misDir;
	
	/**
	 * Returns the selected mission, if one was selected and
	 * Cancel not pressed.  Otherwise returns null.
	 * 
	 * @return Maybe a mission, or null.
	 */
	public Mission getSelectedMission() {
		if (mp != null)
			return mp.getSelectedMission();
		else
			return null;
	}
	
	/**
	 * Returns the text of the directory selector textbox.
	 * No specific content guarantees.
	 * 
	 * @return Directory for map files.
	 */
	public String getDirectory() {
		return misDir;
	}
	
	/**
	 * Construct a mission dialog with a default directory.
	 * Directory is for map files and can be read after
	 * mission selected.
	 * 
	 * @param dir Default map file directory
	 */
	public MissionDialog(File dir) {
		
		
		
		// panel
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel jplQuestion = new JPanel();
		jplQuestion.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		jplQuestion.setLayout(gridbag);
		// label
		JLabel jlbQuestion = new JLabel("Map Directory: ");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gridbag.setConstraints(jlbQuestion, gbc);
		jplQuestion.add(jlbQuestion);
		// textfield
		final JTextField missionDir = new JTextField();
		missionDir.setText(dir.getPath());
		misDir = missionDir.getText();
		
		
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(missionDir, gbc);
		jplQuestion.add(missionDir); 
		
		JButton browse = new JButton("Browse...");
		
		final JDialog misd = this;
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser jfc = new JFileChooser();
				
				try {
					jfc.setCurrentDirectory(new File(missionDir.getText()));
				} catch (Exception ex) { } // don't care
				
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				jfc.showOpenDialog(misd);
				
				missionDir.setText(jfc.getSelectedFile().getPath());
				misDir = missionDir.getText();
				
			}
			
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gridbag.setConstraints(browse, gbc);
		jplQuestion.add(browse);
		
		
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(jplQuestion);
		
		
		mp = new MissionPanel();
		getContentPane().add(mp);
		
		JPanel okcan = new JPanel();
		okcan.setLayout(new BoxLayout(okcan, BoxLayout.X_AXIS));
		okcan.add(Box.createHorizontalGlue());
		JButton ok, cancel;
		ok = new JButton("Start");
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (mp.getSelectedMission() == null) {
					JOptionPane.showMessageDialog(misd, "You must select a mission to begin");
					
				} else {
					setVisible(false);
				}
				
			}
			
		});
		
		okcan.add(ok);
		okcan.add(Box.createHorizontalGlue());
		cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mp = null;
				setVisible(false);
				
			}
			
		});
		okcan.add(cancel);
		okcan.add(Box.createHorizontalGlue());
		
		getContentPane().add(okcan);
		getContentPane().add(new JLabel(" ")); // too lazy to do proper margin right now
		
		setModalityType(JDialog.DEFAULT_MODALITY_TYPE);
		setPreferredSize(new Dimension(500, 300));
		setIconImage(Images.frameIcon);
		setTitle("Select Mission");
		
		pack();
	
		
	}
}
