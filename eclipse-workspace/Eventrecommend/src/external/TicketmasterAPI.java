package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Entity.Item;
import Entity.Item.ItemBuilder;


public class TicketmasterAPI {
	//send HTTP request to TicketMaster API and get response, add some constants
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final int DEFAULT_RADIUS = 100;
	private static final String API_KEY = "dGDqAGRo6Gd3CmOeg79BMDZqHkJSAuOv";
	
	/**
	 * Helper methods
	 */

	//  {
	//    "name": "laioffer",
              //    "id": "12345",
              //    "url": "www.laioffer.com",
	//    ...
	//    "_embedded": {
	//	    "venues": [
	//	        {
	//		        "address": {
	//		           "line1": "101 First St,",
	//		           "line2": "Suite 101",
	//		           "line3": "...",
	//		        },
	//		        "city": {
	//		        	"name": "San Francisco"
	//		        }
	//		        ...
	//	        },
	//	        ...
	//	    ]
	//    }
	//    ...
	//  }
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				
				for(int i = 0; i < venues.length(); i++){
					JSONObject venue = venues.getJSONObject(i);
					
					//get address by concatenating the 3-line string
					StringBuilder adr = new StringBuilder();
					
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						
						if (!address.isNull("line1")) {
							adr.append(address.getString("line1")+"\n");
						}
						if (!address.isNull("line2")) {
							adr.append(address.getString("line2")+"\n");
						}
						if (!address.isNull("line3")) {
							adr.append(address.getString("line3")+"\n");
						}

					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						
						if (!city.isNull("name")) {
							adr.append(city.getString("name")+"\n");
						}
					}
					if (!adr.toString().equals("")) {
						return adr.toString();
					}
				}
				
			}
		}
		return "";
	}


	// {"images": [{"url": "www.example.com/my_image.jpg"}, ...]}
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray images = event.getJSONArray("images");
			
			for (int i = 0; i < images.length(); i++){
				JSONObject image = images.getJSONObject(i);
				
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	// {"classifications" : [{"segment": {"name": "music"}}, ...]}
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			
			for(int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
		
	}

	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();

		for (int i = 0; i < events.length(); ++i) {
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			
			/*private String itemId;
			private String name;
			private double rating;
			private String address;
			private Set<String> categories;
			private String imageUrl;
			private String url;
			private double distance;*/
			
			//JSONObject.isNull("key")
			// name,id,url,rating,distance is under the value of key "_embedded n"
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			
			builder.setCategories(getCategories(event));
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}

		return itemList;
	}

	
	// main function
	// geolocation is necessary, keyword is not.
	public List<Item> search(double lat, double lon, String keyword, int radius) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		if (radius == 0) {
			radius = DEFAULT_RADIUS;
		}
		
		// 1.there may be special character, or wrong format, need to encode in Byte or throw exception
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		// 2. get complete URL
		// transfer the lat & lon to geopoint
		String geohash = GeoHash.encodeGeohash(lat, lon, 8);
		//create query
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geohash, keyword, radius);
		try {
			// 3. build connection
			//create Connection object
			HttpURLConnection connect = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			int responsecode = connect.getResponseCode();
			
			System.out.println("\nSending 'GET' request to URL:" + URL + "?" + query);
			System.out.println("Resonse code:" + responsecode);
			
			// Need to do: deal with the response code: if responsecode == 400 or 200, etc
			
			// 4. Now read response body to get events data to be returned
			
			// BufferedReader read line by line
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream())); 
			String inputline;
			StringBuilder response = new StringBuilder();
			while ((inputline = in.readLine()) != null) {
				response.append(inputline);
			}
			//Close the BufferedReader after reading the inputstream/response data.
			in.close();
			
			//convert response body(StringBuilder) to JSON object
			JSONObject responseJson = new JSONObject(response.toString());
			
			// Extract events array only
			//check if the content in "_embedded" is exist
			if (responseJson.isNull("_embedded")) {
				return new ArrayList<Item>();
			}
			
			//get the info which we need in our project 
			// structure: _embedded----events(Jsonarray)
			JSONObject embedded = responseJson.getJSONObject("_embedded");
			JSONArray events = embedded.getJSONArray("events");
			return getItemList(events);
			
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<Item>();
	}
	
	//helper function
	// 1. print the result getting from Ticketmaster API for debugging
	//    need GeoPoint, So need Geohash to get it
	private void printAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null, 50);
		try {
			for(Item event : events) {
				System.out.println(event.toJSONObject());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main entry for sample TicketMaster API requests.
	 */
	public static void main(String[] args) {
		TicketmasterAPI tmApi = new TicketmasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.printAPI(29.682684, -95.295410);
	}



}
