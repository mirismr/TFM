package jmr.initial.db;

import jmr.initial.db.mySQL;
import java.sql.ResultSet;
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

public class MPEG7HomogeneousTextureSQL extends jmr.initial.descriptor.mpeg7.MPEG7HomogeneousTexture implements Descriptor2MySQL {

  /**
   * Version of the descriptor:
   * If the compute method is recall and version is newer the FeatureVec is override
   */
  private float version = 1.0F;

  /**
   * Default constructor.
   *
   */
  public MPEG7HomogeneousTextureSQL() {
    super();
  }

  /**
   * Constructs a <code>MPEG7HomogeneousTextureSQL</code> objetc
   * @param 	nofScale	Number of Scale in which we divide our frequency 2D space ({@link #nofScale}).
   * @param	nofOrient	Number of orientation in which we divide our frequency space. ({@link #nofOrient}).
   * @param 	toNormalize If we normalize the value in each sector or not ({@link #toNormalize}).
   *
   */
  public MPEG7HomogeneousTextureSQL(int nofScale, int nofOrient, boolean toNormalize) {
    super(nofScale, nofOrient, toNormalize);
  }

  /**
   * Constructor using an image and default parameters.
   * @param im	The image
   */
  public MPEG7HomogeneousTextureSQL(JMRExtendedBufferedImage im) {
    super(im);
  }

  /** Constructs a <code>MPEG7HomogeneousTextureSQL</code> objetc computing the descripor for an image
   * @param 	im		The image
   * @param 	nofScale	Number of scales.
   * @param 	nofOrient	Number of orientations.
   * @param 	toNormalize	If we normalize or not.
   */
  public MPEG7HomogeneousTextureSQL(JMRExtendedBufferedImage im, int nofScale, int nofOrient, boolean toNormalize) {
    super(im, nofScale, nofOrient, toNormalize);
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
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(int, es.ugr.siar.db.mySQL)
   */
  public void fromMySQL(int ID, mySQL db) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#getSQLParamNames()
   */
  public String[] getSQLParamNames() {
    String str[] = new String[6];
    str[0] = "Photo_ID";
    str[1] = "Version";
    str[2] = "nofScale";
    str[3] = "nofOrient";
    str[5] = "histoFreq";
    return str;
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#getTableName()
   */
  public String getTableName() {
    return "`DescMPEG7_HTD`";
  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#toMySQL(int, es.ugr.siar.db.mySQL)
   */
  public void toMySQL(int ID, mySQL db) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see es.ugr.siar.ip.desc.Descriptor2MySQL#fromMySQL(java.sql.ResultSet)
   */
  public void fromMySQL(ResultSet result) {
    // TODO Auto-generated method stub

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
