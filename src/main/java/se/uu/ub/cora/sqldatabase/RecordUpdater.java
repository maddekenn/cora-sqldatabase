package se.uu.ub.cora.sqldatabase;

import java.util.Map;

public interface RecordUpdater {

	void update(String tableName, Map<String, Object> values, Map<String, Object> conditions);

}
