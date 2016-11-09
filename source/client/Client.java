package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Client {
	public static void main(String[] args) throws Exception {
		get();
		post();
		get();
	}
	
	public static void get() throws Exception {
		String url = "http://localhost:8080/8001110016303";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		//con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
	}
	
	public static void post() throws Exception {
		String url = "http://localhost:8080/8001110016303";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		//con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);
		//con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "{\"containsNuts\": 1, \"containsDairy\": 1}";

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeUTF("POST /8001110016303 HTTP/1.1");
		wr.writeUTF("\n\r");
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		System.out.println("Posted");
	}
}
