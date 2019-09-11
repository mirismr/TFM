package ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import jmr.result.ResultMetadata;

/**
 *
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class ImageListInternalFrame extends javax.swing.JInternalFrame {

    /**
     * 
     */
    public ImageListInternalFrame() {
        initComponents();
    }

    /**
     * 
     * @param list 
     */
    public ImageListInternalFrame(List<ResultMetadata> list) { //ResultList list
        this();
        if (list != null) {
            imageListPanel.add(list);
        }
    }

    /**
     * 
     * @param image 
     */
    public void add(BufferedImage image) {
        imageListPanel.add(image);
    }

    /**
     * 
     * @param image
     * @param label 
     */
    public void add(BufferedImage image, String label) {
        imageListPanel.add(image, label);
    }

    /**
     * 
     * @param imageURL
     * @param label 
     */
    public void add(URL imageURL, String label) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageURL);
            if (image != null) {
                imageListPanel.add(image, label);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageListPanel = new jmr.iu.ImageListPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Result");
        getContentPane().add(imageListPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private jmr.iu.ImageListPanel imageListPanel;
    // End of variables declaration//GEN-END:variables
}
