import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Request implements Runnable {
	

	private static final String url = "jdbc:mysql://localhost/food";
	private static final String user = "root";

	private Socket client;
	
	public Request(Socket client) {
		this.client = client;
	}
	
	@Override
	public void run() {
		try {
			// create the input and output streams
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream());
	
			// Send the headers
			out.print("HTTP/1.1 200 \r\n"); // Version & status code
			out.print("Content-Type: text/JSON\r\n"); // The type of data
			out.print("Connection: close\r\n"); // Will close stream
			out.print("\r\n"); // End of headers
	
			// load in the header data
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() == 0) {
					break;
				}
				lines.add(line);
			}
	
			// get the ean out of the request
			int index1 = lines.get(0).indexOf('/') + 1;
			int index2 = lines.get(0).lastIndexOf(' ');
			String ean = lines.get(0).substring(index1, index2);
	
			String method = lines.get(0).substring(0, lines.get(0).indexOf(' '));
	
			// open the connection to the mySQL server
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, user, "DoctorWh0!");
	
			if (method.equalsIgnoreCase("get")) {
				// get the results of a query for the ean using prepared
				// statements to make injection impossible
				PreparedStatement stmt = con.prepareStatement("select * from food where ean = ?");
				stmt.setString(1, ean);
				ResultSet rs = stmt.executeQuery();
	
				// get the response from the database response
				/*int containsNuts = -1;
				while (rs.next()) {
					// read the contains nut boolean
					containsNuts = rs.getInt(2);
				}
				con.close();*/
	
				// really we should be making some json
				String resp = JSONify.toJSON(rs);
				con.close();
	
				// send the response to the client
				out.print(resp + "\r\n");
				System.out.println("Returned data on: " + ean);
			} else if (method.equalsIgnoreCase("post")) {
				// load in each new line
				while ((line = in.readLine()) != null) {
					lines.add(line);
				}
				// really this should probably parsing some json
				int data = Integer.parseInt(lines.get(lines.size() - 1));
				
				PreparedStatement statement = con.prepareStatement("select 1 from food where ean = ?");
				statement.setString(1, ean);
				ResultSet rs = statement.executeQuery();
				
				boolean exists = rs.next();
				
				if (exists) {
					PreparedStatement stmt = con.prepareStatement("update food SET containsNuts = ? WHERE ean = ?");
					stmt.setInt(2, data);
					stmt.setString(1, ean);
					stmt.executeUpdate();
					System.out.println("Added " + ean + " and set to " + data);
				} else {
					PreparedStatement stmt = con.prepareStatement("insert into food (ean, containsNuts) values (?, ?)");
					stmt.setString(1, ean);
					stmt.setInt(2, data);
					stmt.executeUpdate();
					System.out.println("Set " + ean + " to " + data);
				}
			}
			// disconnect
			out.close();
			in.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
