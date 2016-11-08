import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
public class Request implements Runnable {

	private static final String url = "jdbc:mysql://localhost/food?autoReconnect=true&useSSL=false";
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

			// send the client our headers
			headers(out);

			// load in the header data
			ArrayList<String> lines = readHeaders(in);

			String ean = getEan(lines);

			String method = getMethod(lines);

			// declare a new database manager
			DatabaseManager dbm = new DatabaseManager(url, user, "DoctorWh0!");

			if (method.equalsIgnoreCase("get")) {
				// send the response to the client
				out.print(dbm.get(ean) + "\r\n");
				System.out.println("Returned data on: " + ean);
			} else if (method.equalsIgnoreCase("post")) {
				// load in each new line
				String line;
				while ((line = in.readLine()) != null) {
					lines.add(line);
				}
				// really this should probably parsing some json
				int[] data = JSONify.fromJSON(lines.get(lines.size() - 1));

				boolean exists = dbm.exists(ean);

				// update the row if it already exists
				if (exists) {
					dbm.update(ean, data);
					System.out.println("Set " + ean + " to " + data);
				} else {
					dbm.add(ean, data);
					System.out.println("Added " + ean + " and set to " + data);
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

	public static void headers(PrintWriter out) {
		// Send the headers
		out.print("HTTP/1.1 200 \r\n"); // Version & status code
		out.print("Content-Type: application/JSON\r\n"); // The type of data
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

}
