package jmr.descriptor;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import jmr.grid.Grid;
import jmr.grid.SquareGrid;

/**
 * Class representing a list of descriptors (one for each tile) associated to a 
 * gridded media. 
 * 
 * @param <T> the type of the media associated to this grid-based descriptor.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class GriddedDescriptor<T> extends MediaDescriptorAdapter<T> implements Serializable{    
    /**
     * Grid associated to this descriptor
     */
    private Grid<T> grid;
    
    /**
     * List of descriptors
     */
    private ArrayList<MediaDescriptor<T>> descriptors;

    /**
     * The descriptor class for each tile
     */
    private Class<? extends MediaDescriptor> tileDescriptorClass;
    
    /**
     * Comparator used by default.
     */
    static private Comparator DEFAULT_COMPARATOR = new DefaultComparator();
    
    /**
     * Default grid size.
     */
    private static Dimension DEFAULT_GRID_SIZE = new Dimension(2, 2);
    
    /**
     * Default descriptor class for each tile.
     */
    private static Class DEFAULT_TILE_DESCRIPTOR_CLASS
            = jmr.descriptor.color.MPEG7ScalableColor.class;

    /**
     * Constructs a new descriptor using the given grid (and its media) where
     * each tile is described by means of a descriptor of the class 
     * <code>descriptorClass</code>.
     * 
     * The class <code>descriptorClass</code> have to provide, at least, a 
     * constructor with a single parameter of type <code>T</code>.
     * 
     * @param grid the grid associated to this descriptor
     * @param tileDescriptorClass the descriptor class for each tile
     */
    public GriddedDescriptor(Grid<T> grid, Class<? extends MediaDescriptor> tileDescriptorClass) {
        super((T)grid.getSource(), DEFAULT_COMPARATOR);
        // The previous call does not initialize the tile descriptors. It will 
        // be done in the following setTilesDescriptors() call
        this.grid = grid;
        this.tileDescriptorClass = tileDescriptorClass;
        this.setTilesDescriptors(tileDescriptorClass);
    }
    //Revisar: llamadas a set en código anterior
    
    /**
     * Constructs a new grid descriptor for the particular case of an image (as
     * media) with a square grid.
     * 
     * @param image the source image associated to this descriptor
     * @param gridSize the size of the square grid, understood as the number of 
     * titles in the x and y axis.
     * @param descriptorClass the descriptor class for each tile. It have to 
     * to provide, at least, a constructor with a single parameter of type
     * <code>BufferedImage</code>. 
     */
    public GriddedDescriptor(BufferedImage image, Dimension gridSize, Class<? extends MediaDescriptor> descriptorClass) {               
        this(new SquareGrid(image, gridSize),descriptorClass);
    }
    
    /**
     * Constructs a new grid descriptor for the particular case of an image (as
     * media) with a square grid using the default grid size
     * {@link #DEFAULT_GRID_SIZE} and the default descriptor class for each tile
     * {@link #DEFAULT_CLASS_TILE_DESCRIPTOR}.
     *
     * @param image the source image associated to this descriptor.
     */
    public GriddedDescriptor(BufferedImage image) {
        this(image, DEFAULT_GRID_SIZE, DEFAULT_TILE_DESCRIPTOR_CLASS);
    }
    
    /**
     * First initialization of the descriptor as an empty list of descriptor.
     * 
     * Later, the list should be filled in with the descriptors of each tile 
     * (by calling {@link #setTilesDescriptors(java.lang.Class) }).
     *
     * @param media the media associated to this descriptor
     */
    @Override
    public void init(T media) {
        descriptors = new ArrayList<>();
        // We also should add to the list the tiles descriptors, but this method
        // is called from the superclass constructor so, when this code is
        // executed, the local member data (used for constructing the tiles 
        // descriptors) are no initialized yet. Thus, after the super() call 
        // in the construtor, we have to initialize the rest of the descriptor.
    }
    
    /**
     * Set the list of descriptor by calculating a descriptor for each tile.  
     *
     */
    private void setTilesDescriptors(Class descriptorClass) {
        T tile;
        MediaDescriptor descriptor;
        if(!descriptors.isEmpty()){
            descriptors.clear();
        }
        for (int i = 0; i < grid.getNumTiles(); i++) {
            tile = (T)grid.getTile(i);            
            descriptor = MediaDescriptorFactory.getInstance(descriptorClass, tile);
            descriptors.add(descriptor);
        }
    }
    
    /**
     * Returns the grid associated to this descriptor.
     * 
     * @return the grid associated to this descriptor
     */
    public Grid getGrid(){
        return grid;
    }
    
    /**
     * Set the grid associated to this descriptor. It implies the source media
     * and tile descriptors update.
     * 
     * @param grid the new grid associated to this descriptor
     */
    public void setGrid(Grid<T> grid){
        this.grid = grid;
        this.setSource(grid.getSource());
        this.setTilesDescriptors(tileDescriptorClass);
    }
    
    /**
     * Returns the tile descriptor class.
     * 
     * @return the tile descriptor class
     */
    public Class getTileDescriptorClass(){
        return this.tileDescriptorClass;
    }
    
    /**
     * Set the tile descriptor class. It implies the tile descriptors update.
     * 
     * @param tileDescriptorClass the new tile descriptor class. It have to 
     * to provide, at least, a constructor with a single parameter of type
     * <code>T</code>. 
     */
    public void setTileDescriptorClass(Class tileDescriptorClass){
        this.tileDescriptorClass = tileDescriptorClass;
        this.setTilesDescriptors(tileDescriptorClass);
    }
    
    /**
     * Returns the descriptor of the index-th tile.
     * 
     * @param index index ot the tile}
     * @return the descriptor of the index-th tile.
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public MediaDescriptor<T> getTileDescriptor(int index){
        return descriptors.get(index);
    }
    
    /**
     * Returns the number of tiles in this grid descriptor.
     * 
     * @return the number of tiles in this grid descriptor
     */
    public int getNumTiles(){
        return grid.getNumTiles();
    }
    
    /**
     * Set the default grid size for this class. This grid size is used when a
     * specific one is not provided in the object construction.
     *
     * @param gridSize the new grid dimension. 
     */
    static public void setDefaultGridSize(Dimension gridSize) {
        if (gridSize != null) {
            DEFAULT_GRID_SIZE = gridSize;
        }
    }
    
    /**
     * Returns the default grid size for this class. This grid size is used when 
     * a specific one is not provided in the object construction.
     */
    static public Dimension getDefaultGridSize() {
        return DEFAULT_GRID_SIZE;
    }
    
    /**
     * Set the default descriptor class for each tile. This classe is used when
     * a specific one is not provided in the object construction.
     *
     * @param descriptorClass the default descriptor class for each tile. It
     * have to provide, at least, a constructor with a single parameter of type
     * <code>BufferedImage</code>.
     */
    static public void setDefaultTileDescriptorClass(Class<? extends MediaDescriptor> descriptorClass) {
        if (descriptorClass != null) {
            DEFAULT_TILE_DESCRIPTOR_CLASS = descriptorClass;
        }
    }
    
    
    /**
     * Set the default comparator for this class. This comparator is used when a
     * specific one is not provided in the object construction.
     *
     * @param comparator the new comparator. If the given parameter is null, a
     * {@link #DEFAULT_COMPARATOR} comparator is assigned.
     */
    static public void setDefaultComparator(Comparator comparator) {
        DEFAULT_COMPARATOR = comparator != null ? comparator : new DefaultComparator();        
        // No null comparator is allowed. If the given parameter is null, the 
        // default one is used.
    }
    
    
    /**
     * Returns a string representation of this descriptor
     * .
     * @return a string representation of this descriptor 
     */
    @Override
    public String toString(){
        String output ="";
        for(MediaDescriptor descriptor : descriptors){
            output += descriptor.toString()+"\n";
        }
        return output;
    }

    /**
     * Functional (inner) class implementing a comparator between list descriptors
     */
    static class DefaultComparator implements Comparator<GriddedDescriptor, Double> {
        @Override
        public Double apply(GriddedDescriptor t, GriddedDescriptor u) {
            if(t.descriptors.size() != u.descriptors.size()){
                throw new InvalidParameterException("The descriptor lists must have the same size.");
            }
            Double item_distance, sum = 0.0;
            MediaDescriptor m1, m2;
            for(int i=0; i<t.descriptors.size(); i++){
                try{
                    m1 = (MediaDescriptor)t.descriptors.get(i);
                    m2 = (MediaDescriptor)u.descriptors.get(i);
                    item_distance = (Double)m1.compare(m2);
                    sum += item_distance*item_distance;
                }
                catch(ClassCastException e){
                    throw new InvalidParameterException("The comparision between descriptors is not interpetrable as a double value.");
                }
                catch(Exception e){
                    throw new InvalidParameterException("The descriptors are not comparables.");
                }                
            }
            return Math.sqrt(sum);
        }    
    }
    
}
