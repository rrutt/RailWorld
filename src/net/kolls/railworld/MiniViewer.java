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


import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

/**
 * The MiniViewer provides a small, square viewport to navigate a larger image which may only be partially displayed.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class MiniViewer extends Canvas implements MouseListener, MouseMotionListener {
	

	private BufferStrategy strategy;

	private BufferedImage bi;
	
	private int hostpx, hostpy, hostw, hosth;
	//private double sx, sy;
	private double scale;
	
	private int tx, ty;
	
	private int imgw, imgh;

	
	
	/**
	 * The RailCanvas to submit movement requests to.
	 */
	public RailCanvas rc;

	/**
	 * Construct a new miniviewer.  To use, you will then need to 
	 * {@link #config(int, int, int, int)} it and set the
	 * rail canvas {@link #rc}.
	 *
	 */
	public MiniViewer() {
			
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	
	
	// host width and height, source image width and height
	/**
	 * Configure the host width and height, and the actual scaled image width and height.
	 * This controls the red box that shows how much of the image is currently displayed.
	 * In some cases, the host dimension(s) may exceed the image dimensions.  This is fine.
	 * 
	 * @param hw Host (display pane) width
	 * @param hh Host height
	 * @param iw Image width
	 * @param ih Image height
	 */
	public void config(int hw, int hh, int iw, int ih) {
		hostw = hw;
		hosth = hh;
		
		imgw = iw;
		imgh = ih;
		double sx = (double)getWidth() / (double)imgw;
		double sy = (double)getHeight() / (double)imgh;
		// take the lesser of the two (side that is scaled more)
		if (sx < sy) {
			scale = sx;
			ty = (int)Math.round((getHeight()-scale*imgh)/2.0);
			tx = 0;
		
		} else {
			scale = sy;
			
			tx = (int)Math.round((getWidth()-scale*imgw)/2.0);
			ty = 0;
		
		}
		
		
	}
	
	
	
	/**
	 * Indicate the scaling factor between the miniviewer and the image. You must
	 * {@link #config}ure first.
	 * 
	 * @return A <code>double</code> indicating the scale factor.
	 */
	public double getScale() {
		return scale;
	}
	
	/**
	 * When the host changes its viewport display, it can inform the miniviewer so the red box
	 * follows appropriately.
	 * 
	 * @param px  Left edge of display on the host
	 * @param py Top edge of display on the host
	 */
	public void setPos(int px, int py) {
		hostpx = px;
		hostpy = py;
	}
	
	/**
	 * Draw the mini viewer canvas image and call the mini painter
	 * on the rail canvas.
	 */
	public void drawCanvas() {
		if (strategy == null) {
			createBufferStrategy(2);
			strategy = getBufferStrategy();
		}
		if (bi == null) {
			if (rc != null && rc.orig_src != null) {
				GraphicsConfiguration gfxc = getGraphicsConfiguration();
				
				
				
				// may be different from in config because this is actual image,
				// that one is display image (may be scaled, zoomed)
				double sx = (double)getWidth() / (double)rc.orig_src.getWidth();
				double sy = (double)getHeight() / (double)rc.orig_src.getHeight();
				double sc = Math.min(sx,sy);
				
				int sw = (int)(rc.orig_src.getWidth()*sc);
				int sh = (int)(rc.orig_src.getHeight()*sc);
				
				bi = gfxc.createCompatibleImage(sw, sh);
				Graphics2D ofg = bi.createGraphics();
				
				ofg.drawImage(rc.orig_src, 0, 0, sw, sh, this);
				ofg.dispose();
				
				
			} else return;
		}
		
		Graphics2D g2 = (Graphics2D)strategy.getDrawGraphics();
		
		g2.setColor(Color.black);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.translate(tx, ty);
		g2.drawImage(bi, 0, 0, this);
		rc.doMiniPaint(g2);
		g2.setColor(Color.red);
		g2.setStroke(new BasicStroke(1));
		g2.drawRect((int)(hostpx*scale), (int)(hostpy*scale), (int)(hostw*scale), (int)(hosth*scale));
		g2.dispose();

		strategy.show();
		
		
	}


	
	
	public void mouseClicked(MouseEvent e) { 
		double mxp, myp;
		mxp = e.getX();
		myp = e.getY();

		

		mxp -= tx;
		myp -= ty;
		
		// first, scale to absolute coordinates
		mxp /= scale;
		myp /= scale;
		
		mxp /= RailCanvas.zoom;
		myp /= RailCanvas.zoom;
		


		rc.submitCenterCoords((int)mxp, (int)myp);

		

	}
	public void mouseDragged(MouseEvent e) {
		mouseClicked(e);
	}
	public void mouseMoved(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }

}
