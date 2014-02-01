package net.kolls.railworld.opening;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import net.kolls.railworld.RailSegment;
import net.kolls.railworld.io.MetaData;
import net.kolls.railworld.io.RWMMapFilter;
import net.kolls.railworld.io.RWMReader;
import net.kolls.railworld.io.YardMapFilter;
import net.kolls.railworld.io.YardReader;
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
 * A class that loads maps and their images, along with other supporting data.
 * Includes functionality to prompt user to select a local file.
 * Instances of this class are created using factory methods which specify
 * how to get the map.
 * 
 * The image won't be loaded right away.  It will wait for the first call to
 * {@link #getImage()}. This allows uses of the map without the delay
 * of loading the image if it is not needed.
 * 
 * @author Steve Kollmansberger
 */
public abstract class MapLoader {

	private RailSegment[] lines;
	private MetaData md;
	private String filename;
	
	/**
	 * 
	 * @return Return the segments associated with the loaded map.
	 */
	public RailSegment[] getSegments() {
		return lines;
	}
	
	/**
	 * 
	 * @return Return the metadata associated with the loaded map.
	 */
	public MetaData getMetaData() {
		return md;
	}
	
	/**
	 * Loads another instance of this map from its original source (file or URL).
	 * Returns the new MapLoader containing the fresh instance.
	 * 
	 * @return A new MapLoader containing the freshly loaded data.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public abstract MapLoader loadAgain() throws IOException, SAXException;
	
	
	/**
	 * Loads the map image on the first call; caches it for subsequent calls.
	 * 
	 * @return The map image
	 * @throws IOException If the map image cannot be loaded.
	 */
	public abstract BufferedImage getImage() throws IOException;
	
	/**
	 * Protected constructor.  To create an instance, need to use static 
	 * factory methods depending on how to acquire the data. 
	 * 
	 * Values given will be loaded; script manager will be null.
	 * 
	 * @param la RailSegments
	 * @param mmd MetaData
	 * @param file the filename (not path) of the map file
	 * 
	 */
	protected MapLoader(RailSegment[] la, MetaData mmd, String file) {
		lines = la;
		md = mmd;
		filename = file;
		
	}
	
	

	/**
	 * Popup a file chooser dialog to allow the user to select a map file
	 * locally.
	 * 
	 * @param scripts If provided, indicates that a {@link ScriptPanel} should
	 * be shown allowing the user to choose scripts.  The given script manager
	 * (assumed to be empty when passed in; but will be cleared if not)
	 * will then be loaded with the selected scripts.
	 * @param directory Specifies the directory to start in.  May be null.
	 * @return A new MapLoader with the user selected map if one was selected,
	 * null otherwise.
	 * @throws IOException If the map selected by the user cannot be loaded.
	 * @throws SAXException If the map selected by the user cannot be parsed.
	 */
	public static MapLoader loadFromUserPrompt(ScriptManager scripts, File directory) throws IOException, SAXException {
		final JFileChooser jfc = new JFileChooser();
		jfc.addChoosableFileFilter(new YardMapFilter());
		jfc.addChoosableFileFilter(new RWMMapFilter());
		if (directory != null) jfc.setCurrentDirectory(directory);
		final ScriptPanel sp = new ScriptPanel();
		final JPanel jsp = new JPanel();
		jsp.setLayout(new BorderLayout());
		jsp.add(new JLabel("Select Script(s) to Use"), BorderLayout.NORTH);
		jsp.add(sp, BorderLayout.CENTER);
		
		if (scripts != null) {
			
			
			/* jfc.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();

				    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
				    	
				    	File f = (File)e.getNewValue();
				    	sp.setDirectory(f);
				    }
					
					
				}
			}); */
			jfc.setAccessory(jsp);
		} 
		
		
		
		int rv = jfc.showOpenDialog(null);
		if (rv == JFileChooser.APPROVE_OPTION) {
			if (scripts != null) {
				scripts.clear();
				for (int i = 0; i < sp.getScripts().length; i++) {
					scripts.add( sp.getScripts()[i] );
				}
				
			}
			File file = jfc.getSelectedFile();
			return loadFromFile(file);
		}
		return null;
		
	}
	
	/**
	 * Loads the map from the given file.   The map image must be in the same directory.
	 * 
	 * @param file The file containing a compatible map to load, either in Rail World 
	 * or Yard Duty format.
	 * @return The map loader containing the file data.
	 * @throws IOException If the map cannot be loaded.
	 * @throws SAXException If the map cannot be parsed.
	 */
	public static MapLoader loadFromFile(final File file) throws IOException, SAXException {

			
	        
			String u = file.getName().toUpperCase();
			RailSegment[] la = null;
			MetaData md = new MetaData();
			
			
			
			if (u.endsWith(".RWM")) {
				// It's in the native XML format
				la = RWMReader.read(file, md);
				
			}
			if (u.endsWith(".YRD")) {
				// Yard duty format
				la = YardReader.read(file, md);
			}
			
			if (la == null) throw new IOException("Unknown file format");
			
			
			final File imgfile = new File (file.getParentFile() + File.separator + md.imgfile);
			
			        
			return new MapLoader(la, md, u) {

				private BufferedImage bi;
				
				@Override
				public BufferedImage getImage() throws IOException {
					if (bi != null) return bi;
					
					bi = ImageIO.read(imgfile);	
					
					return bi;
				}
				
				@Override
				public MapLoader loadAgain() throws IOException, SAXException {
					return loadFromFile(file);
				}
				
			};
	        	            
	  	
			
	}
	
	
	
	/**
	 * Loads the map from the given URL.  The map image must be in the same directory.
	 * 
	 * @param url The URL pointing to a compatible map to load, in Rail World 
	 * format.  Yard Duty files cannot be loaded over URLs.
	 * @return The map loader containing the file data.
	 * @throws IOException If the map cannot be loaded.
	 * @throws SAXException If the map cannot be parsed.
	 */
	public static MapLoader loadFromURL(final URL url) throws IOException, SAXException {

			
	        
			String u = url.getPath().toUpperCase();
			RailSegment[] la = null;
			MetaData md = new MetaData();
			
			
			
			if (u.endsWith(".RWM")) {
				// It's in the native XML format
				la = RWMReader.read(url, md);
				
			}
			if (u.endsWith(".YRD")) {
				// Yard duty format
				// la = YardReader.read(url, md);
				throw new IOException("Yard duty files cannot be loaded via URLs");
			}
			
			if (la == null) throw new IOException("Unknown file format");
			
			
			
			final URL imgurl = new URL (url, md.imgfile);
			
			return new MapLoader(la, md, url.getPath().substring(url.getPath().lastIndexOf("/") + 1)) {

				private BufferedImage bi;
				
				@Override
				public BufferedImage getImage() throws IOException {
					if (bi != null) return bi;
					
					bi = ImageIO.read(imgurl);	
					
					return bi;
				}
				
				@Override
				public MapLoader loadAgain() throws IOException, SAXException {
					return loadFromURL(url);
				}
				
			};
	        	            
	  	
			
	}
	
	/**
	 * Find the filename (without path) of the map file.
	 * 
	 * @return The filename, possibly in different capitalization than the actual file.
	 */
	public String getFilename() {
		System.out.println(filename);
		return filename;
	}
	
	
	@Override
	public String toString() {
		if (md == null) return "";
		return md.title;
	}
	
	
}
