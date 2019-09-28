package recommendalgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Entity.Item;
import db.DBConnection;
import db.DBConnectionFactory;

//Recommendation based on geo distance and similar categories.
public class GeoRecommendation {
	public List<Item> recommendItems(String user_id, double lat, double lon){
		
		List<Item> recommendItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getConnection();
		
		//Step 1: get all favorite events
		Set<String> favorItemids = conn.getFavoriteItemIds(user_id);
		
		//Step 2: get all categories of all favorite items, sort by count
		Map<String,Integer> recommendCategories = new HashMap<>();
		
		for (String id : favorItemids) {
			Set<String> cats = conn.getCategories(id);
			for (String category: cats) {
				if (!recommendCategories.containsKey(category)) {
					recommendCategories.put(category, 1);
				}else {
					recommendCategories.put(category, recommendCategories.get(category) + 1);
				}
			}
		}
		
		//sorting
		List<Entry<String, Integer>> sortedCategories = new ArrayList<Entry<String, Integer>>(recommendCategories.entrySet());
		Collections.sort(sortedCategories, new Comparator<Entry<String, Integer>>(){
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				if (o1.getValue().equals(o2.getValue())) {
					return 0;
				}
				return o1.getValue() > o2.getValue() ? -1: 1;
			}
		});
		
		
		//Step 3: do search based on category,filter out favorited events, sort by distance
		Set<Item> visitedItems = new HashSet<>();
		List<Item> filteredItems = new ArrayList<>();
		int radius_default = 50;
		for(Entry<String,Integer> category : sortedCategories) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey(), radius_default);
			for (Item item : items) {
				//filtering
				if (!favorItemids.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			
			//sort by distance
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item o1, Item o2) {
					if (o1.getDistance() == o2.getDistance()) {
						return 0;
					}
					return o1.getDistance() < o2.getDistance()? -1: 1;
				}
			});
			
			visitedItems.addAll(items);
			recommendItems.addAll(filteredItems);
		}
		return recommendItems;
		

		
	}
}
