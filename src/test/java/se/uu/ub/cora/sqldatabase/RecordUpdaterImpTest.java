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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.connection.ConnectionSpy;
import se.uu.ub.cora.connection.PreparedStatementSpy;

public class RecordUpdaterImpTest {
	private RecordUpdater recordUpdater;
	private SqlConnectionProviderSpy sqlConnectionProviderSpy;
	private Map<String, Object> conditions;
	private Map<String, Object> values;
	private String tableName = "someTableName";

	@BeforeMethod
	public void beforeMethod() {
		values = new HashMap<>();
		conditions = new HashMap<>();
		values.put("name", "someNewName");
		conditions.put("id", "someId");
		sqlConnectionProviderSpy = new SqlConnectionProviderSpy();
		recordUpdater = RecordUpdaterImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
	}

	@Test
	public void testUpdateRecordNoValues() throws Exception {
		values = new HashMap<>();
		String tableName = "someTableName";
		recordUpdater.update(tableName, values, conditions);
		assertFalse(sqlConnectionProviderSpy.getConnectionHasBeenCalled);
	}

	@Test
	public void testUpdateRecordNoConditions() throws Exception {
		conditions = new HashMap<>();
		String tableName = "someTableName";
		recordUpdater.update(tableName, values, conditions);
		assertFalse(sqlConnectionProviderSpy.getConnectionHasBeenCalled);
	}

	@Test
	public void testUpdateRecordNoValuesNoConditions() throws Exception {
		values = new HashMap<>();
		conditions = new HashMap<>();
		String tableName = "someTableName";
		recordUpdater.update(tableName, values, conditions);
		assertFalse(sqlConnectionProviderSpy.getConnectionHasBeenCalled);
	}

	@Test
	public void testUpdateRecordOneValueOneCondition() throws Exception {
		recordUpdater.update(tableName, values, conditions);

		assertTrue(sqlConnectionProviderSpy.getConnectionHasBeenCalled);

		ConnectionSpy connection = sqlConnectionProviderSpy.connection;
		assertEquals(connection.sql, "update someTableName set name = ? where id = ?");
		PreparedStatementSpy preparedStatementSpy = connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "someNewName");
		assertEquals(preparedStatementSpy.usedSetStrings.get("2"), "someId");

		assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	}

	@Test
	public void testCloseOfConnectionIsCalled() throws Exception {
		recordUpdater.update(tableName, values, conditions);
		ConnectionSpy connectionSpy = sqlConnectionProviderSpy.connection;
		assertTrue(connectionSpy.closeWasCalled);
	}

	@Test
	public void testCloseOfPrepareStatementIsCalled() throws Exception {
		recordUpdater.update(tableName, values, conditions);
		PreparedStatementSpy preparedStatementSpy = sqlConnectionProviderSpy.connection.preparedStatementSpy;
		assertTrue(preparedStatementSpy.closeWasCalled);
	}

	@Test
	public void testUpdateRecordTwoValuesOneCondition() throws Exception {
		values.put("code", "someNewCode");
		recordUpdater.update(tableName, values, conditions);

		assertTrue(sqlConnectionProviderSpy.getConnectionHasBeenCalled);

		ConnectionSpy connection = sqlConnectionProviderSpy.connection;
		assertEquals(connection.sql, "update someTableName set code = ?, name = ? where id = ?");
		PreparedStatementSpy preparedStatementSpy = connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "someNewCode");
		assertEquals(preparedStatementSpy.usedSetStrings.get("2"), "someNewName");
		assertEquals(preparedStatementSpy.usedSetStrings.get("3"), "someId");

		assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	}

	@Test
	public void testUpdateRecordTwoValuesTwoConditions() throws Exception {
		values.put("code", "someNewCode");
		conditions.put("type", "someType");
		recordUpdater.update(tableName, values, conditions);

		assertTrue(sqlConnectionProviderSpy.getConnectionHasBeenCalled);

		ConnectionSpy connection = sqlConnectionProviderSpy.connection;
		assertEquals(connection.sql,
				"update someTableName set code = ?, name = ? where id = ? and type = ?");
		PreparedStatementSpy preparedStatementSpy = connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "someNewCode");
		assertEquals(preparedStatementSpy.usedSetStrings.get("2"), "someNewName");
		assertEquals(preparedStatementSpy.usedSetStrings.get("3"), "someId");

		assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	}

	@Test
	public void testUpdateRecordOneIntegerValue() throws Exception {
		values = new HashMap<>();
		values.put("code", 22);
		recordUpdater.update(tableName, values, conditions);

		assertTrue(sqlConnectionProviderSpy.getConnectionHasBeenCalled);

		ConnectionSpy connection = sqlConnectionProviderSpy.connection;
		assertEquals(connection.sql, "update someTableName set code = ? where id = ?");

		PreparedStatementSpy preparedStatementSpy = connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetIntegers.size(), 1);
		assertEquals(preparedStatementSpy.usedSetIntegers.get("1"), Integer.valueOf(22));
		assertEquals(preparedStatementSpy.usedSetStrings.get("2"), "someId");

		assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	}

	@Test
	public void testUpdateRecordOneValueOneIntegerCondition() throws Exception {
		conditions = new HashMap<>();
		conditions.put("id", 22);
		recordUpdater.update(tableName, values, conditions);

		assertTrue(sqlConnectionProviderSpy.getConnectionHasBeenCalled);

		ConnectionSpy connection = sqlConnectionProviderSpy.connection;
		assertEquals(connection.sql, "update someTableName set name = ? where id = ?");

		PreparedStatementSpy preparedStatementSpy = connection.preparedStatementSpy;
		assertEquals(preparedStatementSpy.usedSetIntegers.size(), 1);
		assertEquals(preparedStatementSpy.usedSetStrings.size(), 1);
		assertEquals(preparedStatementSpy.usedSetStrings.get("1"), "someNewName");
		assertEquals(preparedStatementSpy.usedSetIntegers.get("2"), Integer.valueOf(22));

		assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	}

	@Test(expectedExceptions = SqlStorageException.class)
	public void testUpdateRecordOneValueOneOtherObjectValueThrowsException() throws Exception {
		values = new HashMap<>();
		values.put("code", true);
		recordUpdater.update(tableName, values, conditions);
	}

	@Test(expectedExceptions = SqlStorageException.class, expectedExceptionsMessageRegExp = ""
			+ "Error updating data in someTableName")
	public void testUpdatingSqlErrorThrowsError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		recordUpdater = RecordUpdaterImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		recordUpdater.update("someTableName", values, conditions);
	}

}