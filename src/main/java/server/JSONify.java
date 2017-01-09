package server;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JSONify {
	public static int minSep;
	
	private static final String delimiter = "&";

	/**
	 * Produce JSON from an SQL result set
	 * @param rs The result set to be used
	 * @return JSON on that result set
	 * @throws SQLException
	 */
	public static String toJSON(ResultSet rs) throws SQLException {
		ArrayList<Integer> data = new ArrayList<Integer>();
		String name = "";
		// read in all of the data from the mySQL database
		while (rs.next()) {
			//find special cases
			name = rs.getString(DatabaseManager.nameFieldName);
			
			//find tertiaryFields
			for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
				int contains = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "C");
				int trace = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "T");
				int none = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "N");
				
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
			
			//find binaryFields
			for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
				int contains = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "C");
				int none = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "N");
				
				int code = DatabaseManager.UNKNOWN;
				//determine which code to use
				if (contains - none > minSep) {
					code = DatabaseManager.CONTAINS;
				} else if (none - contains > minSep) {
					code = DatabaseManager.NONE;
				}
				
				data.add(code);
			}
			
			//find contiuous fields
			for (int i = 0; i < DatabaseManager.continuousFieldNames.length; i++) {
				data.add(rs.getInt(DatabaseManager.continuousFieldNames[i]));
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
	
	/**
	 * Decode a url extension
	 * @param extension The extension to be decoded
	 * @return A record object of the decoded url extension
	 */
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
		r.setName(getValue(DatabaseManager.nameFieldName, extension));
		
		r.setData(data);
		
		return r;
	}
	
	/**
	 * Get the value of a field from the url extension
	 * @param fieldName The name of the field
	 * @param extension The url extension to be searched
	 * @return The value of the field as a String
	 */
	public static String getValue(String fieldName, String extension) {
		
		int beginIndex = extension.indexOf(fieldName) + fieldName.length() + 1;
		
		int endIndex = extension.indexOf(delimiter, beginIndex);
		
		if (endIndex == -1) {
			endIndex = extension.length();
		}
		
		String out = extension.substring(beginIndex, endIndex);
		
		//if the field isn't present set to unknown
		if (extension.indexOf(fieldName) == -1) {
			out = "" + DatabaseManager.UNKNOWN;
		}
		
		return out;
	}
}
