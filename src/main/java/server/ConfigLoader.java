package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
	private static String fieldNames;
	private static String minSepStr;
	private static String portStr;
	
	private static String containsStr;
	private static String traceStr;
	private static String noneStr;
	private static String unknownStr;
 
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
		fieldNames = prop.getProperty("fieldNames");
		minSepStr = prop.getProperty("minVoteSeperation");
		portStr = prop.getProperty("port");

		containsStr = prop.getProperty("contains");
		traceStr = prop.getProperty("trace");
		noneStr = prop.getProperty("none");
		unknownStr = prop.getProperty("unknown");
		
		Request.setUrl(prop.getProperty("url"));
		Request.setUser(prop.getProperty("user"));
		Request.setPass(prop.getProperty("pass"));
		
		inputStream.close();
		setValues();
	}
	
	private static void setValues() {
		int port = Integer.parseInt(System.getenv("PORT"));
		System.out.println(port);
		Main.setPort(port);
		
		int minSep = Integer.parseInt(minSepStr);
		JSONify.minSep = minSep;
		
		DatabaseManager.CONTAINS = Integer.parseInt(containsStr);
		DatabaseManager.TRACE = Integer.parseInt(traceStr);
		DatabaseManager.NONE = Integer.parseInt(noneStr);
		DatabaseManager.UNKNOWN = Integer.parseInt(unknownStr);
		
		String[] bases = fieldNames.split(",");
		
		DatabaseManager.setFieldBases(bases);
	}
}
