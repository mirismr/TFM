package jmr.iu;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import jmr.result.ResultMetadata;

/**
 * Panel for showing a list of images. 
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class ImageListPanel extends javax.swing.JPanel {
    /**
     * Internal panel for allocating images.
     */
    private JPanel internalPanel;    
    /**
     * View size for the images showed in this panel.
     */
    private Dimension imageViewSize;   
    /**
     * Default size for the images showed in this panel.
     */
    public final static Dimension DEFAULT_IMAGE_VIEW_SIZE = new Dimension(100,100);
    /**
     * The image selection listener associated to this panel (only one is
     * allowed).
     */
    ImageSelectionListener imageSelectionEventListener=null;
    
    /**
     * Constructs an empty list panel with {@link #DEFAULT_IMAGE_VIEW_SIZE} as 
     * default image view size.
     * 
     */
    public ImageListPanel() {
        this(null,DEFAULT_IMAGE_VIEW_SIZE);
    }

    /**
     * Constructs an empty list panel.
     *
     * @param imageViewSize view size for the images showed in this panel.
     */
    public ImageListPanel(Dimension imageViewSize) {
        this(null, imageViewSize);
    }

    /**
     * Constructs a list panel from the data stored in the given metadata list 
     * and using {@link #DEFAULT_IMAGE_VIEW_SIZE} as default image view size.
     * 
     * @param list list of {@link jmr.result.ResultMetadata} objetcs. The
     * metadata of each result must be an image (if not, an exception is thrown)
     */
    public ImageListPanel(List<ResultMetadata> list) {
        this(list,DEFAULT_IMAGE_VIEW_SIZE); 
    }
    
    
    /**
     * Constructs a list panel from the data stored in the given metadata list.
     * 
     * @param list list of {@link jmr.result.ResultMetadata} objetcs. The
     * metadata of each result must be an image (if not, an exception is thrown)
     * @param imageViewSize view size for the images showed in this panel.
     */
    public ImageListPanel(List<ResultMetadata> list, Dimension imageViewSize) {
        initComponents();
        internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        internalPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                internalPanelMouseClicked(evt);
            }
        });
               
        scrollPanel.setViewportView(internalPanel);
        this.imageViewSize = imageViewSize;
        this.setPreferredSize(new Dimension(imageViewSize.width*5,imageViewSize.height+30));
        if (list!=null){ 
            add(list);
        }
    }
    
    /**
     * Add a set of images to the list.
     * 
     * @param list list of {@link jmr.result.ResultMetadata} objetcs. The
     * metadata of each result must be an image (if not, an exception is thrown)
     */
    public void add(List<ResultMetadata> list) {  
        for (ResultMetadata r : list) {
            String label =  r.getResult().toString(); 
            this.add((BufferedImage)r.getMetadata(), label);           
        }
    }
    
    /**
     * Add a set of images to the list.
     * 
     * @param list the list of images to be added
     */
    public void add(Collection<BufferedImage> list) {  
        for (BufferedImage image : list) {
            this.add(image,null,false);           
        }
    }
    
    /**
     * Add a new image to the list with a tip label. By means the parameter 
     * <code>originalSize</code>, the original size or the default one can be
     * selected.
     * 
     * @param image the image to be added
     * @param label the tip label associated to the image
     * @param originalSize if <tt>true</tt>, the original image size is used
     */
    public void add(BufferedImage image, String label, boolean originalSize){
        ImagePanel imgPanel = new ImagePanel(image,label);
        if(!originalSize) imgPanel.setPreferredSize(imageViewSize);
        internalPanel.add(imgPanel);
    }
    
    /**
     * Add a new image to the list with a tip label and using the default size 
     * {@link #DEFAULT_IMAGE_VIEW_SIZE}.
     * 
     * @param image the image to be added
     * @param label the tip label associated to the image
     */
    public void add(BufferedImage image, String label){
        this.add(image,label,false);
    }
    
    /**
     * Add a new image to the list without tip label and using the default size 
     * {@link #DEFAULT_IMAGE_VIEW_SIZE}.
     * 
     * @param image the image to be added
     */
    public void add(BufferedImage image){
        this.add(image,null,false);
    }
    
    /**
     * Add a new image to the list at the given position with a tip label. By
     * means the parameter <code>originalSize</code>, the original size or the
     * default one can be selected.
     *
     * @param image the image to be added
     * @param index the position at which to insert the component, or
     * <code>-1</code> to append the component to the end
     * @param label the tip label associated to the image
     * @param originalSize if <tt>true</tt>, the original image size is used
     */
    public void add(BufferedImage image, int index, String label, boolean originalSize){
        ImagePanel imgPanel = new ImagePanel(image,label);
        if(!originalSize) imgPanel.setPreferredSize(imageViewSize);
        internalPanel.add(imgPanel, index);
    }
    
    /**
     * Add a new image to the list at the given position with a tip label and
     * using the default size {@link #DEFAULT_IMAGE_VIEW_SIZE}.
     *
     * @param image the image to be added
     * @param index the position at which to insert the component, or
     * <code>-1</code> to append the component to the end
     * @param label the tip label associated to the image
     */
    public void add(BufferedImage image, int index, String label) {
        this.add(image, index, label, false);
    }

    /**
     * Add a new image to the list at the given position without tip label and
     * using the default size {@link #DEFAULT_IMAGE_VIEW_SIZE}.
     *
     * @param image the image to be added
     * @param index the position at which to insert the component, or
     * <code>-1</code> to append the component to the end
     */
    public void add(BufferedImage image, int index) {
        this.add(image, index, null, false);
    }
    
    /**
     * Removes the n-th image of this panel.
     * 
     * @param index index of the image to be removed.
     * @exception  ArrayIndexOutOfBoundsException if the n<sup>th</sup> image 
     * does not exist.
     */
    public void removeImage(int index){
        internalPanel.remove(index);
    }
    
    /**
     * Returns the n-th image of this panel.
     * 
     * @param index index of the element to return.
     * @return the n-th image of this panel
     * @exception  ArrayIndexOutOfBoundsException if the n<sup>th</sup> image 
     * does not exist.
     */
    public BufferedImage getImage(int index){
        ImagePanel img_panel = (ImagePanel)internalPanel.getComponent(index);
        return img_panel.getImage();
    }
    
    /**
     * Returns the list of images of this panel.
     * 
     * @return the list of images of this panel.
     */
    public List<BufferedImage> getAllImages(){
        ArrayList<BufferedImage> output = new ArrayList();
        for(int i=0; i<getNumberOfImages(); i++)
            output.add(getImage(i));
        return output;
    }
    
    /**
     * Returns the number of images in this panel.
     * 
     * @return the number of images in this panel.
     */
    public int getNumberOfImages(){
        return internalPanel.getComponentCount();
    }
    
    /**
     * Set the view size for the images showed in this panel.
     *
     * @param common_imagesize the common size for the images showed in this
     * panel.
     */
    public void setImageViewSize(Dimension common_imagesize) {
        this.imageViewSize = common_imagesize;
    }

    /**
     * Set the view size for the images showed in this panel.
     *
     * @param width
     * @param height
     */
    public void setImageViewSize(int width, int height) {
        this.setImageViewSize(new Dimension(width,height));
    }
    
    /**
     * Returns the view size of this panel.
     * 
     * @return the view size of this panel.
     */
    public Dimension getImageViewSize() {
        return imageViewSize;
    }
    
    /**
     * Adds the specified image selection listener to receive image selection
     * events from this panel.
     *
     * @param listener the image selection listener
     */
    public void addImageSelectionListener(ImageSelectionListener listener) {
        if (listener != null) {
            this.imageSelectionEventListener = listener;
        }
    }

    /**
     * Notify to the image selection listener a new image selection event.
     * 
     * @param evt the image selection event.
     */
    private void notifyImageSelectionEvent(ImageSelectionEvent evt){
        if(imageSelectionEventListener!=null){
            imageSelectionEventListener.imageSelected(evt);
        }
    }
    
    /**
     * Method for handling the mouse clicked event on this panel. If a double
     * click is made on an image, the image selection event will be launched.
     * 
     * @param evt mouse event.
     */
    private void internalPanelMouseClicked(java.awt.event.MouseEvent evt) {                                         
        if (evt.getClickCount() == 2) {
            try {
                ImagePanel imgPanel = (ImagePanel)internalPanel.getComponentAt(evt.getPoint());
                if (imgPanel != null) {
                    notifyImageSelectionEvent(new ImageSelectionEvent(
                            imgPanel.getImage(),
                            imgPanel.getLabel()));
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPanel = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(scrollPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Inner class representing a image selection event. It will be launched if
     * a double click is made on the image.
     */
    public class ImageSelectionEvent extends java.util.EventObject {
        private final String label;
        public ImageSelectionEvent(BufferedImage source, String label) {
            super(source);
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }
    
    /**
     * Inner class representing a image selection event listener.
     */
    public interface ImageSelectionListener extends java.util.EventListener {
        public void imageSelected(ImageSelectionEvent evt);
    }

}
