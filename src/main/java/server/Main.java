package server;

import java.net.ServerSocket;
import java.sql.SQLException;

import server.database.DatabaseManager;
import server.io.ConfigLoader;
import server.io.Request;

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
            // create the table

            // fields are name, ean, and for each base there are 3 fields
            int length = (DatabaseManager.tertiaryFieldNameBases.length * 3) + (DatabaseManager.binaryFieldNameBases.length * 2) + DatabaseManager.continuousFieldNames.length + 2;

            String[] fieldNames = new String[length];
            String[] fieldTypes = new String[length];

            fieldNames[0] = "ean";
            fieldTypes[0] = "varchar(20)";

            fieldNames[1] = "name";
            fieldTypes[1] = "varchar(80)";

            int nextIndex = 2;
            for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "C";
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "T";
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "N";
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.binaryFieldNameBases[i] + "C";
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.binaryFieldNameBases[i] + "N";
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.continuousFieldNames.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.continuousFieldNames[i];
                fieldTypes[nextIndex] = "int(10)";
                nextIndex++;
            }

            new DatabaseManager(ConfigLoader.getUrl(), ConfigLoader.getUser(), ConfigLoader.getPass()).createTable(ConfigLoader.getFoodTableName(), fieldNames, fieldTypes);
        } else {
            // check it has the required fields
        }
        
        //TODO add checks for restaurant, branch and restaurant item tables
    }

    public static void setPort(int p) {
        port = p;
    }
}
