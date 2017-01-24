package server.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import server.database.DatabaseManager;
import server.util.JSONify;

public class ConfigLoader {
    private static String minSepStr;
    private static String portStr;

    private static String containsStr;
    private static String traceStr;
    private static String noneStr;
    private static String unknownStr;

    private static String url;
    private static String user;
    private static String pass;

    private static String foodTableName;
    private static String dbName;
    private static String tertiaryFieldNames;
    private static String binaryFieldNames;
    private static String continuousFieldNames;
    private static String tescoUrl;
    private static String tescoHost;
    private static String key;

    private static String[][] allergenNames;

    /**
     * Loads the config and sets the values in the right place
     * 
     * @throws IOException
     */
    public static void loadConfig() throws IOException {
        // the properties object
        Properties prop = new Properties();
        // the config file name
        String propFileName = "config.properties";

        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }

        // load all of the proprties and set relevant variables
        tertiaryFieldNames = prop.getProperty("tertiaryFieldNames");
        binaryFieldNames = prop.getProperty("binaryFieldNames");
        continuousFieldNames = prop.getProperty("continuousFieldNames");

        minSepStr = prop.getProperty("minVoteSeperation");

        containsStr = prop.getProperty("contains");
        traceStr = prop.getProperty("trace");
        noneStr = prop.getProperty("none");
        unknownStr = prop.getProperty("unknown");

        url = prop.getProperty("url");
        user = prop.getProperty("user");
        pass = prop.getProperty("pass");

        dbName = prop.getProperty("dbName");
        foodTableName = prop.getProperty("foodTableName");

        tescoUrl = prop.getProperty("tescoUrl");
        tescoHost = prop.getProperty("tescoHost");
        key = prop.getProperty("key");

        String[] tertiaryBases;
        if (tertiaryFieldNames.length() > 0) {
            tertiaryBases = tertiaryFieldNames.split(",");
        } else {
            tertiaryBases = new String[0];
        }

        String[] binaryBases;
        if (binaryFieldNames.length() > 0) {
            binaryBases = binaryFieldNames.split(",");
        } else {
            binaryBases = new String[0];
        }

        String[] continuousBases;
        if (continuousFieldNames.length() > 0) {
            continuousBases = continuousFieldNames.split(",");
        } else {
            continuousBases = new String[0];
        }

        DatabaseManager.setFieldBases(tertiaryBases, binaryBases, continuousBases);

        allergenNames = new String[tertiaryBases.length + binaryBases.length][];
        // find other names
        int index = 0;
        for (int i = 0; i < tertiaryBases.length; i++) {
            allergenNames[index] = prop.getProperty(tertiaryBases[i] + "Alts").split(",");
            index++;
        }
        
        for (int i = 0; i < binaryBases.length; i++) {
            allergenNames[index] = prop.getProperty(binaryBases[i] + "Alts").split(",");
            index++;
        }
        TESCOManager.setAllergenNames(allergenNames);

        // close the input stream
        inputStream.close();

        Request.setUrl(url);
        Request.setUser(user);
        Request.setPass(pass);

        double minSep = Double.parseDouble(minSepStr);
        JSONify.minSep = minSep;

        DatabaseManager.CONTAINS = Integer.parseInt(containsStr);
        DatabaseManager.TRACE = Integer.parseInt(traceStr);
        DatabaseManager.NONE = Integer.parseInt(noneStr);
        DatabaseManager.UNKNOWN = Integer.parseInt(unknownStr);
    }

    public static String getMinSepStr() {
        return minSepStr;
    }

    public static String getPortStr() {
        return portStr;
    }

    public static String getContainsStr() {
        return containsStr;
    }

    public static String getTraceStr() {
        return traceStr;
    }

    public static String getNoneStr() {
        return noneStr;
    }

    public static String getUnknownStr() {
        return unknownStr;
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPass() {
        return pass;
    }

    public static String getFoodTableName() {
        return foodTableName;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getTescoUrl() {
        return tescoUrl;
    }

    public static String getTescoHost() {
        return tescoHost;
    }

    public static String getKey() {
        return key;
    }
}
