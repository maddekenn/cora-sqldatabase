/*
 * Copyright 2019 Uppsala University Library
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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataCreatorTest {

	private SqlConnectionProviderSpy sqlConnectionProviderSpy;
	private List<Object> values;
	private DataCreator dataCreator;
	private String sql = "insert into testTable(id, name) values(?, ?)";

	@BeforeMethod
	public void beforeMethod() {
		sqlConnectionProviderSpy = new SqlConnectionProviderSpy();
		dataCreator = DataCreatorImp.usingSqlConnectionProvider(sqlConnectionProviderSpy);
		values = new ArrayList<>();

	}

	@Test
	public void testOneAffectedRow() {
		int createdRows = dataCreator.executeUsingSqlAndValues(sql, values);
		assertEquals(createdRows, 1);
	}

	// TODO: kolla meddelandet, får inte till det med escape av ?
	@Test(expectedExceptions = SqlStorageException.class)
	public void testExecuteSqlThrowsError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		dataCreator.executeUsingSqlAndValues(sql, values);
	}

	@Test
	public void testExecuteSqlErrorThrowsErrorAndSendsAlongOriginalError() throws Exception {
		sqlConnectionProviderSpy.returnErrorConnection = true;
		try {
			dataCreator.executeUsingSqlAndValues(sql, values);
		} catch (Exception e) {
			assertEquals(e.getCause().getMessage(), "error thrown from prepareStatement in spy");
		}
	}

	@Test
	public void testSqlSetAsPreparedStatement() throws Exception {
		dataCreator.executeUsingSqlAndValues(sql, values);
		String generatedSql = sqlConnectionProviderSpy.connection.sql;
		assertEquals(generatedSql, sql);
	}

	// @Test
	// public void testExecuteIsCalledForExecutePreparedStatement() throws Exception {
	// dataUpdater.executeUsingSqlAndValues(sql, values);
	// PreparedStatementSpy preparedStatementSpy =
	// sqlConnectionProviderSpy.connection.preparedStatementSpy;
	// assertTrue(preparedStatementSpy.executeUpdateWasCalled);
	// }
	//
	// @Test
	// public void testCloseOfConnectionIsCalledForExecutePreparedStatement() throws Exception {
	// dataUpdater.executeUsingSqlAndValues(sql, values);
	// ConnectionSpy connectionSpy = sqlConnectionProviderSpy.connection;
	// assertTrue(connectionSpy.closeWasCalled);
	// }
	//
	// @Test
	// public void testCloseOfPrepareStatementIsCalledForExecutePreparedStatement() throws Exception
	// {
	// dataUpdater.executeUsingSqlAndValues(sql, values);
	// PreparedStatementSpy preparedStatementSpy =
	// sqlConnectionProviderSpy.connection.preparedStatementSpy;
	// assertTrue(preparedStatementSpy.closeWasCalled);
	// }
	//
	// @Test
	// public void testUsingValuesFromConditionsForExecutePreparedStatement() throws Exception {
	// PreparedStatementSpy preparedStatementSpy =
	// sqlConnectionProviderSpy.connection.preparedStatementSpy;
	// values.add("SE");
	// values.add("SWE");
	// dataUpdater.executeUsingSqlAndValues(sql, values);
	// assertEquals(preparedStatementSpy.usedSetObjects.get("1"), "SE");
	// assertEquals(preparedStatementSpy.usedSetObjects.get("2"), "SWE");
	// }
	//
	// @Test
	// public void testReturnFromConditionsForExecutePreparedStatement() throws Exception {
	// PreparedStatementSpy preparedStatementSpy =
	// sqlConnectionProviderSpy.connection.preparedStatementSpy;
	// preparedStatementSpy.noOfAffectedRows = 5;
	// values.add("SE");
	// values.add("SWE");
	// int updatedRows = dataUpdater.executeUsingSqlAndValues(sql, values);
	// assertEquals(updatedRows, 5);
	// }
}
