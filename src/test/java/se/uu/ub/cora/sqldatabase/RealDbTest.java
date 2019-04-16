package se.uu.ub.cora.sqldatabase;

import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import se.uu.ub.cora.connection.ParameterConnectionProviderImp;
import se.uu.ub.cora.connection.SqlConnectionProvider;

public class RealDbTest {
	@Test(enabled = false)
	private void test() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://alvin-cora-docker-postgresql:5432/alvin", "alvin", "alvin");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "select * from country;";
		List<Object> values = new ArrayList<>();
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}

	@Test(enabled = false)
	private void testWithWhere() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://diva-cora-docker-postgresql:5432/diva", "diva", "diva");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "select * from organisation where organisation_id = ?;";
		// String sql = "select * from organisation ;";
		List<Object> values = new ArrayList<>();
		values.add(51);
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}

	@Test(enabled = false)
	private void testWithWhereName() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://diva-cora-docker-postgresql:5432/diva", "diva", "diva");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "select * from organisation where organisation_name= ?;";
		// String sql = "select * from organisation ;";
		List<Object> values = new ArrayList<>();
		values.add("Stockholms organisation");
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}

	@Test(enabled = false)
	private void testWithWherenot_eligible() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://diva-cora-docker-postgresql:5432/diva", "diva", "diva");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "select * from organisation where not_eligible= ?;";
		// String sql = "select * from organisation ;";
		List<Object> values = new ArrayList<>();
		values.add(false);
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}

	@Test(enabled = false)
	private void testUpdate() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://alvin-cora-docker-postgresql:5432/alvin", "alvin", "alvin");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "update country set defaultname = ? where alpha2code = 'SE';";
		List<Object> values = new ArrayList<>();
		values.add("fake name se");
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}

	@Test(enabled = false)
	private void testWithWhereAlvin() {
		SqlConnectionProvider sProvider = ParameterConnectionProviderImp.usingUriAndUserAndPassword(
				"jdbc:postgresql://alvin-cora-docker-postgresql:5432/alvin", "alvin", "alvin");
		DataReaderImp dataReaderImp = DataReaderImp.usingSqlConnectionProvider(sProvider);
		String sql = "select * from country where alpha2code= 'SE';";
		// String sql = "select * from organisation ;";
		List<Object> values = new ArrayList<>();
		// values.add(51);
		List<Map<String, Object>> result = dataReaderImp
				.executePreparedStatementQueryUsingSqlAndValues(sql, values);
		assertNotNull(result);
	}
}
