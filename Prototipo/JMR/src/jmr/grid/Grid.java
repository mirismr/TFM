package jmr.grid;

/**
 * Class representing a grid (in an n-dimensional space) over a media.
 * 
 * @param <T> the type of the media associated to this grid and, consequently, 
 * the type of the tile.
 * 
 * @author Jesús Chamorro Martínez (jesus@decsai.ugr.es)
 */
public interface Grid<T> {
    /**
     * Returns the number of tiles in this grid.
     * 
     * @return the number of tiles in this grid
     */
    public int getNumTiles();
    
    /**
     * Returns the tile at the given index. 
     * 
     * @param index index ot the tile with 0 &lt;= index &lt; {@link #getNumTiles()}
     * @return the tile at the given index.
     */
    public T getTile(int index);
    
    /**
     * Returns the media associated to this grid.
     * 
     * @return the media associated to this grid
     */
    public T getSource();
}
