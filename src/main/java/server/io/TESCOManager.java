package server.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import server.database.DatabaseManager;
import server.database.Record;

public class TESCOManager {
    private static String ingredientsFieldName = "\"ingredients\"";
    private static String nameFieldName = "\"description\"";
    private static String allergyFieldName = "\"allergenText\"";
    private static String vegetarianKeyWord = "vegetarian";

    private static String[][] allergenNames;
    private static String vegetarianFieldName = "isVegetarian";

    /**
     * Sets the names the allergens will have when listed in ingredients
     * 
     * @param names
     *            The 2D array of names
     */
    public static void setAllergenNames(String[][] names) {
        allergenNames = names;
    }

    /**
     * Send a request to the tesco api and process the response
     * 
     * @param ean
     *            The barcode number of the product
     * @return A reocrd object containg the details of that product
     * @throws IOException
     */
    public static Record askTesco(String ean) throws IOException {
        try {
            // Get the raw data
            String json = sendGet(ean);
            // Get the list of ingredients
            String ingredients = getField(json, ingredientsFieldName, "[", "],");
            // Get the allelrgy adbice text
            String allergyAdvice = getField(json, allergyFieldName, "\"", "\"");
            // find the allergens in the ingredients and advice text
            int[] data = findAllergens(ingredients, allergyAdvice);
    
            // figure out which field is vegetarian
            int index = -1;
            for (int i = 0; i < DatabaseManager.fieldNames.length; i++) {
                if (DatabaseManager.fieldNames[i].equalsIgnoreCase(vegetarianFieldName)) {
                    index = i;
                    break;
                }
            }
            // if it contains "suitable for vegetarians" we can use that
            if (json.toLowerCase().contains(vegetarianKeyWord)) {
                data[index] = DatabaseManager.NONE;
            }
            // create a record oobject and return it
        
            Record r = new Record();
            r.setData(data);
            r.setName(getField(json, nameFieldName, "\"", "\""));

            return r;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Get a specific field from a string containing JSON
     * 
     * @param json
     *            The json strig
     * @param field
     *            The name of the field to be returned
     * @param startChar
     *            The charaacter that will encapsulate the field (usually ")
     * @param endChar
     *            The character that will end the field (usually ")
     * @return The value of the field as a String
     */
    private static String getField(String json, String field, String startChar, String endChar) {
        int startIndex = json.indexOf(field) + field.length();
        // find the start of the quote
        int begin = json.indexOf(startChar, startIndex) + 1;
        int end = json.indexOf(endChar, begin);

        String fieldContent = json.substring(begin, end);

        return fieldContent;
    }

    /**
     * Creates the data int[] by searching the ingredients list for allergens
     * 
     * @param ingredients
     *            The list of ingredients
     * @param allergyAdvice
     *            The bit that says "May contain traces of nuts due to
     *            maufactuaring methods"
     * @return The int[] representing the allergens that this contains
     */
    private static int[] findAllergens(String ingredients, String allergyAdvice) {
        // all strings are processsed in lower case so that case is irrelevant
        ingredients = ingredients.toLowerCase();
        allergyAdvice = allergyAdvice.toLowerCase();
        // a blank int array is created witht the correct length
        int[] data = new int[DatabaseManager.fieldNames.length];

        for (int i = 0; i < allergenNames.length; i++) {
            boolean allergen = false;
            // search for allergen name
            for (int j = 0; j < allergenNames[i].length; j++) {
                if (ingredients.contains(allergenNames[i][j])) {
                    allergen = true;
                    break;
                }
            }
            if (allergen) {
                data[i] = DatabaseManager.CONTAINS;
            } else {
                // determine whether there are traces of an ingredient
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

    /**
     * Send a get request to the tesco api
     * 
     * @param ean
     *            The barcide number of the product
     * @return The response as a string (Should be JSON)
     * @throws IOException
     */
    private static String sendGet(String ean) throws IOException {
        String url = ConfigLoader.getTescoUrl() + "?gtin=" + ean;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        // add request header
        con.setRequestProperty("Host", ConfigLoader.getTescoHost());
        con.setRequestProperty("Ocp-Apim-Subscription-Key", ConfigLoader.getKey());

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        return response.toString();

    }
}
