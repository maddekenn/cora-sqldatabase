package se.uu.ub.cora.sqldatabase;

import java.sql.Connection;

import se.uu.ub.cora.connection.ConnectionSpy;
import se.uu.ub.cora.connection.SqlConnectionProvider;

public class ConnectionProviderSpy implements SqlConnectionProvider {

	public ConnectionSpy connection = new ConnectionSpy();
	public boolean returnErrorConnection = false;

	@Override
	public Connection getConnection() {
		if (returnErrorConnection) {
			connection.returnErrorConnection = true;
		}
		return connection;
	}

}
