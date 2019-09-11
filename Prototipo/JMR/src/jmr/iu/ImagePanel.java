package jmr.iu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Panel for showing an image with tip information (if available).
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class ImagePanel extends javax.swing.JPanel {
    private BufferedImage image;
    
    /**
     * Constructs a new image panel using as preferred size the image dimension
     * 
     * @param image the image to be shown in the panel
     * @param label info associated to the image
     */
    public ImagePanel(BufferedImage image, String label) {
        initComponents();
        this.setImage(image);
        this.setLabel(label);
        this.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));               
    }
    
    /**
     * Constructs a new image panel without info associated to the image and
     * using as preferred size the image dimension.
     * 
     * @param image the image to be shown in the panel
     */
    public ImagePanel(BufferedImage image) {
        this(image,null);        
    }
    
    /**
     * Paint this image panel. The image is scaled to fit inside the panel size
     * 
     * @param g graphic object
     */
    @Override
    public void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
        }
    }
    
    /**
     * Set the image of this panel.
     * 
     * @param image the image to be shown
     */
    public final void setImage(BufferedImage image){
        this.image = image;
    }
    
    /**
     * Return the image of this panel
     * 
     * @return the image of this panel
     */
    public BufferedImage getImage(){
        return this.image;
    }
    
    /**
     * Set the tip label
     * 
     * @param label the tip label
     */
    public final void setLabel(String label){
        this.setToolTipText(label);
    }
    
    /**
     * Returns the tip label.
     * 
     * @return the tip label.
     */
    public final String getLabel(){
        return this.getToolTipText();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        //If the tooltip is active, then this panel catch all the mouse events.
        //The following code is for mouse clicked redispatching to the parent
        javax.swing.JComponent source = (javax.swing.JComponent) evt.getSource();
        java.awt.event.MouseEvent parentEvent = 
                javax.swing.SwingUtilities.convertMouseEvent(source,evt,source.getParent());
        source.getParent().dispatchEvent(parentEvent);
    }//GEN-LAST:event_formMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

