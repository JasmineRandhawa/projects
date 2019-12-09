package DataBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import Common.Constants;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Fetch all input data sets ( current. previous and previous anonymized) from the database in form of list of class objects
public class InsertResultsIntoDatabase {

	public void InsertAnonymizedIntoDb(List<EmployeeHealthCareInfo> dataList, String tableName) throws SQLException {
		String myUrl = Constants.SqlServerPath;
		Connection conn = DriverManager.getConnection(myUrl, "", "");

		try {
			for (EmployeeHealthCareInfo empInfo : dataList) {
				for (EmployeeHealthCareData data : empInfo.GroupInfo) {
					// the mysql insert statement
					String query = " INSERT INTO " + tableName
							+ "  (GroupId,EmployeeId, Age, Occupation)" + " values (?, ?, ?,?)";

					// create the mysql insert preparedstatement
					PreparedStatement preparedStmt = conn.prepareStatement(query);
					preparedStmt.setInt(1, empInfo.GroupId);
					preparedStmt.setString(2, data.EmployeeId);
					preparedStmt.setString(3, data.Age);
					preparedStmt.setString(4, data.Occupation);

					// execute the preparedstatement
					preparedStmt.execute();
				}
			}

		} finally {
			conn.close();
		}
	}

	public static void InsertInputOrRemainingData(List<EmployeeHealthCareData> dataList, String tableName)
			throws SQLException {
		String myUrl = "jdbc:mysql://127.0.0.01:3306/test";
		Connection conn = DriverManager.getConnection(myUrl, "", "");

		try {
			for (EmployeeHealthCareData data : dataList) {
				// the mysql insert statement
				String query = "INSERT INTO " + tableName
						+ " (EmployeeId, Age, Occupation)" + " values (?, ?, ?)";

				// create the mysql insert preparedstatement
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setInt(1, Integer.parseInt(data.EmployeeId));
				preparedStmt.setInt(2, Integer.parseInt(data.Age));
				preparedStmt.setString(3, data.Occupation);

				// execute the preparedstatement
				preparedStmt.execute();

			}

		} finally {
			conn.close();
		}
	}

	public static void InsertFileData(String filePath, String tableName)
			throws SQLException {
	File  csvFile = new File(filePath);
	BufferedReader br = null;
	String line = "";
	String cvsSplitBy = ",";
	String myUrl = Constants.SqlServerPath;
	Connection conn = DriverManager.getConnection(myUrl, "", "");
	try {

		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {

			// use comma as separator
			String[] data = line.split(cvsSplitBy);
			if (data[0].equals("EmployeeId")) {
				int i = 0;
				continue;
			}
			// the mysql insert statement
			String query = "   INSERT INTO "+ tableName+" (EmployeeId, Age, Occupation)" + " values (?, ?, ?)";

			// create the mysql insert preparedstatement
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setInt(1, Integer.parseInt(data[0].trim()));
			preparedStmt.setInt(2, Integer.parseInt(data[1].trim()));
			preparedStmt.setString(3, data[2].trim());

			// execute the preparedstatement
			preparedStmt.execute();

		}

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		conn.close();
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

}
