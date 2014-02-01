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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.kolls.railworld.Distance;
import net.kolls.railworld.Images;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.Train;
import net.kolls.railworld.tuic.TrainDirectionFinder;

/**
 * Creates a window to display an accident report.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class RailAccidentFrame extends JFrame {

	
	
	/**
	 * Display an accident.
	 * The canvas is used to acquire the image of the scene.
	 * 
	 * @param rc A {@link PlayCanvas} containing the accident location.
	 * @param ra A {@link RailAccident} containing the accident data.
	 * @param title Title of the map.  This is not the title of the accident report window.
	 */
	public RailAccidentFrame(PlayCanvas rc, RailAccident ra, String title) {
		super("Rail Accident Report");
		setIconImage(Images.frameIcon);
		
		// if one train is stopped, it should be second train
		if (ra.t2 != null) {
			Train t;
			if (ra.t1.vel() == 0 && ra.t2.vel() > 0) {
				t = ra.t1;
				ra.t1 = ra.t2;
				ra.t2 = t;
			}
		}
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		JLabel rar = new JLabel("Rail Accident Report");
		Font f = new Font("SansSerif", Font.ITALIC, 30);
		rar.setFont(f);
		
		rar.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		getContentPane().add(rar);
		
		JLabel ct = new JLabel();
		ct.setFont(ct.getFont().deriveFont(Font.BOLD));
		ct.setText(ra.title());
		
		ct.setAlignmentX(Component.CENTER_ALIGNMENT);
		getContentPane().add(ct);
		
		JLabel dt = new JLabel();
		dt.setFont(dt.getFont().deriveFont(Font.BOLD));
		
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
		Date cdt = rightNow.getTime();
		
		dt.setText(sdf.format(cdt));
		
		JTextArea desc = new JTextArea();
		
		sdf = new SimpleDateFormat("h:mm a");
		SimpleDateFormat sdf2 = new SimpleDateFormat("EEE MMM d, yyyy");
		
		TrainDirectionFinder tdf = new TrainDirectionFinder();
		tdf.act(ra.t1);
		
		String t1adesc = tdf.adesc;/*direc(t1ang)*/
		String t2adesc = "";
		
		
		if (ra.t2 != null) {
			tdf.reset();
			tdf.act(ra.t2);
			t2adesc = tdf.adesc;
		}
		
		StringBuilder sb = new StringBuilder();
		
		
		sb.append("About " + sdf.format(cdt) + " on " + sdf2.format(cdt) + ", a ");
		// TODO: fix nullbound
		sb.append(t1adesc +"bound ");
		appendTrainInfo(ra.t1, sb);
		sb.append(" ");
		
		sb.append(ra.midbody());
		if (ra.t2 != null) {
			int i = sb.indexOf("{2d}");
			sb.append(" ");
			if (i > -1)
				sb.replace(i, i+4, t2adesc+"bound ");
			appendTrainInfo(ra.t2, sb);
		}
		sb.append(".");
		
		
		
		
		sb.append(" The accident occured in the ");
		sb.append(myloc(rc, ra.pos));
		sb.append(" ");
		sb.append(title);
		sb.append(" area.");
		
		
		
		// should we be using line.seperator here?
		
		sb.append("\n\nThe train");
		if (ra.t2 != null) sb.append("s");
		sb.append(" involved ");
		if (ra.t2 != null) sb.append("have"); else sb.append("has");
		sb.append(" been removed from play.  The game is paused.");
		
		
		
		
		desc.setText(sb.toString());
		desc.setLineWrap(true);
		desc.setWrapStyleWord(true);
		desc.setEditable(false);
		
		JPanel df = new JPanel();
		df.setLayout(new BoxLayout(df, BoxLayout.LINE_AXIS));
		df.add(Box.createRigidArea(new Dimension(5,5)));
		df.add(desc);
		df.add(Box.createRigidArea(new Dimension(5,5)));
		
		JPanel sc = new JPanel();
		sc.setLayout(new BoxLayout(sc, BoxLayout.PAGE_AXIS));
		
		GraphicsConfiguration gc = getGraphicsConfiguration();
		BufferedImage bi = gc.createCompatibleImage(128, 128);
		Graphics2D g = bi.createGraphics();
		rc.draw(g, ra.pos, 128, 128, Distance.getDefaultZoom(), false);
		g.dispose();
		
		// got train info and graphic
		// remove trains
		
		rc.trains.remove(ra.t1);
		if (ra.t2 != null)
			rc.trains.remove(ra.t2);
		if (rc.trains.getSelectedTrain() == ra.t1 ||
			rc.trains.getSelectedTrain() == ra.t2)
				rc.trains.select(null, null);
		
		
		sc.add(new JLabel(new ImageIcon(bi)));
		sc.add(Box.createRigidArea(new Dimension(5,5)));
		JLabel scl = new JLabel();
		scl.setFont(scl.getFont().deriveFont(Font.ITALIC));
		scl.setText("Scene of the Accident");
		sc.add(scl);
		
		df.add(sc);
		df.add(Box.createRigidArea(new Dimension(5,5)));
		
		getContentPane().add(df);
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		ok.setAlignmentX(Component.CENTER_ALIGNMENT);
		getRootPane().setDefaultButton(ok);
		getContentPane().add(ok);
		
		setPreferredSize(new Dimension(500,300));
		
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		
	}
	
	private void appendTrainInfo(Train t, StringBuilder sb) {
		sb.append(t.array().length);
		sb.append("-car train weighing ");
		sb.append(NumberFormat.getInstance().format(t.weight()));
		sb.append(" tons and traveling at ");
		sb.append((int)t.vel());
		sb.append(" MPH");
	}
	
	private String myloc(RailCanvas rc, Point2D p) {
		Dimension d = rc.areaSize();
		int x, y;
		
		if (p.getX() < d.width / 3) x = -1;
		else if (p.getX() < 2*d.width / 3) x = 0;
		else x = 1;
		
		if (p.getY() < d.height / 3) y = -1;
		else if (p.getY() < 2*d.height / 3) y = 0;
		else y = 1;
		
		if (x == 0 && y == 0) return "central";
		
		String s = "";
		
		if (y == -1) s = "north";
		if (y == 1) s = "south";
		
		if (x == -1) s += "west";
		if (x == 1) s += "east";
		
		return s;
	}
	
	
}
