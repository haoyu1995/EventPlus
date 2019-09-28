package db;

import db.mysql.MySQLDBConnection;

public class DBConnectionFactory {
	private static final String DEFAULT_DB = "mysql";
	
	public static DBConnection getConnection(String db) {
		// This should change based on the pipeline.
		switch(db) {
		case "mysql":
			return new MySQLDBConnection();
		case "mongodb":
			//return new MongoDBConnnection();
			return null;
		default:
			throw new IllegalArgumentException("Invalid Argument");
		}
	}
	
	public static DBConnection getConnection() {
		return getConnection(DEFAULT_DB);
	}
}
