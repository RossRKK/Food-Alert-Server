package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TESCOManager {
	private static String ingredientsFieldName = "\"ingredients\"";
	private static String nameFieldName = "\"description\"";
	private static String allergyFieldName = "\"allergenText\"";
	private static String vegetarianKeyWord = "vegetarian";
	
	private static String[][] allergenNames;
	private static String vegetarianFieldName = "isVegetarian";
	
	public static void setAllergenNames(String[][]names) {
		allergenNames = names;
	}
	
	public static void main(String[] args) {
		try {
			ConfigLoader.loadConfig();
			String ean = "5034660021667";
			Record r = askTesco(ean);
			System.out.println("Name: " + r.getName());
			
			for (int i = 0; i < DatabaseManager.fieldNames.length; i++) {
				System.out.println(DatabaseManager.fieldNames[i] + ": " + r.getData()[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Record askTesco(String ean) throws IOException {
		String json = sendGet(ean);
		String ingredients = getField(json, ingredientsFieldName, '[', ']');
		String allergyAdvice = getField(json, allergyFieldName, '\"', '\"');
		int[] data = findAllergens(ingredients, allergyAdvice);
		
		int index = -1;
		if (json.toLowerCase().contains(vegetarianKeyWord)) {
			for (int i = 0; i < DatabaseManager.fieldNames.length; i++) {
				if (DatabaseManager.fieldNames[i].equalsIgnoreCase(vegetarianFieldName)) {
					index = i;
					break;
				}
			}
		}
		if (index != -1) {
			data[index] = DatabaseManager.NONE;
		} else {
			data[index] = DatabaseManager.CONTAINS;
		}
		
		Record r = new Record();
		r.setData(data);
		r.setName(getField(json, nameFieldName, '\"', '\"'));
		
		return r;
	}
	
	private static String getField(String json, String field, char startChar, char endChar) {
		int startIndex = json.indexOf(field) + field.length();
		//find the start of the quote
		int begin = json.indexOf(startChar, startIndex) + 1;
		int end = json.indexOf(endChar, begin);
		
		String fieldContent = json.substring(begin, end);
		
		return fieldContent;
	}

	private static int[] findAllergens(String ingredients, String allergyAdvice) {
		ingredients = ingredients.toLowerCase();
		allergyAdvice = allergyAdvice.toLowerCase();
		int[] data = new int[DatabaseManager.fieldNames.length];
		
		for (int i = 0; i < DatabaseManager.tertiaryFieldNameBases.length; i++) {
			boolean allergen = false;
			//search for allergen name
			for (int j = 0; j < allergenNames[i].length; j++) {
				if (ingredients.contains(allergenNames[i][j])) {
					allergen = true;
					break;
				}
			}
			if (allergen) {
				data[i] = DatabaseManager.CONTAINS;
			} else {
				//determine whether there are traces of an ingredient
				boolean trace = false;
				for (int j = 0; j < allergenNames[i].length; j++) {
					if (allergyAdvice.contains(allergenNames[i][j])) {
						trace = true;
						break;
					}
				}
				if (trace) {
					data[i] = DatabaseManager.TRACE;
				} else {
					data[i] = DatabaseManager.NONE;
				}
			}
		}
		
		return data;
	}
	
	private static String sendGet(String ean) throws IOException {
		String url = ConfigLoader.getTescoUrl() + "?gtin=" + ean;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("Host", ConfigLoader.getTescoHost());
		con.setRequestProperty("Ocp-Apim-Subscription-Key", ConfigLoader.getKey());

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		return response.toString();

	}
}
