import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JSONify {
	
	private static final String[] fieldNames = {"containsNuts", "containsDairy"};
	private static ArrayList<Integer> data = new ArrayList<Integer>();
	
	//produce json from a result set
	public static String toJSON(ResultSet rs) throws SQLException {
		//read in all of the data from the mySQL database
		while (rs.next()) {
			for (int i = 0; i < fieldNames.length; i++) {
				data.add(rs.getInt(fieldNames[i]));
			}
		}
		
		for (int i = 0; i < data.size(); i++) {
			System.out.println(fieldNames[i] + ": " + data.get(i));
		}
		//generate the output string
		String out;
		if (!data.isEmpty()) {
			out = "{";
			//loop through each element and add it to the string 
			for (int j = 0; j < fieldNames.length; j++) {
				//add the field name and data
				out += "\"" + fieldNames[j] + "\": " + data.get(j);
				//if there is another element add a comma
				if (j < fieldNames.length - 1) {
					out += ", ";
				}
			}
			out += "}";
		} else {
			//if there is no information just return "unknown"
			out = "unknown";
		}
		return out;
	}
	
	//produce an int array from a json string
	public static int[] fromJSON(String json) {
		//declare an integer array with an element for each field
		int[] data = new int[fieldNames.length];
		
		//loop through each field
		for (int i = 0; i < fieldNames.length; i++) {
			//get the substring of the json that is relevant
			int index1 = json.indexOf(fieldNames[i]) + fieldNames[i].length() + 2;
			String subStr = json.substring(index1, index1+2);
			//remove the extra space " 1" if it isn't negative
			if (!subStr.contains("-")) {
				subStr = subStr.substring(1);
			}
			//parse the string to an integer
			data[i] = Integer.parseInt(subStr);
		}
		return data;
	}
}
