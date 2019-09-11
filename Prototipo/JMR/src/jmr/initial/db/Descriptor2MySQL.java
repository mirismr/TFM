package jmr.initial.db;

import java.sql.ResultSet;

/**
 * Interface to use MySQL and  VisualDescriptor.
 *
 * This is an interface with all the function that need to be implemented to correctly
 * load or save all type of  VisualDescriptor to the MySQL Database.
 *
 *
 * @author  RAT Benoit 
 * (<a href="http://ivrg.epfl.ch" target="about_blank">IVRG-LCAV-EPFL</a> 
 *  <a href="http://decsai.ugr.es/vip" target="about_blank">VIP-DECSAI-UGR</a>)
 * @version 1.0
 * @since 15 dec. 07
 *
 *
 */
public interface Descriptor2MySQL {

  /**
   * Give the name of the MySQL table for this descriptor
   * @return the MySQL tablename
   */
  public String getTableName();

  /**
   * Return the name of all fields (parameters/attribute) for each descriptors
   * @return An array of <code>String</code> containing all the fields.
   */
  public String[] getSQLParamNames();

  /**
   * Return the SQL Syntax to create an SQL table for the specific descriptors.
   * @return the MySQL syntax to create a Descriptor Table.
   */
  public String createTable();

  /**
   * Return the MySQL syntax for when we want to do a <code>REPLACE</code>
   * @return the MySQL syntax for REPLACE operation.
   */
  public String replaceHeader();

  /**
   * Find if the descriptors for a specific image is in the database
   * <p>This method can also check if it is an old version of this descriptor or not.</p>
   * @param 	ID				the Photo_ID of the image.
   * @param 	db				The {@link mySQL} database previously connected.
   * @param 	checkVersion	a boolean to know if we want also to check the version of the descriptor or not.
   * @return					true if it exist (with actual version), false otherwise.
   */
  public boolean exist(int ID, mySQL db, boolean checkVersion);

  /**
   * Send a descriptor computed for the image ID to the MySQL database.
   * @param 	ID	The Photo_ID of the image.
   * @param 	db	The {@link mySQL} database previously connected.
   */
  public void toMySQL(int ID, mySQL db);

  /**
   * Recover the descriptor from the MySQL to set the  VisualDescriptor object.
   * @param 	ID	The Photo_ID of the image.
   * @param 	db	The {@link mySQL} database previously connected.
   */
  public void fromMySQL(int ID, mySQL db);

  /**
   * Set the  VisualDescriptor using the next row (<code>n</code>)in the {@link ResultSet}.
   * <p> We start by the result.next() method which mean we need to use the while
   * loop (not the do..while).</p>
   * @param result A result at the position <code>n-1</code>.
   */
  public void fromMySQL(ResultSet result);

}
