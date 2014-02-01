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

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import javax.swing.JPanel;



import net.kolls.railworld.Car;
import net.kolls.railworld.Distance;
import net.kolls.railworld.Train;
import net.kolls.railworld.tuic.TrainPainter;

/**
 * Displays a train in a panel of a given width, perhaps using multiple lines to display the train.
 * 
 * 
 * @author Steve Kollmansberger
 *
 */
public class MultiLineTrainPanel extends JPanel {

	/**
	 * Cars, in order, that compose the train.  The user of this class can modify this vector
	 * and then ask the panel to be repainted.
	 */
	public ArrayList<Car> myVC;
	
	/**
	 * Create a panel with the empty vector of cars.
	 *
	 */
	public MultiLineTrainPanel() {
		myVC = new ArrayList<Car>();
		
		
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		
		int width = getWidth() - 5;
		
		if (width < 1 || myVC.isEmpty()) return;
		
		int height = (int)(Car.CAR_WIDTH.pixels() * Distance.getDefaultZoom()), usedw = 0;
		
		
		ArrayList<Train> ptrains = new ArrayList<Train>();
		ArrayList<Car> ct = new ArrayList<Car>();
		Iterator<Car> ic = myVC.iterator();
		
		
		Car[] cr;
		
		while (ic.hasNext()) {
			Car c = ic.next();
			
			if (c.length().pixels() * Distance.getDefaultZoom() + usedw + 5 > width) {
				height += Car.CAR_WIDTH.pixels() * Distance.getDefaultZoom() * 1.5;
				usedw = 0;
				
				// current ct and make it a train
				cr = (ct.toArray(new Car[0]));
				ptrains.add(new Train(cr));
				ct.clear();
					
			} 
			usedw += (c.length().pixels() + Car.DIST_BETWEEN_CARS.pixels()) * Distance.getDefaultZoom();
			ct.add(c);
			
		
		}
		
		//add remaining cars
		cr = (ct.toArray(new Car[0]));
		ptrains.add(new Train(cr));
		
		
		Graphics2D ofg = (Graphics2D)g;
		

		ofg.setColor(Color.lightGray.brighter());
		ofg.fillRect(0, 0, width, height);
		
		ofg.scale(Distance.getDefaultZoom(), Distance.getDefaultZoom());
		TrainPainter tp = new TrainPainter(null, ofg, false, null);
		
		Iterator<Train> it = ptrains.iterator();
		
		int y = Car.CAR_WIDTH.iPixels() / 2 + 1;
		int x = 1;
		while (it.hasNext()) {
			Train t = it.next();
			
			cr = t.array();
			for (int i = 0; i < cr.length; i++) {
				tp.segment(cr[i], new Line2D.Double(x,y,x+cr[i].length().iPixels(),y));
				x += cr[i].length().iPixels() + Car.DIST_BETWEEN_CARS.iPixels();
			}
			x = 1;
			y += Car.CAR_WIDTH.iPixels() * 1.5;
		}
		
		
		setPreferredSize(new Dimension(width+5, height));
		revalidate();
		
	}
}
