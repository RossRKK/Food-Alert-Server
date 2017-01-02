package server;
import java.net.ServerSocket;
import java.sql.SQLException;

public class Main {

	private static ServerSocket ss;
	
	private static int port;

	public static void main(String args[]) {
		try {
			ConfigLoader.loadConfig();
			port = Integer.parseInt(args[0]);

			intialiseDatabase();
			// Create a ServerSocket to listen on that port.
			ss = new ServerSocket(port);

			// loop until the server is terminated
			boolean done = false;
			while (!done) {
				// open a new thread to handle the request
				(new Thread(new Request(ss.accept()))).start();
			}
			ss.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void intialiseDatabase() throws ClassNotFoundException, SQLException {
		if (!new DatabaseManager(ConfigLoader.getUrl(), ConfigLoader.getUser(), ConfigLoader.getPass()).tableExists(ConfigLoader.getFoodTableName(), ConfigLoader.getDbName())) {
			//create the table
			String[] bases = ConfigLoader.getFieldNames().split(",");
			
			//fields are name, ean, and for each base there are 3 fields
			int length = (bases.length * 3) + 2;
			
			String[] fieldNames = new String[length];
			String[] fieldTypes = new String[length];
			
			fieldNames[0] = "ean";
			fieldTypes[0] = "varchar(20)";
			
			fieldNames[1] = "name";
			fieldTypes[1] = "varchar(25)";
			
			int nextIndex = 2;
			for (int i  = 0 ; i < bases.length; i++) {
				fieldNames[nextIndex] = bases[i] + "C";
				nextIndex++;
				fieldNames[nextIndex] = bases[i] + "T";
				nextIndex++;
				fieldNames[nextIndex] = bases[i] + "N";
				nextIndex++;
			}
			
			for (int i = 2; i < fieldTypes.length; i++){
				fieldTypes[i] = "int(10)";
			}

			new DatabaseManager(ConfigLoader.getUrl(), ConfigLoader.getUser(), ConfigLoader.getPass()).createTable(ConfigLoader.getFoodTableName(), fieldNames, fieldTypes);
		} else {
			//check it has the required fields
		}
	}

	public static void setPort(int p) {
		port = p;
	}
}