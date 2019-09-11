package jmr.grid;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Class representing a grid formed by tiling the plane regularly with squares.
 * 
 * @param <T> the type of the media associated to this grid.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public class SquareGrid<T extends BufferedImage> implements Grid<T>, Serializable{
    /**
     * The source image associated to this grid.
     */
    private transient T source;
    /**
     * The width of the grid, understood as the number of titles in the x-axis.
     */
    private int gridWidth;
    /**
     * The height of the grid, understood as the number of titles in the y-axis.
     */
    private int gridHeight;
    /**
     * The width of the tile in pixels.
     */
    private int tileWidth;
    /**
     * The height of the tile in pixels.
     */
    private int tileHeight;
    
    /**
     * Constructs a new square grid associated to the given image.
     * 
     * @param image the image associated to this grid.
     * @param gridWidth the width of the grid (number of titles in the x-axis).
     * @param gridHeight the height of the grid (number of titles in the y-axis).
     */
    public SquareGrid(T image, int gridWidth, int gridHeight){
        this.source = image;
        this.setGridSize(gridWidth, gridHeight);
    }
    
    /**
     * Constructs a new square grid associated to the given image.
     * 
     * @param image the image associated to this grid
     * @param gridSize the size of the grid, understood as the number of titles 
     * in the x and y axis.
     */
    public SquareGrid(T image, Dimension gridSize){
        this(image,gridSize.width,gridSize.height);
    }
    
    /**
     * Set the size of this grid. The tile size is automatically calculated.
     * 
     * @param gridWidth the width of the grid (number of titles in the x-axis).
     * @param gridHeight the height of the grid (number of titles in the y-axis).
     */
    public final void setGridSize(int gridWidth, int gridHeight){
        this.gridWidth = gridWidth>0 ? gridWidth : 1;
        this.gridHeight = gridHeight>0 ? gridHeight : 1;
        // The tile size is calculated on the basis of the grid size 
        this.tileWidth = source.getWidth() / this.gridWidth;
        this.tileHeight = source.getHeight() / this.gridHeight;
    }
    
    /**
     * Set the size of the tile. The grid size is automatically calculated.
     * 
     * @param tileWidth the width of the tile in pixels.
     * @param tileHeight the height of the tile in pixels.
     */
    public void setTileSize(int tileWidth, int tileHeight){
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        // The grid size is calculated on the basis of the tile size 
        this.gridWidth =  (int)((double)source.getWidth()/tileWidth+0.5);
        this.gridHeight =  (int)((double)source.getHeight()/+tileHeight+0.5);        
    } 
    
    /**
     * Set the image associated to this grid. The grid size is kept and the tile 
     * size is automatically recalculated taking into account the image dimension.
     * 
     * @param image the image associated to this grid.
     */
    public void setSource(T image){
        this.source = image;
        //We keep the grid size, but the tile size could change
        this.tileWidth = source.getWidth() / this.gridWidth;
        this.tileHeight = source.getHeight() / this.gridHeight;
    }
    
    /**
     * Returns the image associated to this grid.
     * 
     * @return the image associated to this grid
     */
    @Override
    public T getSource(){
        return source;
    }
    
    /**
     * Returns the tile at the given coordinate.
     * 
     * @param tileX x coordinate of the tile
     * @param tileY y coordinate of the tile
     * @return the tile at the given coordinate
     */
    public T getTile(int tileX, int tileY){
        int x=tileX*tileWidth;
        int y=tileY*tileHeight;
        int width = Math.min(tileWidth,source.getWidth()-x);
        int height = Math.min(tileHeight,source.getHeight()-y);
        BufferedImage subimage = source.getSubimage(x, y, width, height); 
        return (T)subimage;
    }
    
    /**
     * Returns the tile at the given index. We assume that the indexes are
     * ordered following the sequence (x,y): (0,0),(0,1),...,(1,0),(1,1),...
     * 
     * @param index index ot the tile with 0 &lt;= index &lt; {@link #getNumTiles()}
     * @return the tile at the given index.
     */
    @Override
    public T getTile(int index){
        int tileX = index/gridHeight;
        int tileY = index%gridHeight;        
        return getTile(tileX,tileY);
    }
    
    /**
     * Returns the width of this grid, understood as the number of titles in the 
     * x-axis.
     * 
     * @return the width of this grid
     */
    public int getGridWidth(){
        return gridWidth;
    }
    
    /**
     * Returns the height of this grid, understood as the number of titles in the 
     * y-axis.
     * 
     * @return the height of this grid
     */
    public int getGridHeight(){
        return gridHeight;
    }
    
    /**
     * Returns the number of tiles in this grid.
     * 
     * @return the number of tiles in this grid
     */
    @Override
    public int getNumTiles(){
        return gridWidth*gridHeight;
    }
    
    /**
     * Returns the width of the tile in pixels.
     * 
     * @return the width of the tile
     */
    public int getTileWidth(){
        return tileWidth;
        
    }
    
    /**
     * Returns the height of the tile in pixels.
     * 
     * @return the height of the tile
     */
    public int getTileHeight(){
        return tileHeight;
        
    }
}
