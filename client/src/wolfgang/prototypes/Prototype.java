package wolfgang.prototypes;

import java.sql.*;

public class Prototype {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		Statement stat = conn.createStatement();
		stat.executeUpdate("drop table if exists people;");
		stat.executeUpdate("create table people (name, occupation);");
		PreparedStatement prep = conn.prepareStatement("insert into people values (?, ?);");
		
		prep.setString(1, "Gandhi");
		prep.setString(2, "politics");
		prep.addBatch();
		prep.setString(1, "Touring");
		prep.setString(2, "computers");
		prep.addBatch();
		prep.setString(1, "Wittgenstein");
		prep.setString(2, "smartypandts");
		prep.addBatch();
		
		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);
		
		ResultSet rs = stat.executeQuery("select * from people;");
		while(rs.next())
			System.out.format("{people, \"%s\", \"%s}\n", rs.getString("name"), rs.getString("occupation"));
		rs.close();
		
		conn.close();
	}

}
