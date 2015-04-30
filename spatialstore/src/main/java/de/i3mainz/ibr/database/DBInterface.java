package de.i3mainz.ibr.database;

import de.i3mainz.ibr.connections.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DBInterface implements AutoCloseable {
	
	private static final String driver = "org.postgresql.Driver";
	protected Connection connection = null;
	
	protected DBInterface() throws ClassNotFoundException, SQLException {
		loadJdbcDriver();
		openConnection();
	}
	
	private void loadJdbcDriver() throws ClassNotFoundException {
		Class.forName(driver);
	}
	
	private void openConnection() throws SQLException {
		String user = Config.getProperty("db_user");
		String password = Config.getProperty("db_password");
		String host = Config.getProperty("db_host");
		String port = Config.getProperty("db_port");
		String database = Config.getProperty("db_database");
		String url = ("jdbc:postgresql:" + (host != null ? ("//" + host) + (port != null ? ":" + port : "") + "/" : "") + database);
		connection = DriverManager.getConnection(url, user, password);
	}

	@Override
	public void close() throws SQLException {
		connection.close();
	}
	
}
