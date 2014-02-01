package net.kolls.railworld.scripts.missions.support;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.kolls.railworld.GameLoop;
import net.kolls.railworld.RailCanvas;
import net.kolls.railworld.RailSegment;
import net.kolls.railworld.play.PlayFrame;
import net.kolls.railworld.play.script.DrawListener;
import net.kolls.railworld.play.script.Script;



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
 * A "service" draw listener which provides for fading
 * messages, possibly stacked, to be displayed on the main canvas.
 * 
 * @author Steve Kollmansberger
 * 
 */
public class FadePrinter implements DrawListener {

	protected final int MILLISECONDS_TO_FADE = 6000;
	
	protected class TextPack {
		public String message;
		public Color color;
		public long displayElapsed;
	}
	
//	private TimeQueue<TextPack> futureMessages;
	private List<TextPack> currentMessages;
	private GameLoop gl;
	
	public void add(String message, Color color /*, long displayElapsed */) {
		TextPack tp = new TextPack();
		tp.color = color;
		tp.message = message;
		tp.displayElapsed = gl.elapsed;
		currentMessages.add(tp);
	}
	
	public FadePrinter(GameLoop gl) {
	//	futureMessages = new TimeQueue<TextPack>();
		currentMessages = new ArrayList<TextPack>();
		this.gl = gl;
	}
	
	@Override
	public void draw(Graphics2D gc, Rectangle onScreen) {

		AffineTransform at = gc.getTransform();
		gc.setTransform(new AffineTransform());
		// step 1. move any futures that are due into current
		// we'll just one per step
		
		//TimeQueue<TextPack>.TimeValue<TextPack> tv = futureMessages.poll(gl.elapsed);
		//if (tv != null) {
			//currentMessages.add(0, tv.value);
		//}
		
		// step 2. remove any old messages from current
		// again, just do one per step at this time
		if (!currentMessages.isEmpty() && gl.elapsed > currentMessages.get(0).displayElapsed + MILLISECONDS_TO_FADE) {
			currentMessages.remove(0);
			
		}
		
		// step 3. display!
		int y = 70;
		for (TextPack tp : currentMessages) {
			
			
			double alpha = 0;
			double dt = gl.elapsed - (tp.displayElapsed + MILLISECONDS_TO_FADE / 2);
			alpha = Math.max(0.0, dt / (MILLISECONDS_TO_FADE / 2.0));
			
			alpha = (1.0 - alpha) * 255.0; // in the first part, gl.elapsed will be less than half way through the fade
			// so we end up with a negative value, which becomes 0 (full)
			// then after passing it gradually increases to 1 (disappeared)
			
			if (alpha > 255.0)
				continue;
			
			Color cp = new Color(tp.color.getRed(), tp.color.getGreen(), tp.color.getBlue(), (int)alpha);
			
			RailCanvas.drawOutlineFont(gc, 20, y, tp.message, 16, cp, 0, false);
			
			y += 20;
		}
		
		
		gc.setTransform(at);
	}


}
