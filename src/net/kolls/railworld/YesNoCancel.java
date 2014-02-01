package net.kolls.railworld;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

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
 * Displays a JOptionPane dialog with three buttons: yes, no, and cancel.
 * However, the face of the buttons is given by the caller.  The return values
 * follow the usual JOptionPane constants of YES_OPTION, NO_OPTION, and
 * CANCEL_OPTION.
 * 
 * @author Steve Kollmansberger
 */
public class YesNoCancel {

	/**
	 * Create a new dialog showing a yes, no, and cancel option with text
	 * specified by the user.
	 * 
	 * @param parent The parent frame
	 * @param message The message to display
	 * @param title Title of the dialog
	 * @param yes The text for the "yes" button
	 * @param no The text for the "no" button
	 * @param cancel The text for the "cancel" button
	 * @return A code indicating which button was selected; {@link JOptionPane#YES_OPTION},
	 * {@link JOptionPane#NO_OPTION}, or {@link JOptionPane#CANCEL_OPTION}.  If neither
	 * button is clicked, the cancel value is returned.
	 * 
	 * @see JOptionPane
	 */
	public static int showDialog(Component parent, String message, String title, String yes, String no, String cancel) {
		JOptionPane jop = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, 
				JOptionPane.YES_NO_CANCEL_OPTION, null, new String[] {yes,no,cancel});
		JDialog dialog = jop.createDialog(parent, title);
	    dialog.setVisible(true);
	    Object selectedValue = jop.getValue();
	    if (selectedValue.equals(yes)) return JOptionPane.YES_OPTION;
	    if (selectedValue.equals(no)) return JOptionPane.NO_OPTION;
	    return JOptionPane.CANCEL_OPTION;
	    
		
		
	}
	
}
