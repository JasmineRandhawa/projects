package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import Common.Constants;

//Fetch all input data sets ( current. previous and previous anonymized) from the database in form of list of class objects
public class TruncateTables {

	public void TruncateData() throws SQLException {
		String myUrl = Constants.SqlServerPath;
		Connection conn = DriverManager.getConnection(myUrl, "", "");

		try {
			Statement statement = conn.createStatement();
			statement.executeUpdate("truncate table sample1;");
			statement.executeUpdate("truncate table sample2;");
			statement.executeUpdate("truncate table PreviousAnonymizedSample1;");
		} finally {
			conn.close();
		}
	}

}
