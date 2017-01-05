package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
public class Request implements Runnable {

	private static String url;
	private static String user;
	private static String pass;

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

			// load in the header data
			ArrayList<String> lines = readHeaders(in);

			String ean = getEan(lines);
			String method = getMethod(lines);

			// declare a new database manager
			DatabaseManager dbm = new DatabaseManager(url, user, pass);

			if (method.equalsIgnoreCase("get")) {
				getHeaders(out);
				// send the response to the client
				out.print(dbm.getJSON(ean) + "\r\n");
				System.out.println("Returned data on: " + ean);
			} else if (method.equalsIgnoreCase("post")) {
				System.out.println("This is a post request");
				postHeaders(out);
				
				out.print("Success\r\n\r\n");
				// load in each new line
				String line;
				while ((line = in.readLine()) != null) {
					lines.add(line);
				}
				// really this should probably parsing some json
				System.out.println("Parsing JSON on line: " + lines.get(lines.size() - 1));
				int[] data = JSONify.fromJSON(lines.get(lines.size() - 1));
				boolean exists = false;
				try {
					exists = dbm.exists(ean) /*|| Record.hasRecord(ean)*/;
				} catch (SQLException e) {
					System.out.println("Error determining existing");
				}
				// update the row if it already exists
				if (exists) {
					System.out.println("Attempting to update existing record");
					//Record.update(ean, data, dbm);
					dbm.update(ean, data);
					System.out.println("Set " + ean + " to " + data);
				} else {
					System.out.println("Attempting to add new record");
					//Record.add(ean, data);
					dbm.add(ean, data);
					System.out.println("Added " + ean + " and set to " + data);
				}
			}
			// disconnect
			out.close();
			out.flush();
			in.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void postHeaders(PrintWriter out) {
		// Send the headers
		out.print("HTTP/1.1 201 Created\r\n"); // Version & status code
		out.print("Content-Type: text/plain\r\n"); // The type of data
		out.print("Date: " + new Date().toString() + "\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers
	}

	public static void getHeaders(PrintWriter out) {
		// Send the headers
		out.print("HTTP/1.1 200 OK\r\n"); // Version & status code
		out.print("Content-Type: application/JSON\r\n"); // The type of data
		out.print("Date: " + new Date().toString() + "\r\n"); // The type of data
		out.print("Connection: close\r\n"); // Will close stream
		out.print("\r\n"); // End of headers
	}

	public static ArrayList<String> readHeaders(BufferedReader in) throws IOException {
		// read the headers the client sends into an arraylist
		ArrayList<String> lines = new ArrayList<String>();
		String line;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0) {
				break;
			}
			lines.add(line);
		}

		return lines;
	}

	public static String getMethod(ArrayList<String> lines) {
		return lines.get(0).substring(0, lines.get(0).indexOf(' '));
	}

	public static String getEan(ArrayList<String> lines) {
		// get the ean out of the request
		int index1 = lines.get(0).indexOf('/') + 1;
		int index2 = lines.get(0).lastIndexOf(' ');
		return lines.get(0).substring(index1, index2);
	}

	public static void setUrl(String url) {
		Request.url = url;
	}

	public static void setUser(String user) {
		Request.user = user;
	}

	public static void setPass(String pass) {
		Request.pass = pass;
	}

}
