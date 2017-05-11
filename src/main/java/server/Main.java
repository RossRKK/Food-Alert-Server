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
        DatabaseManager dbm = new DatabaseManager(ConfigLoader.getUrl(), ConfigLoader.getUser(), ConfigLoader.getPass());
        //create the supermarket item table
        if (!dbm.tableExists(ConfigLoader.getFoodTableName(), ConfigLoader.getDbName())) {
            // create the table

            // fields are name, ean, and for each base there are 3 fields
            int length = (DatabaseManager.tertiaryFieldNameBases.length * 3) + (DatabaseManager.binaryFieldNameBases.length * 2) + DatabaseManager.continuousFieldNames.length + 2;

            String[] fieldNames = new String[length];
            String[] fieldTypes = new String[length];
            String[] extras = new String[1];

            fieldNames[0] = "ean";
            fieldTypes[0] = "varchar(20)";
            extras[0] = "primary key (" + fieldNames[0] + ")";

            fieldNames[1] = "name";
            fieldTypes[1] = "varchar(80)";

            int nextIndex = 2;
            for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "C";
                fieldTypes[nextIndex] = "int";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "T";
                fieldTypes[nextIndex] = "int";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i] + "N";
                fieldTypes[nextIndex] = "int";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.binaryFieldNameBases[i] + "C";
                fieldTypes[nextIndex] = "int";
                nextIndex++;

                fieldNames[nextIndex] = DatabaseManager.binaryFieldNameBases[i] + "N";
                fieldTypes[nextIndex] = "int";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.continuousFieldNames.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.continuousFieldNames[i];
                fieldTypes[nextIndex] = "double";
                nextIndex++;
            }

            dbm.createTable(ConfigLoader.getFoodTableName(), fieldNames, fieldTypes, extras);
        }
        
        //create the food services tables
        if (!dbm.tableExists(ConfigLoader.getServiceTableName(), ConfigLoader.getDbName())) {
            // create the table

            // fields are id, name and description
            int length = 3;

            String[] fieldNames = new String[length];
            String[] fieldTypes = new String[length];
            String[] extras = new String[1];

            fieldNames[0] = "foodServiceID";
            fieldTypes[0] = "varchar(20)";
            extras[0] = "primary key (" + fieldNames[0] + ")";

            fieldNames[1] = "name";
            fieldTypes[1] = "varchar(80)";
            
            fieldNames[2] = "description";
            fieldTypes[2] = "varchar(140)";

            dbm.createTable(ConfigLoader.getServiceTableName(), fieldNames, fieldTypes, extras);
        }
        
        //create the service items table
        if (!dbm.tableExists(ConfigLoader.getItemTableName(), ConfigLoader.getDbName())) {
            // create the table

            // fields are name, ean, and for each base there are 3 fields
            int length = 6 + DatabaseManager.tertiaryFieldNameBases.length + DatabaseManager.binaryFieldNameBases.length + DatabaseManager.continuousFieldNames.length;

            String[] fieldNames = new String[length];
            String[] fieldTypes = new String[length];
            String[] extras = new String[2];

            fieldNames[0] = "itemID";
            fieldTypes[0] = "varchar(20)";
            extras[0] = "primary key (" + fieldNames[0] + ")";

            fieldNames[1] = "name";
            fieldTypes[1] = "varchar(80)";
            
            fieldNames[2] = "description";
            fieldTypes[2] = "varchar(140)";
            
            fieldNames[3] = "category";
            fieldTypes[3] = "varchar(80)";
            
            fieldNames[4] = "price";
            fieldTypes[4] = "double";
            
            fieldNames[5] = "serviceID";
            fieldTypes[5] = "varchar(20)";
            //FOREIGN KEY (PersonID) REFERENCES Persons(PersonID)
            extras[1] = "foreign key (" + fieldNames[5] + ") references " + ConfigLoader.getServiceTableName() + "(serviceID)";

            int nextIndex = 6;
            for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.tertiaryFieldNameBases[i];
                fieldTypes[nextIndex] = "boolean";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.binaryFieldNameBases[i];
                fieldTypes[nextIndex] = "boolean";
                nextIndex++;
            }

            for (int i = 0; i < DatabaseManager.continuousFieldNames.length; i++) {
                fieldNames[nextIndex] = DatabaseManager.continuousFieldNames[i];
                fieldTypes[nextIndex] = "double";
                nextIndex++;
            }

            dbm.createTable(ConfigLoader.getItemTableName(), fieldNames, fieldTypes, extras);
        }
    }

    public static void setPort(int p) {
        port = p;
    }
}
