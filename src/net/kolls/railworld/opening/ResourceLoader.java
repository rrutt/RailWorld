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


import java.net.URL;

import javax.swing.JProgressBar;

import net.kolls.railworld.Factories;
import net.kolls.railworld.Images;
import net.kolls.railworld.Sounds;

/**
 * Loads basic resources, such as images, sounds, and icons.
 * Will also load maps for Applets.  Designed to be run
 * in a thread upfront (on the splash screen or other
 * initial display), displays as a progress bar
 * showing how much has been loaded. 
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class ResourceLoader extends JProgressBar implements Runnable {

	
	/**
	 * When used with an applet, will contain all the loaded maps when finished.
	 */
	public MapLoader[] maps = {};
	
	private Applet a;
	
	/**
	 * Create a resource loader.  If the applet parameter is specified, only
	 * applet-related resources are loaded.  In addition, the applet's MAPS
	 * parameter will be tapped and all maps specified there-in will be loaded.
	 * 
	 * @param applet Specified if the current run is applet, otherwise null.
	 */
	public ResourceLoader(Applet applet) {
		a = applet;
		
	}
	
	/**
	 * Begins the resource loading.  This is set up to allow the loader
	 * to be a thread runnable.
	 */
	public void run() {
		// configure the progress bar
		setMinimum(0);
		// icons, opening or applet, 7 regular sounds, 9 engine sounds,
		// if applet, however many maps there are
//		String mapp = null;
//		if (a != null) mapp = a.getParameter("MAPS");
		String[] mapurls;
//		if (mapp != null) {
//			mapurls = mapp.split(",");
//		} else {
			mapurls = new String[0];
//		}
		
		setMaximum(1+1+7+9+mapurls.length);
		setValue(0);
		
		// prepare options
		Options.loadPreferences();
		
		Images imgs = new Images();
		// first load opening/applet logos
		imgs.loadFrameIcon();
		if (a != null)
			imgs.loadApplet();
		else 
			imgs.loadOpening();
		
		setValue(1);
		
		// next load what everyone needs: icons and sound
		imgs.loadIcons();
		setValue(2);
		
        Sounds s = new Sounds();
        s.loadSounds(this, Options.getSoundSystem());
        
        // before maps can be loaded, the factories must be initialized
        Factories.init();
        
		// load all applet maps
        // for non-applet, mapurls will be length 0
        // so nothing will happen
		maps = new MapLoader[mapurls.length];
		for (int i = 0; i < mapurls.length; i++) {
			try {
				maps[i] = MapLoader.loadFromURL(new URL(mapurls[i]));
			} catch (Exception ex) {
				maps[i] = null;
				System.out.println("Unable to load map "+mapurls[i]+", reason: ");
				ex.printStackTrace();
			}
			setValue(getValue() + 1);
		}
			
		setValue(getMaximum());
	}

}
