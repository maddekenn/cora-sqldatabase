package se.uu.ub.cora.sqldatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import se.uu.ub.cora.connection.SqlConnectionProvider;

public class RecordUpdaterImp implements RecordUpdater {

	private SqlConnectionProvider connectionProvider;

	private RecordUpdaterImp(SqlConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}

	public static RecordUpdater usingSqlConnectionProvider(
			SqlConnectionProvider connectionProvider) {
		return new RecordUpdaterImp(connectionProvider);
	}

	public SqlConnectionProvider getConnectionProvider() {
		// needed for test
		return connectionProvider;
	}

	@Override
	public void update(String tableName, Map<String, String> values,
			Map<String, String> conditions) {
		try {
			if (!values.isEmpty() && !conditions.isEmpty()) {
				StringBuilder sqlBuilder = new StringBuilder("update " + tableName + " set ");
				Entry<String, String> firstValue = values.entrySet().iterator().next();
				sqlBuilder.append(firstValue.getKey()).append(" = ?");

				sqlBuilder.append(" where ");
				Entry<String, String> firstCondition = conditions.entrySet().iterator().next();
				sqlBuilder.append(firstCondition.getKey()).append(" = ?");
				String sql = sqlBuilder.toString();

				Connection connection = connectionProvider.getConnection();
				connection.prepareStatement(sql);
			}

		} catch (SQLException e) {
			throw SqlStorageException.withMessageAndException("Error updating data in " + tableName,
					e);
		}
	}

}
