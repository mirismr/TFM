package jmr.initial.db;

import jmr.initial.descriptor.mpeg7.MPEG7EdgeHistogram;
import jmr.initial.db.mySQL;
import java.sql.ResultSet;
import jmr.media.JMRExtendedBufferedImage;
import jmr.initial.db.Descriptor2MySQL;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.ByteArrayInputStream;
import jmr.initial.descriptor.mpeg7.MPEG7EdgeHistogram;

/**
 * <p>Title: JMR Project</p>
 * <p>Description: Java Multimedia Retrieval API</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: University of Granada</p>
 * @author Jesus Chamorro Martinez
 * @version 1.0
 */

public class MPEG7EdgeHistogramSQL extends MPEG7EdgeHistogram implements Descriptor2MySQL {
  /**
   * Version of the descriptor:
   * If the compute method is recall and version is newer the FeatureVec is override
   */
  private float version = 1.0F;

  /**
   * Default constructor without parameters.
   * 
   */
  public MPEG7EdgeHistogramSQL() {
    super();
  }

  /**
   * Constructor
   *  @param threshold Threshold
   *  @param numBlock Number of blocks
   */
  public MPEG7EdgeHistogramSQL(int threshold, int numBlock) {
    super(threshold,numBlock);
  }

  /**
   * Constructs the object for an image using the default parameters.
   * @param im	The image
   */
  public MPEG7EdgeHistogramSQL(JMRExtendedBufferedImage im) {
    super(im);
  }

  /**
   * Constructs the object for an image using the specified parameters.
   * @param im	The image
   * @param numBlock Number of blocks
   */
  public MPEG7EdgeHistogramSQL(JMRExtendedBufferedImage im, int treshold, int numBlock) {
    super(im,treshold, numBlock);
  }

  /**
   * It permits to overwrite previous values computed with this algorithm if it has changed
   * @return the ID  number has a float with the version of the algorithm used.
   */
  public float getVersion() {
    return this.version;
  }

  /* (non-Javadoc)
   */
  public String getTableName() {
    return "`DescMPEG7_EHD`";
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#createTable()
   */
  public String createTable() {
    String str = "CREATE TABLE " + getTableName() + " ( \n";
    String[] paramName = getSQLParamNames();
    str += "`" + paramName[0] + "` INT( 10 ) NOT NULL , \n";
    str += "`" + paramName[1] + "` FLOAT NOT NULL DEFAULT '1.0', \n";
    str += "`" + paramName[2] +
        "` SMALLINT UNSIGNED NOT NULL DEFAULT '256', \n";
    str += "`" + paramName[3] + "` INT(10) NOT NULL DEFAULT '1100', \n";
    str += "`" + paramName[4] + "` BINARY( 80 ) NULL, \n";
    str += "PRIMARY KEY ( `" + paramName[0] + "` )\n" +
        ") ENGINE = MYISAM CHARACTER SET utf8 COLLATE utf8_general_ci";
    return str;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(int, es.ugr.siar.db.mySQL)
   */
  public void fromMySQL(int ID, mySQL db) {
    String[] paramName = getSQLParamNames();
    String sql = " SELECT * FROM " + getTableName();
    sql += "WHERE `Photo_ID`=" + ID + ";";
    if (db.queryOneRowResult(sql)) {
      treshold = db.getValueInt(paramName[2]);
      numBlock = db.getValueInt(paramName[3]);
      setHisto(db.getBytes(paramName[4]));
    }
  }

  /*
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(java.sql.ResultSet)
   */
  public void fromMySQL(ResultSet result) {
    String[] paramName = getSQLParamNames();
    try {
      result.next();
      treshold = result.getInt(paramName[2]);
      numBlock = result.getInt(paramName[3]);
      setHisto(result.getBytes(paramName[4]));
    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#toMySQL(int, es.ugr.siar.db.mySQL)
   */
  public void toMySQL(int ID, mySQL db) {

    if ( (exist(ID, db, false) && !exist(ID, db, true)) || !exist(ID, db, false)) { //If the ID exist in the database.
      //Obtain the INSERT header
      String sql = replaceHeader();

      PreparedStatement pstmt = db.getPreparedStatement(sql);
      try {
        pstmt.setInt(1, ID);
        pstmt.setFloat(2, version);
        pstmt.setInt(3, treshold);
        pstmt.setInt(4, numBlock);
        pstmt.setBinaryStream(5, new ByteArrayInputStream(getByteHisto()), 80);
        pstmt.executeUpdate();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#getSQLParamNames()
   */
  public String[] getSQLParamNames() {
    String str[] = new String[5];
    str[0] = "Photo_ID";
    str[1] = "Version";
    str[2] = "treshold";
    str[3] = "numBlock";
    str[4] = "histo";
    return str;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#exist(int, es.ugr.siar.db.mySQL, boolean)
   */
  public boolean exist(int ID, mySQL db, boolean checkVersion) {
    boolean isOK = false;
    String sql = "SELECT `Photo_ID`, `Version` FROM " + getTableName() + " ";
    sql += "WHERE `Photo_ID` =" + ID;
    if (db.queryOneRowResult(sql)) {
      //Useless check but we do it...
      if (ID == db.getValueInt("Photo_ID")) {
        //Check also the version of this descriptors.
        if (!checkVersion) {
          isOK = true;
        }
        else if (version <= (float) db.getValueDouble("Version")) {
          isOK = true;
          System.out.println(ID + " exist in database " + db.toString());
        }
      }
    }
    //Close the result set for the next query.
    db.closeResultSet();
    return isOK;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#replaceHeader()
   */
  public String replaceHeader() {
    String sql1 = "", sql2 = "", coma = "";
    String[] paramName = getSQLParamNames();
    for (int i = 0; i < paramName.length; i++) {
      if (i != 0) {
        coma = ",";
      }
      sql1 += coma + "`" + paramName[i] + "`";
      sql2 += coma + "?";
    }
    return "REPLACE INTO " + getTableName() + " (" + sql1 + ") VALUES (" + sql2 +
        ");";
  }

}
