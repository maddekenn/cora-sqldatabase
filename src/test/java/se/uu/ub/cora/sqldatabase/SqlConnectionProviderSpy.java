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

import se.uu.ub.cora.connection.ConnectionSpy;
import se.uu.ub.cora.connection.SqlConnectionProvider;

public class SqlConnectionProviderSpy implements SqlConnectionProvider {

	public ConnectionSpy connection = new ConnectionSpy();
	public boolean returnErrorConnection = false;
	public boolean getConnectionHasBeenCalled = false;

	@Override
	public Connection getConnection() {
		getConnectionHasBeenCalled = true;
		if (returnErrorConnection) {
			connection.returnErrorConnection = true;
		}
		return connection;
	}

}
