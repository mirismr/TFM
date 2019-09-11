package jmr.initial.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;

//import javax.swing.*;
//import com.mysql.jdbc.*;

/**
 * @author Jose Manuel Soto Hidalgo
 * @author  RAT Benoit 
 * (<a href="http://ivrg.epfl.ch" target="about_blank">IVRG-LCAV-EPFL</a> 
 *  <a href="http://decsai.ugr.es/vip" target="about_blank">VIP-DECSAI-UGR</a>)
 * @version 1.0
 * @since 14 déc. 07
 *
 */
public class mySQL  implements java.io.Serializable{
	/**     */
	private static final long serialVersionUID = -1852256537075893981L;
	private Connection conexion;
	private Statement stmt;
	private ResultSet result;

	private String host;
	private String user;
	private String pass;
	private String db;

	public String output;

	/**
	 * Constructor that create a connection on the select a database
	 * <ul>
	 * <li>host=localhost</li>
	 * <li>user=pdm</li>
	 * <li>pass=bnel7n</li>
	 * </ul>
	 *
	 * @param 	db 	name of the database
	 */

	public mySQL(String db) {
		this("localhost","pdm","bnel7n",db);
	}

	/** Constructor that create a connection on the a specific database on a mySQL server.
	 * @param host		hostname of the server typically (localhost)
	 * @param user		user for this database
	 * @param pass		password for this database
	 * @param db		The database name.
	 */
	public mySQL(String host,String user,String pass,String db) {
		this.host = host;
		this.user=user;
		this.pass=pass;
		this.db = db;
		connect("com.mysql.jdbc.Driver");
	}

	//------------------

	/**
	 * Metodo que devuelve un Objeto del tipo Connection
	 * @return La Conexion
	 */
	public Connection getConnection(){
		return conexion;
	}

	/**
	 * Connect to a specific database given a driver type. In this case a MySQL driver.
	 *
	 *
	 * @param driver
	 * @return a true value if the connection is done correctly, otherwise return false.
	 */
	private boolean connect(String driver){
		try{
			Class.forName(driver).newInstance();
			conexion = DriverManager.getConnection(
					"jdbc:mysql://"+host+"/"+db+"?user="+user+"&password="+pass);
			try{
				stmt = conexion.createStatement();
			}
			catch(SQLException ex){
				//JOptionPane.showMessageDialog(this,"No se pudo conectar a la BD: "+ex.getMessage().toString()+"", "Error...", JOptionPane.ERROR_MESSAGE);
				System.err.println("Can't connect to db "+ex.getMessage().toString());
				return false;
			}

		} catch(Exception e){
			System.err.println("Connection ratée: "+e);
			System.exit(-1);
		}
		return true;
	}

	/**
	 * Metodo que cierra una conexion abierta a una Base de Datos
	 */
	public void close(){
		try {
			//if(result != null) result.close();
			if(stmt != null) stmt.close();
			if(conexion != null) conexion.close();
		}
		catch(SQLException ex){
			output = "Error al cerrar la Base de Datos "+db;
		}
	}

	public boolean isConnected() {
		try {
			return !conexion.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	//------------------

	/**
	 * Metodo que obtiene el resulSet de una consulta
	 * @see #queryResult(String)
	 * @return ResultSet con la consulta o null si no se ha ejecutado la consulta o hubo error
	 */
	public ResultSet getResultSet(){
		return result;
	}

	public PreparedStatement getPreparedStatement(String sql) {
		try {
			return conexion.prepareStatement(sql);
		} catch (SQLException e) {
			errorManager("Prepapare Statement badly done", e);
		}
		return null;
	}

	/**
	 * Method that close a ResultSet previously performed
	 */
	public void closeResultSet(){
		try{
			result.close();
		}catch(SQLException e){
			errorManager("Can't close ResultSet", e);
		}
	}


	/**
	 * Perform a query on the MySQL database that perform the resultSet
	 * <p>
	 * The type of query is Select because it return a result, this method
	 * can perform query such insert or update
	 * </p>
	 *
	 * @param sql	The MySQL query.
	 * @return 		a true value if the query was done correctly.
	 */
	public boolean queryResult(String sql){
		try{
			result=stmt.executeQuery(sql);
		}
		catch (SQLException e){
			errorManager(sql,e);
			return false;
		}
		return true;
	}

	public boolean queryOneRowResult(String sql) {
		this.queryResult(sql);
		try {
			return (result.last());
		} catch (SQLException e) {
			errorManager("Go to last/unique row error", e);
		} //Even if it is the first row.
		return false;
	}

	/**
	 * Perform a query that modify the MySQL database and don't return any result.
	 * <p>
	 * The query type are : INSERT,UPDATE,DELETE,CREATE<br>
	 * The connection with the MySQL must be obtained previously. {@link #connect(String)}
	 * </p>
	 *
	 * @param sql	The MySQL query.
	 * @return 		a true value if the query was done correctly.
	 */
	public boolean queryUpdate(String sql){
		try{
			stmt.executeUpdate(sql);
		}
		catch (SQLException e){
			errorManager(sql,e);
			return false;
		}
		return true;
	}

	//------------------

	/**
	 * Method that read next register (Row) in ResultSet
	 * <p>We can use this function in a while loop:
	 * while(sql.nextRS()) { sql.getResultSet() }
	 * </p>
	 *
	 * @see #queryResult(String)
	 * @return True value if the operation was done correctly
	 */
	public boolean nextRow(){
		try {
			if (result.next()) return true;
		}
		catch (SQLException e) {
			return errorManager("Bad Next Result Operation",e);
		}
		return false;
	}

	/**
	 * Method that read previous register (Row) in ResultSet
	 *
	 * @see #queryResult(String)
	 * @return True value if the operation was done correctly
	 */
	public boolean previousRow(){
		boolean ret=false;
		try {
			if (result.previous())
				ret=true;
		}
		catch (SQLException e) {
			errorManager("getValue Error", e);
		}
		return ret;
	}

	//------------------

	/**
	 * get the value of a specific column/attribute of the actual row.
	 *
	 * @param 	attribute 	Name of the column of the attribute to read
	 * @see #queryResult(String)
	 * @see #nextRow()
	 * @see #previousRow()
	 * @return the result for this column in <b>String</b> format
	 */
	public String getValueS(String attribute){
		String ret=null;
		if(result == null)
			ret=null;
		else{
			try {
				ret= result.getString(attribute);
			}
			catch (Exception e) {
				errorManager("getValue Error", e);
			}
		}
		return ret;
	}

	/**
	 * get the value of a specific column/attribute of the actual row.
	 *
	 * @param 	attribute 	Name of the column of the attribute to read
	 * @see #queryResult(String)
	 * @see #nextRow()
	 * @see #previousRow()
	 * @return the result for this column in <b>Int</b> format
	 */
	public int getValueInt(String attribute){
		int ret=-1;
		if(result == null)
			ret= -1;
		else{
			try {
				ret= result.getInt(attribute);
			}
			catch (Exception e) {

				errorManager("Attribute:"+attribute+" error or bad Row Selection", e);
			}
		}
		return ret;
	}

	/**
	 * get the value of a specific column/attribute of the actual row.
	 *
	 * @param 	attribute 	Name of the column of the attribute to read
	 * @see #queryResult(String)
	 * @see #nextRow()
	 * @see #previousRow()
	 * @return the result for this column in <b>Double</b> format
	 */
	public double getValueDouble(String attribute){
		double ret=-1;
		if(result == null)
			ret= -1;
		else{
			try {
				ret= result.getDouble(attribute);
			}
			catch (Exception e) {
				errorManager("Attribute:"+attribute+" error", e);
			}
		}
		return ret;
	}


	public byte[] getBytes(String attribute) {
		byte [] ret = null;
		if(result == null) return ret;
		else{
			try {
				ret= result.getBytes(attribute);
			}
			catch (Exception e) {
				errorManager("Attribute:"+attribute+" error", e);
			}
		}
		return ret;
	}



	public int[] getColumnInt(String attribute) {

		int [] ret = null;
		try {
			if(result.last()){
				int nof_rows = result.getRow();
				result.first();
				ret = new int[nof_rows];
				int count=0;
				do {
					ret[count++] = result.getInt(attribute);
				}
				while(result.next());
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return ret;
	}

	//------------------

	public String toString() {
		return "mysql://"+db+"@"+host;
	}

	/**
	 * Function to manage error message due to bad MySQL syntax or connection problem
	 *
	 *
	 * @param msg 	A string message give by the user
	 * @param e		The automatic generated exception
	 * @return		A false value meaning that the system have found an error.
	 */
	private boolean errorManager(String msg,Exception e) {
		if(e instanceof SQLException) {
			System.err.println(msg+"\n"+e.getMessage().toString());
		}
		else {
			e.printStackTrace();
		}
//		JOptionPane.showMessageDialog(this, ex.getMessage().toString(), "Error...", JOptionPane.ERROR_MESSAGE);
		return false;

	}

}
