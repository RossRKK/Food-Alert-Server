package server;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JSONify {
	public static int minSep;
	
	private static final String delimiter = "&";

	//private static ArrayList<Integer> data = new ArrayList<Integer>();

	// produce json from a result set
	public static String toJSON(ResultSet rs) throws SQLException {
		ArrayList<Integer> data = new ArrayList<Integer>();
		String name = "";
		// read in all of the data from the mySQL database
		while (rs.next()) {
			name = rs.getString("name");
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
		String out = "{";
		if (name != null) {
			out += "\"name\": \"" + name + "\", ";
		}
		if (!data.isEmpty()) {
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
	
	public static Record decode(String extension) {
		if (extension.indexOf("?") == -1) {
			return null;
		}
		Record r = new Record();
		// declare an integer array with an element for each field
		int[] data = new int[DatabaseManager.fieldNames.length];

		// loop through each field
		for (int i = 0; i < DatabaseManager.fieldNames.length; i++) {
			// get the substring of the json that is relevant		
			String subStr = getValue(DatabaseManager.fieldNames[i], extension);
			
			data[i] = Integer.parseInt(subStr);
		}	
		r.setName(getValue("name", extension));
		
		r.setData(data);
		
		return r;
	}
	
	public static String getValue(String fieldName, String extension) {
		int beginIndex = extension.indexOf(fieldName) + fieldName.length() + 1;
		
		int endIndex = extension.indexOf(delimiter, beginIndex);
		
		if (endIndex == -1) {
			endIndex = extension.length();
		}
		
		return extension.substring(beginIndex, endIndex);
	}
}
