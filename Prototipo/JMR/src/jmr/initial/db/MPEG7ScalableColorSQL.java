package jmr.initial.db;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jmr.initial.db.mySQL;
import jmr.media.JMRExtendedBufferedImage;
import jmr.initial.db.Descriptor2MySQL;

/**
 * <p>Title: JMR Project</p>
 * <p>Description: Java Multimedia Retrieval API</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: University of Granada</p>
 * @author Jesus Chamorro Martinez
 * @version 1.0
 */

public class MPEG7ScalableColorSQL extends jmr.descriptor.color.MPEG7ScalableColor implements Descriptor2MySQL{

  /**
   * Version of the descriptor:
   * If the compute method is recall and version is newer the FeatureVec is override
   */
  private float version = 1.0F;

//  /**
//   * Constructor initiating the descriptor without computing the feature vector from an image
//   * @param numC  Number of coeefficient in the histogram : 32,64,128,256
//   * @param numB  Number of bitplanes discarded in the histograms response
//   */
//  public MPEG7ScalableColorSQL(int numC, int numB) {
//    super(numC, numB);
//  }
//
//  /**
//   *  Constructor with default value {@link #nofCoefficients}=256 and {@link #nofBitPlanesDiscarded}=0
//   * */
//  public MPEG7ScalableColorSQL() {
//    super();
//  }

  /**
   * Constructor initiating the descriptor and computing the resulting feature vector from an image
   * @param im Input image
   * @param numC Number of coeefficient in the histogram : 32,64,128,256
   * @param numB Number of bitplanes discarded in the histograms response
   * (Actually this is not implemented for this Java code, but its emulated to follow MPEG7 standard)
   */
  public MPEG7ScalableColorSQL(JMRExtendedBufferedImage im, int numC, int numB) {
    super(im, numC, numB);
  }

  /**
   *  Construct the object with default values computing the resulting feature vector from an image
   * @param im Input image
   * */
  public MPEG7ScalableColorSQL(JMRExtendedBufferedImage im) {
    super(im);
  }

  /**
   * It permits to overwrite previous values computed with this algorithm if it has changed
   * @return the ID  number has a float with the version of the algorithm used.
   */
  public float getVersion() {
    return this.version;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#createTable()
   */
  public String createTable() {
    String str = "CREATE TABLE " + getTableName() + " ( \n";
    String[] colName = getSQLParamNames();
    str += colName[0] + " INT( 10 ) NOT NULL , \n";
    str += colName[1] + " FLOAT NOT NULL DEFAULT '1.0', \n";
    str += colName[2] + " SMALLINT UNSIGNED NOT NULL DEFAULT '256', \n";
    str += colName[3] + " TINYINT( 1 ) UNSIGNED NOT NULL DEFAULT '0', \n";
    str += colName[4] + " VARBINARY( 256 ) NOT NULL, \n";
    str += colName[5] + " BINARY( 32 ) NOT NULL, \n";
    str += "PRIMARY KEY ( `Photo_ID` )\n" +
        ") ENGINE = MYISAM CHARACTER SET utf8 COLLATE utf8_general_ci";

    return str;
  }

  //TODO: Version and Photo ID are not parameters.
  public String[] getSQLParamNames() {
    String str[] = new String[6];
    str[0] = "Photo_ID";
    str[1] = "Version";
    str[2] = "nofCoefficients";
    str[3] = "nofBitPlanesDiscarded";
    str[4] = "histoMagnitud";
    str[5] = "histoSign";
    return str;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#getTableName()
   */
  public String getTableName() {
    return "`DescMPEG7_SCD`";
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(int, es.ugr.siar.db.mySQL)
   */
  public void fromMySQL(int ID, mySQL db) {
    String[] paramName = getSQLParamNames();
    String sql = " SELECT * FROM " + getTableName();

    sql += "WHERE `Photo_ID`=" + ID + ";";
    if (db.queryOneRowResult(sql)) {
      nofCoefficients = db.getValueInt(paramName[2]);
      nofBitPlanesDiscarded = db.getValueInt(paramName[3]);
      byte[] magn = db.getBytes(paramName[4]);
      byte[] sign = db.getBytes(paramName[5]);
      setHistoHaar(magn, sign);
    }

  }

  /*
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(java.sql.ResultSet)
   */
  public void fromMySQL(ResultSet result) {
    String[] paramName = getSQLParamNames();
    try {
      result.next();
      nofCoefficients = result.getInt(paramName[2]);
      nofBitPlanesDiscarded = result.getInt(paramName[3]);
      byte[] magn = result.getBytes(paramName[4]);
      byte[] sign = result.getBytes(paramName[5]);
      setHistoHaar(magn, sign);
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
        pstmt.setInt(3, nofCoefficients);
        pstmt.setInt(4, nofBitPlanesDiscarded);
        pstmt.setBinaryStream(5, new ByteArrayInputStream(getBitPlane()), 256);
        pstmt.setBinaryStream(6, new ByteArrayInputStream(getCoefficientSigns()), 32);
        pstmt.executeUpdate();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      //db.queryUpdate(sql);
    }

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
