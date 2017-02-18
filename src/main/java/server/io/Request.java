package server.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import server.database.DatabaseManager;
import server.database.Record;
import server.util.JSONify;

public class Request implements Runnable {

    private static String url;
    private static String user;
    private static String pass;
    
    private BufferedReader in;
    private PrintWriter out;
    
    private String extension;
    private String path;
    
    private String endodedData;
    
    private String ean;
    private String method;
    // declare a new database manager
    private DatabaseManager dbm;

    private Socket client;

    public Request(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            setUpIO();

            processURL();
            switch (path) {
                case "b":
                    //handle barcode requests to /b/
                    barcodeRequest();
                    break;
                case "r":
                    //handle restaurant requests
                    break;
                case "branch":
                    //handle branch requests
                    break;
                case "web":
                    //serve the relevant webpage
                    break;
                default:
                    //serve the homepage
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
         // disconnect
            out.close();
            
            try {
                in.close();
            } catch (IOException e) {
                System.out.println("Error closing input stream");
            }
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket");
            }
        }

    }
    
    /**
     * setup the IO used by this request
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    private void setUpIO() throws ClassNotFoundException, SQLException, IOException {
        // create the input and output streams
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream());
        
        // declare a new database manager
        dbm = new DatabaseManager(url, user, pass);
    }

    /**
     * Process the incoming information
     * @throws IOException 
     */
    private void processURL() throws IOException {
     // load in the header data
        ArrayList<String> lines = readHeaders(in);

        extension = getExtension(lines);
        path = getPath(extension);
        
        endodedData = getData(extension);
        
        ean = getEan(endodedData);
        method = getMethod(lines);
    }
    
    /**
     * Handle barcode requests
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void barcodeRequest() throws SQLException, ClassNotFoundException, IOException {
        if (method.equalsIgnoreCase("get")) {
            printHeaders();

            Record r = JSONify.decode(endodedData);

            if (r != null) {
                int[] data = r.getData();
                String name = r.getName();

                boolean exists = dbm.exists(ean);
                // update the row if it already exists
                if (exists) {
                    System.out.println("Attempting to update existing record");
                    // Record.update(ean, data, dbm);
                    dbm.update(ean, name, data);
                    System.out.println("Set " + ean + " to " + data);
                } else {
                    System.out.println("Attempting to add new record");
                    // Record.add(ean, data);
                    dbm.add(ean, name, data);
                    System.out.println("Added " + ean + " and set to " + data);
                }
            } else {
                // send the response to the client
                out.print(dbm.getJSON(ean) + "\r\n");
                System.out.println("Returned data on: " + ean);
            }
        }
    }

    /**
     * Return the last segment of the URL that contains the encoded data
     * @param extension The full extension of the URL
     * @return The encoded data at the end of the URL
     */
    private String getData(String extension) {
        return extension.substring(extension.lastIndexOf('/'),  extension.length());
    }

    /**
     * Return the path that is being addressed
     * @param extension The full extension on the URL
     * @return The path that the request was directed at
     */
    private String getPath(String extension) {
        return extension.substring(0, extension.lastIndexOf('/'));
    }

    /**
     * Print the headers to the client
     */
    public void printHeaders() {
        // Send the headers
        out.print("HTTP/1.1 200 OK\r\n"); // Version & status code
        out.print("Content-Type: application/JSON\r\n"); // The type of data
        out.print("Date: " + new Date().toString() + "\r\n"); // The type of
                                                              // data
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

    public static String getExtension(ArrayList<String> lines) {
        // get the ean out of the request
        int index1 = lines.get(0).indexOf('/') + 1;
        int index2 = lines.get(0).lastIndexOf(' ');
        return lines.get(0).substring(index1, index2);
    }

    public static String getEan(String extension) {
        try {
            return extension.substring(0, extension.indexOf('?'));
        } catch (StringIndexOutOfBoundsException e) {
            return extension;
        }
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
