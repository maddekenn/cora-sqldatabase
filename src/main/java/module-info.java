module se.uu.ub.cora.sqldatabase {
	requires transitive java.naming;
	requires transitive java.sql;

	exports se.uu.ub.cora.connection;
	exports se.uu.ub.cora.sqldatabase;
}