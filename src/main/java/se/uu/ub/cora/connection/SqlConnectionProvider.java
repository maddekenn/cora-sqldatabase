package se.uu.ub.cora.connection;

import java.sql.Connection;

public interface SqlConnectionProvider {

	Connection getConnection();

}