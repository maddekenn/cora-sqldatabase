package se.uu.ub.cora.sqldatabase;

import java.util.Map;

public interface RecordUpdater {

	void update(String tableName, Map<String, String> values, Map<String, String> conditions);

}
