package se.uu.ub.cora.sqldatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
	public void update(String tableName, Map<String, Object> values,
			Map<String, Object> conditions) {
		try {
			possiblyUpdate(tableName, values, conditions);
		} catch (SQLException e) {
			throw SqlStorageException.withMessageAndException("Error updating data in " + tableName,
					e);
		}
	}

	private void possiblyUpdate(String tableName, Map<String, Object> values,
			Map<String, Object> conditions) throws SQLException {
		if (valuesAndConditionsExist(values, conditions)) {
			tryToUpdate(tableName, values, conditions);
		}
	}

	private boolean valuesAndConditionsExist(Map<String, Object> values,
			Map<String, Object> conditions) {
		return !values.isEmpty() && !conditions.isEmpty();
	}

	private void tryToUpdate(String tableName, Map<String, Object> values,
			Map<String, Object> conditions) throws SQLException {

		StringBuilder sqlBuilder = new StringBuilder("update " + tableName + " set ");
		List<Object> preparedStatementsValues = createBasePart(values, sqlBuilder);
		List<Object> preparedStatementsConditions = createConditionPart(conditions, sqlBuilder);

		String sql = sqlBuilder.toString();
		Connection connection = connectionProvider.getConnection();
		try {
			tryToPrepareAndExecuteStatement(preparedStatementsValues, preparedStatementsConditions,
					sql, connection);
		} finally {
			connection.close();
		}
	}

	private void tryToPrepareAndExecuteStatement(List<Object> preparedStatementsValues,
			List<Object> preparedStatementsConditions, String sql, Connection connection)
			throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		try {
			prepareAndExecuteStatement(preparedStatementsValues, preparedStatementsConditions,
					preparedStatement);
		} finally {
			preparedStatement.close();
		}
	}

	private void prepareAndExecuteStatement(List<Object> preparedStatementsValues,
			List<Object> preparedStatementsConditions, PreparedStatement preparedStatement)
			throws SQLException {
		int counter = 1;
		counter = setValues(preparedStatementsValues, preparedStatement, counter);
		setConditions(preparedStatementsConditions, preparedStatement, counter);
		preparedStatement.executeUpdate();
	}

	private int setConditions(List<Object> preparedStatementsConditions,
			PreparedStatement preparedStatement, int counter) throws SQLException {
		for (Object psCondition : preparedStatementsConditions) {
			categorizeAndSetValues(preparedStatement, counter, psCondition);
			counter++;
		}
		return counter;
	}

	private int setValues(List<Object> preparedStatementsValues,
			PreparedStatement preparedStatement, int counter) throws SQLException {
		for (Object psValue : preparedStatementsValues) {
			categorizeAndSetValues(preparedStatement, counter, psValue);
			counter++;
		}
		return counter;
	}

	private void categorizeAndSetValues(PreparedStatement preparedStatement, int counter,
			Object psCondition) throws SQLException {
		if (psCondition instanceof String) {
			preparedStatement.setString(counter, (String) psCondition);
		} else if (psCondition instanceof Integer) {
			preparedStatement.setInt(counter, (Integer) psCondition);
		} else {
			throw new SQLException("object type not supported");
		}
	}

	private List<Object> createConditionPart(Map<String, Object> conditions,
			StringBuilder sqlBuilder) {
		List<String> conditionsSqlParts = new ArrayList<>();
		List<Object> preparedStatementsConditions = new ArrayList<>();
		for (Entry<String, Object> conditionEntry : conditions.entrySet()) {
			conditionsSqlParts.add(conditionEntry.getKey() + " = ?");
			preparedStatementsConditions.add(conditionEntry.getValue());
		}
		sqlBuilder.append(String.join(" and ", conditionsSqlParts));
		return preparedStatementsConditions;
	}

	private List<Object> createBasePart(Map<String, Object> values, StringBuilder sqlBuilder) {
		List<String> valuesSqlParts = new ArrayList<>();
		List<Object> preparedStatementsValues = new ArrayList<>();
		for (Entry<String, Object> entry : values.entrySet()) {
			valuesSqlParts.add(entry.getKey() + " = ?");
			preparedStatementsValues.add(entry.getValue());
		}
		sqlBuilder.append(String.join(", ", valuesSqlParts));
		sqlBuilder.append(" where ");
		return preparedStatementsValues;
	}

}
