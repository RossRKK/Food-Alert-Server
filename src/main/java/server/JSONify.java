package server;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class JSONify {
	public static int minSep;

	//private static ArrayList<Integer> data = new ArrayList<Integer>();

	// produce json from a result set
	public static String toJSON(ResultSet rs) throws SQLException {
		ArrayList<Integer> data = new ArrayList<Integer>();
		// read in all of the data from the mySQL database
		while (rs.next()) {
			for (int i = 0; i < DatabaseManager.fieldBases.length; i++) {
				int contains = rs.getInt(DatabaseManager.fieldBases[i] + "C");
				int trace = rs.getInt(DatabaseManager.fieldBases[i] + "T");
				int none = rs.getInt(DatabaseManager.fieldBases[i] + "N");
				
				int code = DatabaseManager.UNKNOWN;
				//determine which code to use
				if (contains - trace > minSep) {
					code = DatabaseManager.CONTAINS;
				} else if (contains - none > minSep) {
					code = DatabaseManager.CONTAINS;
				} else if (trace - contains > minSep) {
					code = DatabaseManager.TRACE;
				} else if (trace - none > minSep) {
					code = DatabaseManager.TRACE;
				} else if (none - contains > minSep) {
					code = DatabaseManager.NONE;
				} else if (none - trace > minSep) {
					code = DatabaseManager.NONE;
				}
				
				data.add(code);
			}
		}

		// generate the output string
		String out;
		if (!data.isEmpty()) {
			out = "{";
			// loop through each element and add it to the string
			for (int j = 0; j < DatabaseManager.fieldNames.length; j++) {
				// add the field name and data
				out += "\"" + DatabaseManager.fieldNames[j] + "\": " + data.get(j);
				// if there is another element add a comma
				if (j < DatabaseManager.fieldNames.length - 1) {
					out += ", ";
				}
			}
			out += "}";
		} else {
			//if the data is empty send unkown codes
			out = "{";
			// loop through each element and add it to the string
			for (int j = 0; j < DatabaseManager.fieldNames.length; j++) {
				// add the field name and data
				out += "\"" + DatabaseManager.fieldNames[j] + "\": " + DatabaseManager.UNKNOWN;
				// if there is another element add a comma
				if (j < DatabaseManager.fieldNames.length - 1) {
					out += ", ";
				}
			}
			out += "}";
		}
		return out;
	}

	// produce an int array from a json string
	public static int[] fromJSON(String json) {
		// declare an integer array with an element for each field
		int[] data = new int[DatabaseManager.fieldNames.length];

		// loop through each field
		for (int i = 0; i < DatabaseManager.fieldNames.length; i++) {
			// get the substring of the json that is relevant

			/*int index = json.indexOf(DatabaseManager.fieldNames[i]) + DatabaseManager.fieldNames[i].length() + 2;
			String subStr = json.substring(index, index + 1);
			if (subStr.contains("-")) {
				subStr = json.substring(index, index + 2);
			}*/
			int index = json.indexOf(DatabaseManager.fieldNames[i]) + DatabaseManager.fieldNames[i].length();
			
			String s = json.substring(index, json.indexOf(",", index));
			
			Scanner sc = new Scanner(s);
			// parse the string to an integer
			int curData = DatabaseManager.UNKNOWN;
			while (sc.hasNext()) {
				try {
					curData = sc.nextInt();
					break;
				} catch (InputMismatchException e) {
					
				}
			}
			data[i] = curData;
			sc.close();
		}
		
		return data;
	}
}
