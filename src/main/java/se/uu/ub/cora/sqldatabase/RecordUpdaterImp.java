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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

public class RecordUpdaterImp implements RecordUpdater {

	private DataUpdater dataUpdater;

	public RecordUpdaterImp(DataUpdater dataUpdater) {
		this.dataUpdater = dataUpdater;
	}

	@Override
	public void updateRecordInDbUsingTableAndValuesAndConditions(String tableName,
			Map<String, Object> columns, Map<String, Object> conditions) {

		List<Object> valuesForUpdate = new ArrayList<>();
		String sql = createFirstPartOfSqlStatement(tableName, columns, valuesForUpdate);

		String secondPart = createSecondPartOfSqlStatement(conditions, valuesForUpdate);
		sql += secondPart;

		dataUpdater.executeUsingSqlAndValues(sql, valuesForUpdate);
	}

	private String createSecondPartOfSqlStatement(Map<String, Object> conditions,
			List<Object> valuesForUpdate) {
		List<String> conditionNames = new ArrayList<>();
		for (Entry<String, Object> condition : conditions.entrySet()) {
			conditionNames.add(condition.getKey());
			valuesForUpdate.add(condition.getValue());
		}
		return " where " + createValuePartOfSql(conditionNames);
	}

	private String createFirstPartOfSqlStatement(String tableName, Map<String, Object> columns,
			List<Object> valuesForUpdate) {
		List<String> columnNames = new ArrayList<>(columns.size());
		for (Entry<String, Object> column : columns.entrySet()) {
			columnNames.add(column.getKey());
			valuesForUpdate.add(column.getValue());
		}
		return createSqlForTableNameAndColumns(tableName, columnNames);
	}

	public DataUpdater getDataUpdater() {
		return dataUpdater;
	}

	private String createSqlForTableNameAndColumns(String tableName, List<String> columnNames) {
		String sql = "update " + tableName + " set ";

		StringJoiner joiner = new StringJoiner(", ");
		for (String columnName : columnNames) {
			joiner.add(columnName + " = ?");
		}
		sql += joiner.toString();
		return sql;
	}

	private String createValuePartOfSql(List<String> conditions) {
		StringJoiner joiner = new StringJoiner(" and ");
		for (String key : conditions) {
			joiner.add(key + " = ?");
		}
		return joiner.toString();
	}

}
