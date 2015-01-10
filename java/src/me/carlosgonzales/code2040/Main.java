package me.carlosgonzales.code2040;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
	private static String baseURL = "http://challenge.code2040.org/api/";
	private static String token;;
	private static String accessPoint;

	public static void main(String[] args) {
		doRegistration();
		stage1();
		stage2();
		stage3();
		stage4();
	}

	private static void doRegistration() {
		System.out.println("****Registration");
		accessPoint = "register";
		JSONObject obj = new JSONObject().put("email", "cnexus0@gmail.com")
				.put("github",
						"https://github.com/cnexus/CODE2040_API_challenge");

		String line = getResult(obj);

		// Save registration token
		token = new JSONObject(line).getString("result");
		System.out.println(line);
	}

	private static void stage1() {
		// Part 1: Get string to reverse
		accessPoint = "getstring";
		JSONObject obj = getJson();

		System.out.println();
		System.out.println("****Stage 1");
		String line = getResult(obj);
		System.out.println(line);
		String data = new JSONObject(line).getString("result");

		// Part 2: Reverse the string and POST to server
		accessPoint = "validatestring";
		String reverse = new StringBuilder(data).reverse().toString();
		obj = getJson().put("string", reverse);

		System.out.println(getResult(obj));
	}

	private static void stage2() {
		System.out.println();
		System.out.println("****Stage 2");

		// Get data
		accessPoint = "haystack";
		JSONObject obj = getJson();

		String result = getResult(obj);
		System.out.println(result);

		JSONObject rootObject = new JSONObject(result.toString());
		rootObject = rootObject.getJSONObject("result");

		// Parse response from server

		JSONArray haystack = rootObject.getJSONArray("haystack");
		String needle = rootObject.getString("needle");

		// Convert haystack to array

		ArrayList<String> list = new ArrayList<String>();
		if (haystack != null) {
			int len = haystack.length();
			for (int i = 0; i < len; i++) {
				list.add(haystack.get(i).toString());
			}
		}

		int index = -1;

		// Search for needle in haystack

		for (int i = 0; i < list.size(); i++) {
			if (needle.equals(list.get(i)))
				index = i;
		}

		accessPoint = "validateneedle";
		obj = getJson().put("needle", index);
		System.out.println(getResult(obj));

	}

	private static void stage3() {
		System.out.println();
		System.out.println("****Stage 3");
		accessPoint = "prefix";
		String result = getResult(getJson());
		System.out.println(result);

		JSONObject rootObject = new JSONObject(result.toString());
		rootObject = rootObject.getJSONObject("result");

		// Parse response from server

		JSONArray array = rootObject.getJSONArray("array");
		String prefix = rootObject.getString("prefix");

		ArrayList<String> list = new ArrayList<String>();
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				String item = array.get(i).toString();
				if (!item.startsWith(prefix))
					list.add(item);
			}
		}

		JSONArray newArray = new JSONArray(list);
		accessPoint = "validateprefix";
		JSONObject o = getJson().put("array", newArray);
		System.out.println(getResult(o));
	}

	private static void stage4() {
		accessPoint = "time";
		System.out.println();
		System.out.println("****Stage 4");
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		iso.setTimeZone(tz);
		JSONObject result = new JSONObject(getResult(getJson()));
		System.out.println(result);
		result = result.getJSONObject("result");

		String datestamp = result.getString("datestamp");
		// Hotfix for Java not being able to handle more than 3 decimal places
		datestamp = datestamp.substring(0, datestamp.lastIndexOf(".")).trim();
		int seconds = result.getInt("interval");
		try {
			Date date = iso.parse(datestamp);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.SECOND, seconds);
			String time = cal.getTime().toString();

			accessPoint = "validatetime";
			JSONObject obj = getJson().put("datestamp", time);
			System.out.println(getResult(obj));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * 
	 * Utility functions
	 * 
	 * 
	 * 
	 */

	private static JSONObject getJson() {
		return new JSONObject().put("token", token);
	}

	private static String getResult(JSONObject obj) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				executePost(obj)));
		StringBuffer buffer = new StringBuffer();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			reader.close();
			return buffer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	private static InputStream executePost(JSONObject obj) {
		try {
			if (accessPoint.isEmpty())
				throw new RuntimeException("Did not initialize accessPoint");

			URL url = new URL(baseURL + accessPoint);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());

			out.write(obj.toString());
			out.close();

			// BufferedReader reader = new BufferedReader(new
			// InputStreamReader(connection.getInputStream()));
			//
			// String line = "";
			// try {
			// System.out.println("Response for JSON " + obj + " is:");
			// while((line = reader.readLine()) != null){
			// System.out.println(line);
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			return connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
