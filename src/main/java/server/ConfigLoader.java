package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

	public static void loadConfig() throws IOException {
		Properties prop = new Properties();
		String propFileName = "config.properties";

		InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		// get the property value and print it out
		tertiaryFieldNames = prop.getProperty("tertiaryFieldNames");
		binaryFieldNames = prop.getProperty("binaryFieldNames");
		continuousFieldNames = prop.getProperty("continuousFieldNames");

		minSepStr = prop.getProperty("minVoteSeperation");
		portStr = prop.getProperty("port");

		containsStr = prop.getProperty("contains");
		traceStr = prop.getProperty("trace");
		noneStr = prop.getProperty("none");
		unknownStr = prop.getProperty("unknown");

		url = prop.getProperty("url");
		user = prop.getProperty("user");
		pass = prop.getProperty("pass");

		dbName = prop.getProperty("dbName");
		foodTableName = prop.getProperty("foodTableName");

		inputStream.close();
		setValues();
	}

	private static void setValues() {

		// int port = Integer.parseInt(System.getenv("PORT"));
		// System.out.println(port);
		// Main.setPort(port);

		Request.setUrl(url);
		Request.setUser(user);
		Request.setPass(pass);

		double minSep = Double.parseDouble(minSepStr);
		JSONify.minSep = minSep;

		DatabaseManager.CONTAINS = Integer.parseInt(containsStr);
		DatabaseManager.TRACE = Integer.parseInt(traceStr);
		DatabaseManager.NONE = Integer.parseInt(noneStr);
		DatabaseManager.UNKNOWN = Integer.parseInt(unknownStr);

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
}
