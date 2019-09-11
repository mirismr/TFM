package jmr.region;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * A class representing an image region.
 * 
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */public class Region {
    /**
     * Source image.
     */ 
    private BufferedImage source;
    /**
     * The shape of the region
     */
    private Shape shape = null;
    
    /**
     * Constructs a new region using by default a rectangular shape of size the 
     * image size. 
     * 
     * @param image the source image.
     */
    public Region(BufferedImage image){
        this.source = image;
        if(image!=null){
            shape = new Rectangle(image.getWidth(),image.getHeight());
        }
    }
    
    /**
     * Constructs a new region.
     * 
     * @param image the source image.
     * @param shape the shape of the region
     */
    public Region(BufferedImage image, Shape shape){
        this.source = image;
        this.shape = shape;        
    }

    /**
     * Returns the image associated to this region.
     * 
     * @return the image associated to this region.
     */
    public BufferedImage getSource() {
        return source; 
    }

    /**
     * Returns the shape of this region.
     * 
     * @return the shape of this region.
     */
    public Shape getShape() {
        return shape;
    }
    
    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this
     * region, as described by the
     *  <a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * definition of insideness</a>.
     *
     * @param p the specified <code>Point2D</code> to be tested
     * @return <code>true</code> if the specified <code>Point2D</code> is inside
     * the boundary of the region; <code>false</code> otherwise.
     */
    public boolean contains(Point2D p) {
        return shape.contains(p);
    }
    
    /**
     * Tests if a specified {@link Point2D} is inside the boundary of this
     * region, as described by the
     *  <a href="{@docRoot}/java/awt/Shape.html#def_insideness">
     * definition of insideness</a>.
     *
     * @param x the X coordinate of the pixel (in relation to the image origin).
     * @param y the Y coordinate of the pixel (in relation to the image origin).
     * @return <code>true</code> if the specified <code>Point2D</code> is inside
     * the boundary of the region; <code>false</code> otherwise.
     */
    public boolean contains(int x, int y) {
        return shape.contains(x,y);
    }
    
    /**
     * Returns the RGB color of the specified pixel inside the region.
     *
     * @param x the X coordinate of the pixel (in relation to the image origin).
     * @param y the Y coordinate of the pixel (in relation to the image origin).
     * @return the RGB color of the specified pixel; <code>null</code> if the
     * pixel is not inside the region.
     */
    public Color getRGB(int x, int y) {
        if (!shape.contains(x,y)) {
            return null;
        }
        return new Color(source.getRGB(x, y));
    }
    
    /**
     * Returns the width of the region bounds.
     * 
     * @return the width of the region bounds.
     */
    public int getWidth() {
        return shape.getBounds().width;
    }
    
    /**
     * Returns the height of the region bounds.
     * 
     * @return the height of the region bounds.
     */
    public int getHeight() {
        return shape.getBounds().height;
    }
    
    /**
     * Returns the location (upper-left corner) of the rectangle associated to 
     * the region bounds.
     * 
     * @return the location of the region bounds.
     */
    public Point getLocation() {
        return shape.getBounds().getLocation();
    }
    
    /**
     * Creates an image with the pixels inside this region.
     * 
     * @return an image with the pixels inside this region.
     */
    public BufferedImage createImage(){
        BufferedImage output = new BufferedImage(getWidth(),getHeight(), BufferedImage.TYPE_INT_ARGB);
        RegionIterator.Pixel it = new RegionIterator.Pixel(this);
        Point location = this.getLocation();
        Color color;

        while(it.hasNext()){
            color = it.next();
            output.setRGB(it.getX()-location.x, it.getY()-location.y, color.getRGB());   
        }
        return output;
    }
}
