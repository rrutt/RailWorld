package net.kolls.railworld.opening;



import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.border.Border;

// got this off java forums
/**
 * A border which displays an image.  Used for {@link Opening} frame.
 * 
 * @author Posted on Java forums
 */
public class CenteredBackgroundBorder implements Border {
    private final BufferedImage image;

    /**
     * Construct a background based on an image
     * 
     * @param image
     */
    public CenteredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int x0 = x + (width-image.getWidth())/2;
        int y0 = y + (height-image.getHeight())/2;
        g. drawImage(image, x0, y0, null);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(0,0,0,0);
    }

    public boolean isBorderOpaque() {
        return true;
    }
}