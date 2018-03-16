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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.connection.ConnectionSpy;
import se.uu.ub.cora.connection.PreparedStatementSpy;
import se.uu.ub.cora.connection.ResultSetSpy;

public class RecordReaderImpTest {
	private RecordReader recordReader;
	private ConnectionProviderSpy sqlConnectionProviderSpy;

	@BeforeMethod
	public void beforeMethod() {
		sqlConnectionProviderSpy = new ConnectionProviderSpy();
		recordReader = RecordReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
	}

	@Test
	public void testRecordReaderImpImplementsRecordReader() throws Exception {
		assertNotNull(recordReader);
	}

	@Test
	public void testReadNoResultsReturnsEmptyList() throws Exception {
		String tableName = "someTableName";
		List<Map<String, String>> results = recordReader.readAllFromTable(tableName);
		assertEquals(results, Collections.emptyList());
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading data from someTableName")
	public void testReadSqlErrorThrowsError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		recordReader = RecordReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		recordReader.readAllFromTable("someTableName");
	}

	@Test
	public void testReadSqlErrorThrowsErrorAndSendsAlongOriginalError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		recordReader = RecordReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		try {
			recordReader.readAllFromTable("someTableName");
		} catch (Exception e) {
			assertEquals(e.getCause().getMessage(), "error thrown from prepareStatement in spy");
		}
	}

	@Test
	public void testGeneratedSqlQueryString() throws Exception {
		recordReader.readAllFromTable("someTableName");
		String generatedSql = sqlConnectionProviderSpy.connection.sql;
		assertEquals(generatedSql, "select * from someTableName");
	}

	@Test
	public void testExecuteQueryIsCalled() throws Exception {
		recordReader.readAllFromTable("someTableName");
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.executeQueryWasCalled);
	}

	@Test
	public void testCloseOfConnectionIsCalled() throws Exception {
		recordReader.readAllFromTable("someTableName");
		ConnectionSpy connectionSpy = sqlConnectionProviderSpy.connection;
		assertTrue(connectionSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfPrepareStatementIsCalled() throws Exception {
		recordReader.readAllFromTable("someTableName");
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfResultSetIsCalled() throws Exception {
		recordReader.readAllFromTable("someTableName");
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		assertTrue(resultSetSpy.closeWasCalled);
	}

	@Test
	public void testIfResultSetContainsDataGetResultSetMetadataIsCalled() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		List<String> columnNames = createListOfColumnNames();
		// List<String> columnNames = new ArrayList<>();
		// columnNames.add("someColumnName");
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		resultSetSpy.rowValues = rowValues;

		resultSetSpy.hasNext = true;
		recordReader.readAllFromTable("someTableName");
		assertEquals(resultSetSpy.getMetadataWasCalled, true);
	}

	@Test
	public void testIfResultSetContainsDataReturnedDataHasKeysFromResultSet() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		// List<String> columnNames = new ArrayList<>();
		List<String> columnNames = createListOfColumnNames();
		// columnNames.add("someColumnName");
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		resultSetSpy.rowValues = rowValues;

		List<Map<String, String>> readAllFromTable = recordReader.readAllFromTable("someTableName");
		Map<String, String> row0 = readAllFromTable.get(0);

		assertEquals(row0.keySet().size(), 4);
		assertTrue(row0.containsKey("someColumnName"));
	}

	@Test
	public void testIfResultSetContainsDataReturnedDataHasMoreKeysFromResultSet() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		resultSetSpy.rowValues = rowValues;

		List<Map<String, String>> readAllFromTable = recordReader.readAllFromTable("someTableName");
		Map<String, String> row0 = readAllFromTable.get(0);

		assertEquals(row0.keySet().size(), 4);
		assertTrue(row0.containsKey(columnNames.get(0)));
		assertTrue(row0.containsKey(columnNames.get(1)));
		assertTrue(row0.containsKey(columnNames.get(2)));
		assertTrue(row0.containsKey(columnNames.get(3)));
	}

	private List<String> createListOfColumnNames() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("someColumnName");
		columnNames.add("someOtherColumnName");
		columnNames.add("twoColumnName");
		columnNames.add("someColumnNameThree");
		return columnNames;
	}

	@Test
	public void testIfResultSetContainsDataReturnedDataContainsValuesFromResultSet()
			throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		// Map<String, String> columnValues =
		// createMapWithColumnNamesAndValues(columnNames, "");
		// resultSetSpy.columnValues = columnValues;
		List<Map<String, String>> rowValues = new ArrayList<>();

		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);

		resultSetSpy.rowValues = rowValues;

		List<Map<String, String>> readAllFromTable = recordReader.readAllFromTable("someTableName");
		Map<String, String> row0 = readAllFromTable.get(0);

		assertEquals(readAllFromTable.size(), 1);
		assertEquals(row0.keySet().size(), 4);
		assertEquals(row0.get(columnNames.get(0)), "value1");
		assertEquals(row0.get(columnNames.get(1)), "secondValue");
		assertEquals(row0.get(columnNames.get(2)), "thirdValue");
		assertEquals(row0.get(columnNames.get(3)), "someOther value four");
	}

	private Map<String, String> createMapWithColumnNamesAndValues(List<String> columnNames,
			String extraValue) {
		Map<String, String> columnValues = new HashMap<>();
		columnValues.put(columnNames.get(0), "value1" + extraValue);
		columnValues.put(columnNames.get(1), "secondValue" + extraValue);
		columnValues.put(columnNames.get(2), "thirdValue" + extraValue);
		columnValues.put(columnNames.get(3), "someOther value four" + extraValue);
		return columnValues;
	}

	@Test
	public void testIfResultSetContainsMoreRowsDataReturnedDataContainsValuesFromResultSet()
			throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		Map<String, String> columnValues2 = createMapWithColumnNamesAndValues(columnNames, "2");
		rowValues.add(columnValues2);
		resultSetSpy.rowValues = rowValues;

		List<Map<String, String>> readAllFromTable = recordReader.readAllFromTable("someTableName");
		Map<String, String> row0 = readAllFromTable.get(0);

		assertEquals(readAllFromTable.size(), 2);
		assertEquals(row0.keySet().size(), 4);
		assertEquals(row0.get(columnNames.get(0)), "value1");
		assertEquals(row0.get(columnNames.get(1)), "secondValue");
		assertEquals(row0.get(columnNames.get(2)), "thirdValue");
		assertEquals(row0.get(columnNames.get(3)), "someOther value four");
	}
}
