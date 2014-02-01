package net.kolls.railworld;

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


import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


/**
 * A class to manage all the built-in images the program needs.  This class
 * does not manage the map images.  The methods within are not static because
 * they need to use getClass to find the resource location.
 * 
 * @author Steve Kollmansberger
 *
 */
public class Images {
	
	
	public static ImageIcon pointer, eraser, track, lu, hidden, hiddenlu, ee, crossing, curve;
	public static ImageIcon save, stop, zoomin, zoomout, edit, information, sep, pause, populate, newt, delete, importt, exportt, contexthelp;
	public static ImageIcon undo, redo;
	public static BufferedImage opening;
	public static BufferedImage applet;
	public static Image frameIcon;
	
	
	public static ImageIcon sp_green, sp_yellow, sp_red, sp_greenred, sp_yellowred, sp_reduturn, sp_ulgreenred, sp_uluturn;
	
	
	
	/**
	 * Load all icons from JAR file or network.
	 */
	public void loadIcons() {
		pointer = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Pointer.gif"));
		eraser = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Eraser.gif"));
		track = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Track.gif"));
		lu = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/LU.gif"));
		hidden = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Hidden.gif"));
		hiddenlu = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/HiddenLU.gif"));
		ee = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/EE.gif"));
		crossing = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Crossing.gif"));
		curve = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Curve.gif"));
		
		save = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Save.gif"));
		stop = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Stop.gif"));
		zoomin = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/ZoomIn.gif"));
		zoomout = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/ZoomOut.gif"));
		edit = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Edit.gif"));
		information = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Information.gif"));
		pause = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Pause.gif"));
		
		sep = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/SEP.gif"));
		populate = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Populate.gif"));
		
		newt = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/New.gif"));
		delete = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Delete.gif"));
		importt = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Import.gif"));
		exportt = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Export.gif"));
		contexthelp = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/ContextualHelp.gif"));
		
		undo = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Undo.gif"));
		redo = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/Redo.gif"));
	
		
		sp_red = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/Red.gif"));
		sp_yellow = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/Yellow.gif"));
		sp_green = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/Green.gif"));
		sp_greenred = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/GreenRed.gif"));
		sp_yellowred = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/YellowRed.gif"));
		sp_reduturn = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/RedUTurn.gif"));
		sp_ulgreenred = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/ULGreenRed.gif"));
		sp_uluturn = new ImageIcon(getClass().getResource("/net/kolls/railworld/images/sp/ULUTurn.gif"));
	}
	
	/**
	 * Load the main application menu image.
	 * @see #opening
	 */
	public void loadOpening() {
		try {
			opening = ImageIO.read(getClass().getResource("/net/kolls/railworld/images/Opening.png"));
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	/**
	 * Load the applet title bar image.
	 * @see #applet
	 */
	public void loadApplet() {
		try {
			applet = ImageIO.read(getClass().getResource("/net/kolls/railworld/images/Applet.png"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Load the window icon.
	 * @see #frameIcon
	 */
	public void loadFrameIcon() {
		try {
			frameIcon = ImageIO.read(getClass().getResource("/net/kolls/railworld/images/Icon.png"));
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
