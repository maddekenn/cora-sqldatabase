/*
 * Copyright 2018, 2019 Uppsala University Library
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

public class DataReaderImpTest {
	private DataReader dataReader;
	private SqlConnectionProviderSpy sqlConnectionProviderSpy;
	private Map<String, String> conditions;
	private List<Object> values;

	@BeforeMethod
	public void beforeMethod() {
		conditions = new HashMap<>();
		conditions.put("alpha2code", "SE");
		sqlConnectionProviderSpy = new SqlConnectionProviderSpy();
		dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		values = new ArrayList<>();

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

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading data from someTableName: no row returned")
	public void testReadOneNoResultsThrowsException() throws Exception {
		String tableName = "someTableName";
		dataReader.readOneRowFromDbUsingTableAndConditions(tableName, conditions);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading data from someTableName")
	public void testReadOneSqlErrorThrowsError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
	}

	@Test
	public void testReadOneSqlErrorThrowsErrorAndSendsAlongOriginalError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		try {
			dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		} catch (Exception e) {
			assertEquals(e.getCause().getMessage(), "error thrown from prepareStatement in spy");
		}
	}

	@Test
	public void testGeneratedSqlQueryForOneString() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		String generatedSql = sqlConnectionProviderSpy.connection.sql;
		assertEquals(generatedSql, "select * from someTableName where alpha2code = ?");
	}

	@Test
	public void testExecuteQueryForOneIsCalled() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.executeQueryWasCalled);
	}

	@Test
	public void testExecuteQueryForOneIsCalledUsingValueFromConditions() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "SE");
	}

	@Test
	public void testGeneratedSqlQueryForOneStringTwoConditions() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		conditions.put("alpha3code", "SWE");
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		String generatedSql = sqlConnectionProviderSpy.connection.sql;
		assertEquals(generatedSql,
				"select * from someTableName where alpha2code = ? and alpha3code = ?");
	}

	@Test
	public void testExecuteQueryForOneIsCalledUsingValuesFromConditions() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		conditions.put("alpha3code", "SWE");
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "SE");
		assertEquals(preparedStatementSpy.usedSetStrings.get("2"), "SWE");
	}

	@Test
	public void testCloseOfConnectionIsCalledAfterReadOne() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		ConnectionSpy connectionSpy = sqlConnectionProviderSpy.connection;
		assertTrue(connectionSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfPrepareStatementIsCalledAfterReadOne() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfResultSetIsCalledAfterReadOne() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		assertTrue(resultSetSpy.closeWasCalled);
	}

	@Test
	public void testIfResultSetContainsDataForOneGetResultSetMetadataIsCalled() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);

		resultSetSpy.hasNext = true;
		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		assertEquals(resultSetSpy.getMetadataWasCalled, true);
	}

	private void setValuesInResultSetSpy(ResultSetSpy resultSetSpy) {
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = createListOfRowValues(columnNames);
		resultSetSpy.rowValues = rowValues;
	}

	private List<String> createListOfColumnNames() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("someColumnName");
		columnNames.add("someOtherColumnName");
		columnNames.add("twoColumnName");
		columnNames.add("someColumnNameThree");
		return columnNames;
	}

	private List<Map<String, String>> createListOfRowValues(List<String> columnNames) {
		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		return rowValues;
	}

	@Test
	public void testIfResultSetContainsDataForOneReturnedDataHasKeysFromResultSet()
			throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		setValuesInResultSetSpy(resultSetSpy);

		Map<String, String> readRow = dataReader
				.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);

		assertEquals(readRow.keySet().size(), 4);
		assertTrue(readRow.containsKey("someColumnName"));
	}

	@Test
	public void testIfResultSetContainsDataForOneContainsExpectedKeys() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = createListOfRowValues(columnNames);
		resultSetSpy.rowValues = rowValues;

		Map<String, String> readRow = dataReader
				.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		assertEquals(readRow.keySet().size(), 4);
		assertTrue(readRow.containsKey(columnNames.get(0)));
		assertTrue(readRow.containsKey(columnNames.get(1)));
		assertTrue(readRow.containsKey(columnNames.get(2)));
		assertTrue(readRow.containsKey(columnNames.get(3)));
	}

	@Test
	public void testIfResultSetContainsDataForOneContainsExptectedValues() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = createListOfRowValues(columnNames);
		resultSetSpy.rowValues = rowValues;

		Map<String, String> readRow = dataReader
				.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
		assertEquals(readRow.keySet().size(), 4);
		assertEquals(readRow.get(columnNames.get(0)), "value1");
		assertEquals(readRow.get(columnNames.get(1)), "secondValue");
		assertEquals(readRow.get(columnNames.get(2)), "thirdValue");
		assertEquals(readRow.get(columnNames.get(3)), "someOther value four");
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading data from someTableName: more than one row returned")
	public void testIfResultSetContainsDataForOneMoreRowsDataReturnedDataContainsValuesFromResultSet()
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

		dataReader.readOneRowFromDbUsingTableAndConditions("someTableName", conditions);
	}

	@Test
	public void testReadFromTableUsingConditionNoResultsReturnsEmptyList() throws Exception {
		String sql = "someTableName";
		List<Map<String, Object>> results = dataReader
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertEquals(results, Collections.emptyList());
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading data from someTableName")
	public void testReadFromTableUsingConditionSqlErrorThrowsError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
	}

	@Test
	public void testReadFromTableUsingConditionSqlErrorThrowsErrorAndSendsAlongOriginalError()
			throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		dataReader = DataReaderImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		try {
			dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		} catch (Exception e) {
			assertEquals(e.getCause().getMessage(), "error thrown from prepareStatement in spy");
		}
	}

	@Test
	public void testSqlSetAsPreparedStatement() throws Exception {
		dataReader.executePreparedStatementQueryUsingSqlAndValues(
				"select * from someTableName where alpha2code = ?", values);
		String generatedSql = sqlConnectionProviderSpy.connection.sql;
		assertEquals(generatedSql, "select * from someTableName where alpha2code = ?");
	}

	@Test
	public void testExecuteQueryIsCalledForExecutePreparedStatement() throws Exception {
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.executeQueryWasCalled);
	}

	@Test
	public void testCloseOfConnectionIsCalledForExecutePreparedStatement() throws Exception {
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		ConnectionSpy connectionSpy = sqlConnectionProviderSpy.connection;
		assertTrue(connectionSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfPrepareStatementIsCalledForExecutePreparedStatement() throws Exception {
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfResultSetIsCalledForExecutePreparedStatement() throws Exception {
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		assertTrue(resultSetSpy.closeWasCalled);
	}

	@Test
	public void testIfSetMetadataIsCalledForExecutePreparedStatement() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);

		resultSetSpy.hasNext = true;
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		assertEquals(resultSetSpy.getMetadataWasCalled, true);
	}

	@Test
	public void testIfResultSetContainsDataReturnedDataHasKeysFromResultSetForExecutePreparedStatement()
			throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();
		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);
		resultSetSpy.rowValues = rowValues;

		List<Map<String, Object>> readAllFromTable = dataReader
				.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		Map<String, Object> row0 = readAllFromTable.get(0);

		assertEquals(row0.keySet().size(), 4);
		assertTrue(row0.containsKey("someColumnName"));
		assertTrue(row0.containsKey(columnNames.get(0)));
		assertTrue(row0.containsKey(columnNames.get(1)));
		assertTrue(row0.containsKey(columnNames.get(2)));
		assertTrue(row0.containsKey(columnNames.get(3)));
	}

	@Test
	public void testIfResultSetContainsDataReturnedDataContainsValuesFromResultSetForExecutePreparedStatement()
			throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		resultSetSpy.hasNext = true;
		List<String> columnNames = createListOfColumnNames();
		resultSetSpy.columnNames = columnNames;

		List<Map<String, String>> rowValues = new ArrayList<>();

		Map<String, String> columnValues = createMapWithColumnNamesAndValues(columnNames, "");
		rowValues.add(columnValues);

		resultSetSpy.rowValues = rowValues;

		List<Map<String, Object>> readAllFromTable = dataReader
				.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		Map<String, Object> row0 = readAllFromTable.get(0);

		assertEquals(readAllFromTable.size(), 1);
		assertEquals(row0.keySet().size(), 4);
		assertEquals(row0.get(columnNames.get(0)), "value1");
		assertEquals(row0.get(columnNames.get(1)), "secondValue");
		assertEquals(row0.get(columnNames.get(2)), "thirdValue");
		assertEquals(row0.get(columnNames.get(3)), "someOther value four");
	}

	@Test
	public void testUsingValuesFromConditionsForExecutePreparedStatement() throws Exception {
		ResultSetSpy resultSetSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy.resultSet;
		setValuesInResultSetSpy(resultSetSpy);
		values.add("SE");
		values.add("SWE");
		dataReader.executePreparedStatementQueryUsingSqlAndValues("someTableName", values);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetObjects.get("1"), "SE");
		assertEquals(preparedStatementSpy.usedSetObjects.get("2"), "SWE");
	}

}