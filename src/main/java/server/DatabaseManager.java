package server;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
	public static String[] fieldNames;
	public static String[] fieldBases;
	
	public static int CONTAINS;
	public static int TRACE;
	public static int NONE;
	public static int UNKNOWN;

	private Connection con;
	private String url;
	private String user;
	private String pass;

	public DatabaseManager(String url, String user, String pass) throws ClassNotFoundException, SQLException {
		this.url = url;
		this.pass = pass;
		this.user = user;
		Driver myDriver = new com.mysql.jdbc.Driver();
		DriverManager.registerDriver(myDriver);
		// open the connection to the mySQL server
		con = DriverManager.getConnection(url, user, pass);
	}

	/**
	 * Returns the database's details on a given ean as JSON
	 * @param ean The barcode number of the item in question
	 * @return	The formatted JSON for a given ean
	 * @throws SQLException
	 */
	public String getJSON(String ean) throws SQLException {
		// get the results of a query for the ean using prepared
		// statements to make injection impossible
		PreparedStatement stmt = con.prepareStatement("select * from " + ConfigLoader.getFoodTableName() + " where ean = ?");
		stmt.setString(1, ean);
		ResultSet rs = stmt.executeQuery();

		// get the json response
		String ans = JSONify.toJSON(rs);
		stmt.close();
		rs.close();
		con.close();
		return ans;
	}
	
	/**
	 * Returns the database's details on a given ean as a result set
	 * @param ean The barcode number of the item in question
	 * @return The results set for that ean
	 * @throws SQLException
	 */
	public int getVotes(String ean, String field) throws SQLException {
		// get the results of a query for the ean using prepared
		// statements to make injection impossible
		PreparedStatement stmt = con.prepareStatement("select " + field + " from " + ConfigLoader.getFoodTableName() + " where ean = ?");
		stmt.setString(1, ean);
		ResultSet rs = stmt.executeQuery();

		rs.next();
		int votes = rs.getInt(field);
		
		stmt.close();
		rs.close();
		con.close();
		return votes;
	}

	/**
	 * Insert a new item into the database
	 * @param ean The barcode number of the item in question
	 * @param data The details of the item
	 * @throws SQLException
	 */
	public void add(String ean, int data[]) throws SQLException {
		// insert into food (ean, containsNuts) values (?, ?)
		// generate the correct prepared statement
		//"insert into food (ean, fieldName1, fieldName2, ...) values (?, ?, ?, ...)
		String command = "insert into " + ConfigLoader.getFoodTableName() + " (ean, ";
		for (int i = 0; i < data.length; i++) {
			//generate the correct field name
			command += fieldBases[i] + "C, " + fieldBases[i] + "T, " + fieldBases[i] + "N";
			//command += fieldNames[i];
			if (i < data.length - 1) {
				command += ", ";
			}
		}
		command += ") values (?, ";
		for (int i = 0; i < data.length; i++) {
			//put the vote in the write place
			if (data[i] == CONTAINS) {
				command += "?, 0, 0";
			} else if (data[i] == TRACE) {
				command += "0, ?, 0";
			} else if (data[i] == NONE) {
				command += "0, 0, ?";
			} else {
				command += "0, 0, 0";
			}
			
			//command += "?";
			if (i < data.length - 1) {
				command += ", ";
			}
		}
		command += ")";
		// set up the prepared statement
		System.out.println(command);
		PreparedStatement stmt = con.prepareStatement(command);
		stmt.setString(1, ean);
		// fill the statement with values
		//the 2 is to account for the ean (so we start at the second element)
		for (int i = 2; i < data.length + 2; i++) {
			stmt.setInt(i, 1);
		}
		
		// execute the prepared statement
		stmt.executeUpdate();
		stmt.close();
		con.close();
	}

	/**
	 * Updates the data on a given item
	 * @param ean The barcode number of the item in question
	 * @param data The details of the item
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	public void update(String ean, int data[]) throws SQLException, ClassNotFoundException {
		// update food SET containsNuts = ? WHERE ean = ?
		// generate the correct prepared statement
		String command = "update " + ConfigLoader.getFoodTableName() + " set ";
		for (int i = 0; i < data.length; i++) {
			if (data[i] != UNKNOWN) {
				command += fieldBases[i] + getExt(data[i]) + " = ?";
				if (i < data.length - 1) {
					command += ", ";
				}
			}
		}
		command += " where ean = ?";
		int[] votes = new int[data.length];
		
		for (int i = 0; i < votes.length; i++) {
			votes[i] = new DatabaseManager(url, user, pass).getVotes(ean, DatabaseManager.fieldBases[i] + getExt(data[i]));
			System.out.println(votes[i]);
		}
		// set up the prepared statement
		PreparedStatement stmt = con.prepareStatement(command);
		// fill the statement with values
		int i;
		for (i = 1; i <= data.length; i++) {
			//get the current number of votes
			stmt.setInt(i, votes[i -1] + 1);
		}
		stmt.setString(i, ean);
		// execute the prepared statement
		stmt.executeUpdate();
		stmt.close();
		con.close();
	}
	
	private String getExt(int data) {
		String ext = "C";
		if (data == TRACE) {
			ext = "T";
		} else if (data == NONE) {
			ext = "N";
		}
		return ext;
	}
	/**
	 * Checks whether data on a given ean exists
	 * @param ean The barcode number of the item in question
	 * @return Whether that item exists in the database
	 * @throws SQLException
	 */
	public boolean exists(String ean) throws SQLException {
		// run a query to see if the element already exists
		PreparedStatement statement = con.prepareStatement("select 1 from " + ConfigLoader.getFoodTableName() +" where ean = ?");
		statement.setString(1, ean);
		ResultSet rs = statement.executeQuery();
		boolean ans = rs.next();
		statement.close();
		rs.close();
		return ans;
	}

	public static void setFieldBases(String[] fieldBases) {
		DatabaseManager.fieldBases = fieldBases;
		String[] names = new String[fieldBases.length];
		for (int i = 0; i < fieldBases.length; i++) {
			String cur = fieldBases[i];
			cur = cur.substring(0, 1).toUpperCase() + cur.substring(1);
			names[i] = cur;
		}
		fieldNames = names;
	}
	
	/**
	 * Determines whether a table exists in the database
	 * @param tableName The name of the table to check for
	 * @param dbName The name of the db to check for it in
	 * @return Whether the table exists
	 * @throws SQLException
	 */
	public boolean tableExists(String tableName, String dbName) throws SQLException {
		/*SELECT table_name
		FROM information_schema.tables
		WHERE table_schema = 'databasename'
		AND table_name = 'testtable';*/
		PreparedStatement statement = con.prepareStatement("select ? from information_schema.tables where table_schema = ?;");
		statement.setString(1, tableName);
		statement.setString(2,  dbName);
		ResultSet rs = statement.executeQuery();
		boolean ans = rs.next();
		statement.close();
		rs.close();
		return ans;
	}
	
	/**
	 * Create a new table in the databasse
	 * @param tableName The name of the table
	 * @param fieldNames The names of the fields
	 * @param fieldTypes The SQL types of the fields
	 * @throws SQLException 
	 */
	public void createTable(String tableName, String[] fieldNames, String[] fieldTypes) throws SQLException {
		//create table table_name (fieldName type, ...);
		String command = "create table " + tableName +" (";
		
		for (int i = 0; i < fieldNames.length; i++) {
			command += fieldNames[i] + " " + fieldTypes[i];
			if (i < fieldNames.length - 1) {
				command += ", ";
			}
		}
		
		command += ");";
		PreparedStatement statement = con.prepareStatement(command);
		
		statement.execute();
		//ResultSet rs = statement.executeQuery();
		statement.close();
		//rs.close();
	}
}
