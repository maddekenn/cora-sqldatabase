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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RecordUpdaterFactoryTest {
	private SqlConnectionProviderSpy connectionProvider;
	private RecordUpdaterFactoryImp updaterFactory;

	@BeforeMethod
	public void beforeMethod() {
		connectionProvider = new SqlConnectionProviderSpy();
		updaterFactory = new RecordUpdaterFactoryImp(connectionProvider);
	}

	@Test
	public void testInit() throws Exception {
		assertEquals(updaterFactory.getConnectionProvider(), connectionProvider);
	}

	@Test
	public void testFactor() throws Exception {
		RecordUpdater recordUpdater = updaterFactory.factor();
		assertTrue(recordUpdater instanceof RecordUpdaterImp);
	}

	@Test
	public void testProvidedConnectionProviderIsUsed() throws Exception {
		RecordUpdaterImp recordUpdater = (RecordUpdaterImp) updaterFactory.factor();
		assertEquals(recordUpdater.getConnectionProvider(), connectionProvider);
	}
}
