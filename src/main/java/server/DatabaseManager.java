package server;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
	// fieldnames are added in this oreder:
	// tertiary, binary, continuous
	public static String[] fieldNames;
	public static String[] tertiaryFieldNameBases;
	public static String[] binaryFieldNameBases;
	public static String[] continuousFieldNames;
	public static String nameFieldName = "name";

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
	 * 
	 * @param ean
	 *            The barcode number of the item in question
	 * @return The formatted JSON for a given ean
	 * @throws SQLException
	 */
	public String getJSON(String ean) throws SQLException {
		// get the results of a query for the ean using prepared
		// statements to make injection impossible
		PreparedStatement stmt = con
				.prepareStatement("select * from " + ConfigLoader.getFoodTableName() + " where ean = ?");
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
	 * 
	 * @param ean
	 *            The barcode number of the item in question
	 * @return The results set for that ean
	 * @throws SQLException
	 */
	public int getVotes(String ean, String field) throws SQLException {
		// get the results of a query for the ean using prepared
		// statements to make injection impossible
		PreparedStatement stmt = con
				.prepareStatement("select " + field + " from " + ConfigLoader.getFoodTableName() + " where ean = ?");
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
	 * 
	 * @param ean
	 *            The barcode number of the item in question
	 * @param data
	 *            The details of the item
	 * @throws SQLException
	 */
	public void add(String ean, String name, int data[]) throws SQLException {
		// insert into food (ean, containsNuts) values (?, ?)
		// generate the correct prepared statement
		// "insert into food (ean, fieldName1, fieldName2, ...) values (?, ?, ?,
		// ...)
		String command = "insert into " + ConfigLoader.getFoodTableName() + " (ean, name, ";
		int index = 0;
		for (int i = 0; i < tertiaryFieldNameBases.length; i++) {
			// generate the correct field name
			command += tertiaryFieldNameBases[i] + "C, " + tertiaryFieldNameBases[i] + "T, " + tertiaryFieldNameBases[i]
					+ "N";
			// command += fieldNames[i];
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		for (int i = 0; i < binaryFieldNameBases.length; i++) {
			// generate the correct field name
			command += binaryFieldNameBases[i] + "C, " + binaryFieldNameBases[i] + "N";
			// command += fieldNames[i];
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		for (int i = 0; i < continuousFieldNames.length; i++) {
			// generate the correct field name
			command += continuousFieldNames[i];
			// command += fieldNames[i];
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		command += ") values (?, ?, ";

		index = 0;
		for (int i = 0; i < tertiaryFieldNameBases.length; i++) {

			// put the vote in the write place
			if (data[index] == CONTAINS) {
				command += "?, 0, 0";
			} else if (data[index] == TRACE) {
				command += "0, ?, 0";
			} else if (data[index] == NONE) {
				command += "0, 0, ?";
			} else {
				command += "0, ?";
			}

			// command += "?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		for (int i = 0; i < binaryFieldNameBases.length; i++) {
			// put the vote in the write place
			if (data[index] == CONTAINS) {
				command += "?, 0";
			} else if (data[index] == NONE) {
				command += "0, ?";
			} else {
				command += "0, ?";
			}

			// command += "?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		for (int i = 0; i < continuousFieldNames.length; i++) {
			command += "?";
			// command += "?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		command += ")";
		// set up the prepared statement
		PreparedStatement stmt = con.prepareStatement(command);
		stmt.setString(1, ean);
		stmt.setString(2, name);

		int nonContinuousLength = tertiaryFieldNameBases.length + binaryFieldNameBases.length;
		// fill the statement with values
		// the 3 is to account for the e (so we start at the second element)
		index = 3;
		for (int i = 0; i < nonContinuousLength; i++) {
			int noVotes = 1;
			if (data[i] == UNKNOWN) {
				noVotes = 0;
			}
			stmt.setInt(index, noVotes);
			index++;
		}

		for (int i = nonContinuousLength; i < data.length; i++) {
			stmt.setInt(index, data[i]);
			index++;
		}

		// execute the prepared statement
		stmt.executeUpdate();
		stmt.close();
		con.close();
	}

	/**
	 * Updates the data on a given item
	 * 
	 * @param ean
	 *            The barcode number of the item in question
	 * @param data
	 *            The details of the item
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public void update(String ean, String name, int data[]) throws SQLException, ClassNotFoundException {
		// update food SET containsNuts = ? WHERE ean = ?
		// generate the correct prepared statement
		String command = "update " + ConfigLoader.getFoodTableName() + " set name = ?, ";
		int index = 0;
		// add tertiary fields
		for (int i = 0; i < tertiaryFieldNameBases.length; i++) {
			command += tertiaryFieldNameBases[i] + getExt(data[index]) + " = ?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		// add binary fields
		for (int i = 0; i < binaryFieldNameBases.length; i++) {
			command += binaryFieldNameBases[i] + getExt(data[index]) + " = ?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		// add contiuous fields
		for (int i = 0; i < continuousFieldNames.length; i++) {
			command += continuousFieldNames[i] + " = ?";
			if (index < data.length - 1) {
				command += ", ";
			}
			index++;
		}

		command += " where ean = ?";
		int nonContinuousLength = tertiaryFieldNameBases.length + binaryFieldNameBases.length;
		int[] votes = new int[nonContinuousLength];

		index = 0;
		for (int i = 0; i < tertiaryFieldNameBases.length; i++) {
			votes[index] = new DatabaseManager(url, user, pass).getVotes(ean,
					tertiaryFieldNameBases[i] + getExt(data[i]));
			index++;
		}

		for (int i = 0; i < binaryFieldNameBases.length; i++) {
			votes[index] = new DatabaseManager(url, user, pass).getVotes(ean,
					binaryFieldNameBases[i] + getExt(data[i]));
			index++;
		}
		// set up the prepared statement
		PreparedStatement stmt = con.prepareStatement(command);

		// fill the statement with values
		stmt.setString(1, name);
		index = 2;
		// add binary and tertiary votes
		for (int i = 0; i < nonContinuousLength; i++) {
			int noVotes = 1;
			if (data[i] == UNKNOWN) {
				noVotes = 0;
			}
			// get the current number of votes
			stmt.setInt(index, votes[i] + noVotes);
			index++;
		}
		// add continuous data
		for (int i = 0; i < continuousFieldNames.length; i++) {
			stmt.setInt(index, data[i]);
			index++;
		}
		stmt.setString(index, ean);
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
	 * 
	 * @param ean
	 *            The barcode number of the item in question
	 * @return Whether that item exists in the database
	 * @throws SQLException
	 */
	public boolean exists(String ean) throws SQLException {
		// run a query to see if the element already exists
		PreparedStatement statement = con
				.prepareStatement("select 1 from " + ConfigLoader.getFoodTableName() + " where ean = ?");
		statement.setString(1, ean);
		ResultSet rs = statement.executeQuery();
		boolean ans = rs.next();
		statement.close();
		rs.close();
		return ans;
	}

	public static void setFieldBases(String[] tertiaryFieldBases, String[] binaryFieldBases,
			String[] contiuousFieldBases) {
		tertiaryFieldNameBases = tertiaryFieldBases;
		binaryFieldNameBases = binaryFieldBases;
		continuousFieldNames = contiuousFieldBases;
		String[] names = new String[tertiaryFieldNameBases.length + binaryFieldNameBases.length
				+ continuousFieldNames.length];
		int index = 0;
		// add tertiary field names to the field names array
		for (int i = 0; i < tertiaryFieldNameBases.length; i++) {
			String cur = tertiaryFieldNameBases[i];
			cur = cur.substring(0, 1).toUpperCase() + cur.substring(1);
			names[index] = "contains" + cur;
			index++;
		}

		// add bianry field names to the field names array
		for (int i = 0; i < binaryFieldNameBases.length; i++) {
			String cur = binaryFieldNameBases[i];
			cur = cur.substring(0, 1).toUpperCase() + cur.substring(1);
			names[index] = "is" + cur;
			index++;
		}

		// add contiuous filed names to the field names array
		for (int i = 0; i < continuousFieldNames.length; i++) {
			names[index] = continuousFieldNames[i];
			index++;
		}

		fieldNames = names;
	}

	/**
	 * Determines whether a table exists in the database
	 * 
	 * @param tableName
	 *            The name of the table to check for
	 * @param dbName
	 *            The name of the db to check for it in
	 * @return Whether the table exists
	 * @throws SQLException
	 */
	public boolean tableExists(String tableName, String dbName) throws SQLException {
		/*
		 * SELECT table_name FROM information_schema.tables WHERE table_schema =
		 * 'databasename' AND table_name = 'testtable';
		 */
		PreparedStatement statement = con
				.prepareStatement("select ? from information_schema.tables where table_schema = ?;");
		statement.setString(1, tableName);
		statement.setString(2, dbName);
		ResultSet rs = statement.executeQuery();
		boolean ans = rs.next();
		statement.close();
		rs.close();
		return ans;
	}

	/**
	 * Create a new table in the databasse
	 * 
	 * @param tableName
	 *            The name of the table
	 * @param fieldNames
	 *            The names of the fields
	 * @param fieldTypes
	 *            The SQL types of the fields
	 * @throws SQLException
	 */
	public void createTable(String tableName, String[] fieldNames, String[] fieldTypes) throws SQLException {
		// create table table_name (fieldName type, ...);
		String command = "create table " + tableName + " (";

		for (int i = 0; i < fieldNames.length; i++) {
			command += fieldNames[i] + " " + fieldTypes[i];
			if (i < fieldNames.length - 1) {
				command += ", ";
			}
		}

		command += ");";
		PreparedStatement statement = con.prepareStatement(command);

		statement.execute();
		// ResultSet rs = statement.executeQuery();
		statement.close();
		// rs.close();
	}
}
