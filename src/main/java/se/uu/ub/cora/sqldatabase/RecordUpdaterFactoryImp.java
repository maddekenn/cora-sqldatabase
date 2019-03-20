package se.uu.ub.cora.sqldatabase;

import se.uu.ub.cora.connection.SqlConnectionProvider;

public class RecordUpdaterFactoryImp implements RecordUpdaterFactory {

	private SqlConnectionProvider connectionProvider;

	public RecordUpdaterFactoryImp(SqlConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}

	public SqlConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

	public RecordUpdater factor() {
		return RecordUpdaterImp.usingSqlConnectionProvider(connectionProvider);
	}

}
