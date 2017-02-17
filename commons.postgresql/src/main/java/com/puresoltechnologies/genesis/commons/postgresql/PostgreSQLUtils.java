package com.puresoltechnologies.genesis.commons.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgreSQLUtils {

    public static Connection connect(String host, int port, String database, String user, String password, boolean ssl)
	    throws SQLException {
	Properties props = new Properties();
	props.setProperty("user", user);
	props.setProperty("password", password);
	if (ssl) {
	    props.setProperty("ssl", "true");
	}
	Connection connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + database,
		props);
	connection.setAutoCommit(false);
	return connection;
    }

    public static Connection connect(Properties configuration) throws NumberFormatException, SQLException {
	return PostgreSQLUtils.connect(configuration.getProperty("host"),
		Integer.parseInt(configuration.getProperty("port")), configuration.getProperty("database"),
		configuration.getProperty("user"), configuration.getProperty("password"),
		Boolean.parseBoolean(configuration.getProperty("ssl")));
    }
}
