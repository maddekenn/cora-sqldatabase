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

import se.uu.ub.cora.connection.SqlConnectionProvider;

public final class RecordReaderImp implements RecordReader {
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
			throw SqlStorageException
					.withMessageAndException("Error reading data from " + tableName, e);
		}
	}

	private List<Map<String, String>> tryToReadAllFromTable(String tableName) throws SQLException {
		String sql = createSelectAllFor(tableName);
		Connection connection = sqlConnectionProvider.getConnection();
		try {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
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
		} finally {
			connection.close();
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

}