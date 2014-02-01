package net.kolls.railworld.opening;

/* 
 * Taken from:  VEX, a visual editor for XML
 *
 * Copyright (c) 2003 John Krasnay
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */


import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Component containing a list of checkboxes.
 * 
 * Copyright (c) 2003 John Krasnay
 * 
 * @author John Krasnay
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CheckBoxList extends JList {

    private boolean[] selected;
    
    /**
     * Class constructor.
     * @param items Items with which to populate the list.
     */
    @SuppressWarnings("unchecked")
	public CheckBoxList(Object[] items) {
        super(items);
        this.selected = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            this.selected[i] = false;
        }
        this.setCellRenderer(new CheckListCellRenderer());
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CheckListListener listener = new CheckListListener();
        this.addMouseListener(listener);
        this.addKeyListener(listener);
    }
    
    /**
     * Returns an array of the objects that have been selected.
     * Overrides the JList method.
     * @return An array of selected values
     */
    @Override
	public Object[] getSelectedValues() {
        ArrayList<Object> list = new ArrayList<Object>(this.selected.length);
        for (int i = 0; i < this.selected.length; i++) {
            if (selected[i]) {
                list.add(this.getModel().getElementAt(i));
            }
        }
        
        return list.toArray();
    }
    
    @Override
	public void setSelectedIndices(int[] indices) {
    	for (int i = 0; i < this.selected.length; i++) {
    		selected[i] = false;
    	}
    	for (int i = 0; i < indices.length; i++) {
    		selected[indices[i]] = true;
    	}
    }
    
    //===================================================== PRIVATE

    private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        
	private class CheckListCellRenderer 
        extends JCheckBox 
        implements ListCellRenderer {
    
    	/**
    	 * The cell renderer for each line item in the list box
    	 */
        public CheckListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(noFocusBorder);
        }        
        
		public Component getListCellRendererComponent(JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
        
            this.setText(value.toString());
            this.setSelected(selected[index]);
            this.setFont(list.getFont());
            
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {        
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }        
            
            if (cellHasFocus) {
                this.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
            } else {
                this.setBorder(noFocusBorder);
            }
            
            return this;
        }
    }
    
    private class CheckListListener implements MouseListener, KeyListener {
        
        public void mouseClicked(MouseEvent e) {
            if (e.getX() < 20) {
                // Heuristic that they clicked on the checkbox part
                doCheck();
            }
        }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == ' ') {
                doCheck();
            } 
        }
        public void keyTyped(KeyEvent e) { }
        public void keyReleased(KeyEvent e) { }
        
        private void doCheck() {
            int index = getSelectedIndex();
            if (index >= 0) {
                selected[index] = !selected[index];
            }
            repaint();
        }
    }
        

}