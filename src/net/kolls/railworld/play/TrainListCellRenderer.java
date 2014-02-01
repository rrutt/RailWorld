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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import javax.swing.*;

/**
 * Renders a list cell for an active train.
 * Includes a small icon showing the train's position, along with the train's speed and weight.
 * 
 * @author From Java forums, modified by Steve Kollmansberger
 *
 */
public class TrainListCellRenderer extends JPanel implements ListCellRenderer {

	private PlayFrame pf;
	private Point2D p;
	private Train myT;
	
	/**
	 * Creates the renderer.
	 * 
	 * @param pf PlayFrame with source image
	 * @param metrics Font information
	 * @param width The width of the cell.  Height is fixed at 32.
	 */
	public TrainListCellRenderer(PlayFrame pf, FontMetrics metrics, int width) {
		super();
		//rc = mrc;
		this.pf = pf;
		
		baseline = metrics.getAscent() + borderWidth;
		this.height = /*32;*/ metrics.getHeight() + (2 * borderWidth);
		this.width = width;	
		
		
	}
	String text;
    final int borderWidth = 2;
    final int baseline;
    final int width;
    final int height;
    
    

    // I got this off the internet
    /** 
     * Return the renderers fixed size here.  
     */
    @Override
	public Dimension getPreferredSize() {
    	return new Dimension(width, height);
    }

    
    /**
     * Completely bypass all of the standard JComponent painting machinery.
     * This is a special case: the renderer is guaranteed to be opaque,
     * it has no children, and it's only a child of the JList while
     * it's being used to rubber stamp cells.
     * <p>
     * Clear the background and then draw the text.
     */
    @Override
	public void paint(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;
    	
    	g2.setColor(getBackground());
    	g2.fillRect(0, 0, getWidth(), getHeight());
    	
    	
    	
    	
    	g2.setColor(getForeground());
    	g2.drawString(text, 32+borderWidth*2, baseline);
    	g2.translate(1,1);
    	
    	/*
    	BufferedImage thumb = crw.get(myT);
    	if (thumb != null)
    		g2.drawImage(thumb, 0, 0, this);
    		*/
    	p = myT.pos.getPoint();
    	int x1 = (int)(p.getX()*Distance.getDefaultZoom());
    	int y1 = (int)(p.getY()*Distance.getDefaultZoom());
    	
    	// center on this point
    	x1 -= 16;
    	y1 -= 16;
    	
    	g2.drawImage(pf.tlcr, 0, 0, 31, 31, x1, y1, x1+32, y1+32, null);  
    			
    	
    	
    	
    }


    /* This is is the ListCellRenderer method.  It just sets
     * the foreground and background properties and updates the
     * local text field.
     */
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) 
    {
	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	}
	else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}
	
	if (value == null) return this;
	
	myT = (Train)value;
	
	
	
	final int ispeed = new Double(myT.vel()).intValue();
	final String ft = NumberFormat.getInstance().format(myT.length().feet());
	final String w = NumberFormat.getInstance().format(myT.weight());
	text = Integer.toString(ispeed) + " MPH, " + ft + " Feet, " + w + " Tons";
	//p = myT.pos.getPoint();
	//crw.add(myT);

	return this;
    }

}
