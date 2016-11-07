import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
	public static final String[] fieldNames = {"containsNuts", "containsDairy"};
	
	private Connection con;
	
	public DatabaseManager(String url, String user, String pass) throws ClassNotFoundException, SQLException {
		// open the connection to the mySQL server
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(url, user, "DoctorWh0!");
	}
	
	public String get(String ean) throws SQLException {
		// get the results of a query for the ean using prepared
		// statements to make injection impossible
		PreparedStatement stmt = con.prepareStatement("select * from food where ean = ?");
		stmt.setString(1, ean);
		ResultSet rs = stmt.executeQuery();				
		

		//get the json response
		String ans = JSONify.toJSON(rs);
		con.close();
		return ans;
	}
	
	public void add(String ean, int data[]) throws SQLException {
		//insert into food (ean, containsNuts) values (?, ?)
		//generate the correct prepared statement
		String command = "insert into food (ean, ";
		for (int i = 0; i < data.length; i++) {
			command += fieldNames[i];
			if (i < data.length - 1) {
				command += ", ";
			}
		}
		command += ") values (?, ";
		for (int i = 0; i < data.length; i++){
			command += "?";
			if (i < data.length - 1) {
				command += ", ";
			}
		}
		//set up the prepared statement
		PreparedStatement stmt = con.prepareStatement(command);
		stmt.setString(1, ean);
		//fill the statement with values
		for (int i = 2; i < data.length + 2; i++) {
			stmt.setInt(i, data[i-2]);	
		}
		//execute the prepared statement
		stmt.executeUpdate();
	}
	
	public void update(String ean, int data[]) throws SQLException {
		//update food SET containsNuts = ? WHERE ean = ?
		//generate the correct prepared statement
		String command = "update food set ";
		for (int i = 0; i < data.length; i++) {
			command += fieldNames[i] + " = ?";
			if (i < data.length - 1) {
				command += ", ";
			}
		}
		command += " where ean = ?";
		//set up the prepared statement
		PreparedStatement stmt = con.prepareStatement(command);
		//fill the statement with values
		int i;
		for (i = 1; i <= data.length; i++) {
			stmt.setInt(i, data[i-1]);
		}
		stmt.setString(i, ean);
		//execute the prepared statement
		stmt.executeUpdate();
	}
	
	public boolean exists(String ean) throws SQLException {
		//run a query to see if the element already exists
		PreparedStatement statement = con.prepareStatement("select 1 from food where ean = ?");
		statement.setString(1, ean);
		ResultSet rs = statement.executeQuery();
		return rs.next();
	}
}
