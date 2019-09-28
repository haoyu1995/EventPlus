package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Entity.Item;
import Entity.Item.ItemBuilder;
import db.DBConnection;
import db.DBConnectionFactory;
import external.TicketmasterAPI;

public class MySQLDBConnection implements DBConnection {
	private Connection conn;
	//constructor
	public MySQLDBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (conn != null) {
			try {
				conn.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	
	/*
	 * Change the favorite item History
	 * */
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history(user_id,item_id) VALUES (?,?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for(String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
	
		}catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override //可以帮助检查重写的函数
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return;
		}
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for(String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
	
		}catch (SQLException e) {
			e.printStackTrace();
		}


	}

	/*
	 * read from favorite item History
	 * */
	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return new HashSet<String>();
		}
		Set<String> fItemIds = new HashSet<>();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet res = stmt.executeQuery();
			while(res.next()) {
				String id = res.getString("item_id");
				fItemIds.add(id);
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return fItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, itemId);
				ResultSet res = stmt.executeQuery();
				// use iterator read from ResultSet
				
				ItemBuilder item = new ItemBuilder();
				
				while (res.next()) {
					item.setItemId(res.getString("item_id"));
					item.setName(res.getString("name"));
					item.setAddress(res.getString("address"));
					item.setUrl(res.getString("url"));
					item.setDistance(res.getDouble("distance"));
					item.setImageUrl(res.getString("image_url"));
					item.setRating(res.getDouble("rating"));
					//category not in ResultSet of items table
					item.setCategories(getCategories(itemId));
				}
				favoriteItems.add(item.build());
			}
			
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<String> categories = new HashSet<String>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				String cat = res.getString("category");
				categories.add(cat);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term, int radius) {
		// TODO Auto-generated method stub
		TicketmasterAPI tmAPI = new TicketmasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term, radius);
//		System.out.println(items.size());
		//save item
		for (Item item: items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		if (conn == null) {
			// show alert on browser
			System.err.println("DB connection failed!");
			return;
		}
		try {
			// String sql = "INSERT IGNORE INTO items VALUES ("item.getItemId()+","+item.getName()+","+item.getRating()")";
			// PreparedStatement is easy to add sql
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.executeUpdate();
			
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			stmt = conn.prepareStatement(sql);
			for(String category: item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				boolean success = stmt.execute();
//				System.out.println(success);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}


	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		String fullname = "";
		if (conn == null) {
			return new String();
		}
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				fullname = fullname + res.getString("first_name")+" "+res.getString("last_name");
			}
//			System.out.println(fullname);
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return fullname;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
	@Override
	public boolean registerClient(String userId, String password, String firstName, String lastName) {
		if (conn == null) {
			System.err.println("DB connection failed!");
			return false;
		}
		
		try {
			String sql = "SELECT * FROM users WHERE user_id = ?";
			
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);

			ResultSet rs = stmt.executeQuery();
			
			// de-duplicate registered names
			if (rs.next()) {
				return false;
			}
			
			sql = "INSERT INTO users VALUES (?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			stmt.setString(3, firstName);
			stmt.setString(4, lastName);
			stmt.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return true;
	}
	//debug
//	public static void main(String[] args) {
//		DBConnection connection = DBConnectionFactory.getConnection();
//		System.out.println(connection.getFullname("yu777"));
//	}


}
