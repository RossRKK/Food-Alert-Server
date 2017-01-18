package server.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import server.database.DatabaseManager;
import server.database.Record;
import server.io.ConfigLoader;
import server.io.TESCOManager;

public class JSONify {
    public static double minSep;

    private static final String delimiter = "&";

    /**
     * Produce JSON from an SQL result set
     * 
     * @param rs
     *            The result set to be used
     * @return JSON on that result set
     * @throws SQLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static String toJSON(ResultSet rs, String ean) throws SQLException, IOException, ClassNotFoundException {
        ArrayList<Integer> data = new ArrayList<Integer>();
        String name = "";
        // read in all of the data from the mySQL database
        while (rs.next()) {
            // find special cases
            name = rs.getString(DatabaseManager.nameFieldName);

            // find tertiaryFields
            for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
                int contains = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "C");
                int trace = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "T");
                int none = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "N");

                int code = DatabaseManager.UNKNOWN;
                // determine which code to use
                if (contains > trace && contains > none) {
                    code = DatabaseManager.CONTAINS;
                } else if (trace > contains && trace > none) {
                    code = DatabaseManager.TRACE;
                } else if (none > contains && none > trace) {
                    code = DatabaseManager.NONE;
                }

                data.add(code);
            }

            // find binaryFields
            for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
                int contains = rs.getInt(DatabaseManager.binaryFieldNameBases[i] + "C");
                int none = rs.getInt(DatabaseManager.binaryFieldNameBases[i] + "N");

                int code = DatabaseManager.UNKNOWN;
                // determine which code to use
                if (contains - none > minSep) {
                    code = DatabaseManager.CONTAINS;
                } else if (none - contains > minSep) {
                    code = DatabaseManager.NONE;
                }

                data.add(code);
            }

            // find contiuous fields
            for (int i = 0; i < DatabaseManager.continuousFieldNames.length; i++) {
                data.add(rs.getInt(DatabaseManager.continuousFieldNames[i]));
            }
        }

        boolean reconfirm = shouldReconfirm(rs, data);

        // ask tesco if we don;t know
        if (data.isEmpty()) {
            Record r = TESCOManager.askTesco(ean);
            if (r != null) {
                name = r.getName();
                int maxLength = 80;
                if (name.length() >= maxLength) {
                    name = name.substring(0, maxLength - 1);
                }

                for (int i = 0; i < r.getData().length; i++) {
                    data.add(new Integer(r.getData()[i]));
                }

                // add it ot the database
                DatabaseManager dbm = new DatabaseManager(ConfigLoader.getUrl(), ConfigLoader.getUser(), ConfigLoader.getPass());
                dbm.add(ean, name, r.getData());
                // override the reconfirm flag
                reconfirm = true;
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
                // if (j < DatabaseManager.fieldNames.length - 1) {
                out += ", ";
                // }
            }
            out += "reconfirm: \"" + reconfirm + "\"}";
        } else {
            // if the data is empty send unkown codes
            // loop through each element and add it to the string
            for (int j = 0; j < DatabaseManager.fieldNames.length; j++) {
                // add the field name and data
                out += "\"" + DatabaseManager.fieldNames[j] + "\": " + DatabaseManager.UNKNOWN;
                // if there is another element add a comma
                // if (j < DatabaseManager.fieldNames.length - 1) {
                out += ", ";
                // }
            }
            out += "reconfirm: \"true\"}";
        }
        return out;
    }

    /**
     * Determines whether the reconfirm flag should be true or false
     * 
     * @param rs
     *            the SQL result set to be checked
     * @return Whether the data should be reconfirmed
     * @throws SQLException
     */
    public static boolean shouldReconfirm(ResultSet rs, ArrayList<Integer> data) throws SQLException {
        rs.beforeFirst();
        while (rs.next()) {
            int index = 0;
            for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
                int contains = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "C");
                int trace = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "T");
                int none = rs.getInt(DatabaseManager.tertiaryFieldNameBases[i] + "N");

                if (data.get(index).intValue() == DatabaseManager.CONTAINS) {
                    int nextBig = trace > none ? trace : none;
                    double minSeperation = minSep * contains;
                    if (contains - nextBig <= Math.ceil(minSeperation)) {
                        return true;
                    }
                } else if (data.get(index).intValue() == DatabaseManager.TRACE) {
                    int nextBig = contains > none ? contains : none;
                    double minSeperation = minSep * trace;
                    if (trace - nextBig <= Math.ceil(minSeperation)) {
                        return true;
                    }
                } else if (data.get(index).intValue() == DatabaseManager.NONE) {
                    int nextBig = contains > trace ? contains : trace;
                    double minSeperation = minSep * none;
                    if (none - nextBig <= Math.ceil(minSeperation)) {
                        return true;
                    }
                } else {
                    return true;
                }

                index++;
            }

            for (int i = 0; i < DatabaseManager.binaryFieldNameBases.length; i++) {
                int contains = rs.getInt(DatabaseManager.binaryFieldNameBases[i] + "C");
                int none = rs.getInt(DatabaseManager.binaryFieldNameBases[i] + "N");

                if (data.get(index).intValue() == DatabaseManager.CONTAINS) {
                    double minSeperation = minSep * contains;
                    if (contains - none <= Math.ceil(minSeperation)) {
                        return true;
                    }
                } else if (data.get(index).intValue() == DatabaseManager.NONE) {
                    double minSeperation = minSep * none;
                    if (none - contains <= Math.ceil(minSeperation)) {
                        return true;
                    }
                } else {
                    return true;
                }

                index++;
            }
        }
        return false;
    }

    /**
     * Decode a url extension
     * 
     * @param extension
     *            The extension to be decoded
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
     * 
     * @param fieldName
     *            The name of the field
     * @param extension
     *            The url extension to be searched
     * @return The value of the field as a String
     */
    public static String getValue(String fieldName, String extension) {
        int beginIndex = extension.indexOf(fieldName) + fieldName.length() + 1;

        int endIndex = extension.indexOf(delimiter, beginIndex);

        if (endIndex == -1) {
            endIndex = extension.length();
        }

        String out = extension.substring(beginIndex, endIndex);

        // if the field isn't present set to unknown
        if (extension.indexOf(fieldName) == -1) {
            out = "" + DatabaseManager.UNKNOWN;
        }

        return out;
    }
}
