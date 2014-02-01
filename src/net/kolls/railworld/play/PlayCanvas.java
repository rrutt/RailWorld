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

import net.kolls.railworld.*;
import net.kolls.railworld.play.script.ScriptManager;
import net.kolls.railworld.tuic.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.ArrayList;





/**
 * A {@link RailCanvas} for the play module.
 * 
 * @author Steve Kollmansberger
 *
 */
@SuppressWarnings("serial")
public class PlayCanvas  extends RailCanvas {
	

	/**
	 * The trains in play
	 */
	public Trains trains;
	
	/**
	 * The script manager containing all active scripts
	 */
	public ScriptManager sm;
	
	/**
	 * Should we show context information in the canvas based on mouse cursor?
	 */
	public boolean showContextInfo;
	
	private ArrayList<RailSegment> dynamicSegs;
	private int currmx, currmy;
	
	
	/**
	 * Create a play canvas.
	 * 
	 * @param s The source image.
	 * @param lines The rail segments.
	 * @param mini The miniviewer to update.
	 */
	public PlayCanvas(BufferedImage s, RailSegment[] lines, MiniViewer mini) {
		
		super(s,lines,mini);
		
		dynamicSegs = new ArrayList<RailSegment>();
		int i;
		
		
		
		for (i = 0; i < lines.length; i++) 
			if (lines[i].isDynamic()) 
				dynamicSegs.add(lines[i]);
		
		
		
		currmx = currmy = -1;
		showContextInfo = true;

		
		
	}

	
	
	
	@Override
	public void doMiniPaint(Graphics2D g) {
		for (Train t : trains) {
			TrainMiniPainter tac = new TrainMiniPainter(g, miniv.getScale() * RailCanvas.zoom, miniv.getScale() * RailCanvas.zoom);
			tac.act(t);
		}
	}	


	
	// shadow vx and vy
	@Override
	protected void doPaint(Graphics2D g, int hvx, int hvy, boolean detailed) {
		
		
		
		for (RailSegment r : dynamicSegs)
			r.draw(4, g);
		
		
		
		// why two loops?  Because the drawing may need the trains vector
		// of ANY particular segment
		// so only after all dynamic drawing is complete
		// can the trains vector be cleared
		for (RailSegment r : la)
			r.trains().clear();
			
		
		
			
		for (Train t : trains) {
			TrainPainter tac = new TrainPainter(this, g, t.followMe || t.followMeOnce, trains.getSelectedCar());
			
			tac.act(t);
			if (tac.didFollow) {
				t.followMeOnce = false;
			}
		}
		
		if (!detailed) return;
		
		
		// perform script painting
		sm.draw(g, new Rectangle(hvx, hvy, this.getWidth(), this.getHeight()));
		
		// show info based on mouse position
		if (currmx > -1 && showContextInfo) {
			String desc = null;
			TrainClickTest tct = new TrainClickTest(currmx, currmy);
			for (Train t : trains) {
				tct.act(t);
				if (tct.cc != null) {
					desc = tct.cc.show();
					if (tct.cc.isLoadable()) desc += tct.cc.loaded() ? " (loaded)" : " (empty)";
					
					break;
				}
			}
			
			// if no train, then consider segments
			Point2D pos = new Point2D.Double(currmx, currmy);
			if (desc == null) {
				for (RailSegment r : dynamicSegs) {
					desc = r.mouseOver(pos);
					if (desc != null) {
						break;
					}
				}
			}
			
			
			if (desc != null) {
				drawOutlineFont(g, currmx, currmy, desc, (int)(12.0 / g.getTransform().getScaleX()), Color.white, 0, false);
				
			}
			
			
		}
		
	
	}


	


	@Override
	public void mouseClicked(MouseEvent e) { 
		
		Point2D p = transform(e);

		// check to see if we select a train
		Train t;
		int i;
		TrainClickTest tc = new TrainClickTest((int)p.getX(), (int)p.getY());
		
		boolean didWork = false;
		for (i = 0; i < trains.size(); i++) {
			t = trains.get(i);
			tc.act(t);
			if (tc.cc != null) {
				didWork = true;
				// clicked on this train
				trains.select(t, tc.cc);
				return; // don't process track events
			}
		}
		
		
		// apply any behavior for track items
		for ( i = 0; i < dynamicSegs.size(); i++) {
			if (dynamicSegs.get(i).click(p, this)) {
				didWork = true;
				break;
			}
		} 

		
		if (didWork == false) trains.select(null, null);
		
		
	}
	
	
	
	@Override
	public void leftDrag(MouseEvent e) {
		
		
		Train t = trains.getSelectedTrain();
		if (t != null) t.followMe = false;
		

	}
	
	@Override
	public void leftMove(MouseEvent e) { 
		
		Point2D p = transform(e);
		currmx = (int)p.getX();
		currmy = (int)p.getY();
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {	
		currmx = currmy = -1;
	}
	
}
