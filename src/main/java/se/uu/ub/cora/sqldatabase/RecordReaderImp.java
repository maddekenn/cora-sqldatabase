/*
 * Copyright 2018 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.sqldatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import se.uu.ub.cora.connection.SqlConnectionProvider;

public final class RecordReaderImp implements RecordReader {
	private static final String ERROR_READING_DATA_FROM = "Error reading data from ";
	private SqlConnectionProvider sqlConnectionProvider;

	private RecordReaderImp(SqlConnectionProvider sqlConnectionProvider) {
		this.sqlConnectionProvider = sqlConnectionProvider;
	}

	public static RecordReaderImp usingSqlConnectionProvider(
			SqlConnectionProvider sqlConnectionProvider) {
		return new RecordReaderImp(sqlConnectionProvider);
	}

	@Override
	public List<Map<String, String>> readAllFromTable(String tableName) {
		try {
			return tryToReadAllFromTable(tableName);
		} catch (SQLException e) {
			throw SqlStorageException.withMessageAndException(ERROR_READING_DATA_FROM + tableName,
					e);
		}
	}

	private List<Map<String, String>> tryToReadAllFromTable(String tableName) throws SQLException {
		String sql = createSelectAllFor(tableName);
		return readFromTableUsingSql(sql);
	}

	private List<Map<String, String>> readFromTableUsingSql(String sql) throws SQLException {
		Connection connection = sqlConnectionProvider.getConnection();
		try {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			return getResultUsingQuery(prepareStatement);
		} finally {
			connection.close();
		}
	}

	private List<Map<String, String>> getResultUsingQuery(PreparedStatement prepareStatement)
			throws SQLException {
		try {
			ResultSet resultSet = prepareStatement.executeQuery();
			try {
				List<String> columnNames = createListOfColumnNamesFromResultSet(resultSet);
				return createListOfMapsFromResultSetUsingColumnNames(resultSet, columnNames);
			} finally {
				resultSet.close();
			}
		} finally {
			prepareStatement.close();
		}
	}

	private HashMap<String, String> createMapForCurrentRowInResultSet(ResultSet resultSet,
			List<String> columnNames) throws SQLException {
		HashMap<String, String> row = new HashMap<>();
		for (String columnName : columnNames) {
			row.put(columnName, resultSet.getString(columnName));
		}
		return row;
	}

	private List<Map<String, String>> createListOfMapsFromResultSetUsingColumnNames(
			ResultSet resultSet, List<String> columnNames) throws SQLException {
		List<Map<String, String>> all = new ArrayList<>();
		while (resultSet.next()) {
			HashMap<String, String> row = createMapForCurrentRowInResultSet(resultSet, columnNames);
			all.add(row);
		}
		return all;
	}

	private List<String> createListOfColumnNamesFromResultSet(ResultSet resultSet)
			throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		return createListOfColumnNamesFromMetadata(metaData, columnCount);
	}

	private List<String> createListOfColumnNamesFromMetadata(ResultSetMetaData metaData,
			int columnCount) throws SQLException {
		List<String> columnNames = new ArrayList<>();
		for (int i = 1; i <= columnCount; i++) {
			columnNames.add(metaData.getColumnName(i));
		}
		return columnNames;
	}

	private String createSelectAllFor(String tableName) {
		return "select * from " + tableName;
	}

	@Override
	public Map<String, String> readOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, String> conditions) {
		try {
			return tryToReadOneRowFromDbUsingTableAndConditions(tableName, conditions);
		} catch (SQLException e) {
			throw SqlStorageException.withMessageAndException(ERROR_READING_DATA_FROM + tableName,
					e);
		}
	}

	private Map<String, String> tryToReadOneRowFromDbUsingTableAndConditions(String tableName,
			Map<String, String> conditions) throws SQLException {
		String sql = createSqlForTableNameAndConditions(tableName, conditions);
		List<Map<String, String>> readRows = readFromTableUsingSqlAndConditions(sql, conditions);
		throwErrorIfNoRowIsReturned(tableName, readRows);
		throwErrorIfMoreThanOneRowIsReturned(tableName, readRows);
		return getSingleResultFromList(readRows);
	}

	private List<Map<String, String>> readFromTableUsingSqlAndConditions(String sql,
			Map<String, String> conditions) throws SQLException {
		Connection connection = sqlConnectionProvider.getConnection();
		try {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			addParameterValuesToPreparedStatement(conditions, prepareStatement);
			return getResultUsingQuery(prepareStatement);
		} finally {
			connection.close();
		}
	}

	private void addParameterValuesToPreparedStatement(Map<String, String> conditions,
			PreparedStatement prepareStatement) throws SQLException {
		int position = 1;
		for (String value : conditions.values()) {
			prepareStatement.setString(position, value);
			position++;
		}
	}

	private void throwErrorIfNoRowIsReturned(String tableName, List<Map<String, String>> readRows) {
		if (readRows.isEmpty()) {
			throw SqlStorageException
					.withMessage(ERROR_READING_DATA_FROM + tableName + ": no row returned");
		}
	}

	private void throwErrorIfMoreThanOneRowIsReturned(String tableName,
			List<Map<String, String>> readRows) {
		if (resultHasMoreThanOneRow(readRows)) {
			throw SqlStorageException.withMessage(
					ERROR_READING_DATA_FROM + tableName + ": more than one row returned");
		}
	}

	private boolean resultHasMoreThanOneRow(List<Map<String, String>> readRows) {
		return readRows.size() > 1;
	}

	private Map<String, String> getSingleResultFromList(List<Map<String, String>> readRows) {
		return readRows.get(0);
	}

	private String createSqlForTableNameAndConditions(String tableName,
			Map<String, String> conditions) {
		String sql = "select * from " + tableName + " where ";
		String conditionPart = createConditionPartOfSql(conditions);
		sql += conditionPart;
		return sql;
	}

	private String createConditionPartOfSql(Map<String, String> conditions) {
		StringJoiner joiner = new StringJoiner(" and ");
		for (String key : conditions.keySet()) {
			joiner.add(key + " = ?");
		}
		return joiner.toString();
	}

}