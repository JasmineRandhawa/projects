package Common;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Constants {
	
	public static String LDiversityHTMLOutputFilePath = "Results/LDiversityAnonymizationOutput.html";
	public static String OriginalMInvarianceHTMLOutputFilePath = "Results/OriginalMInvarianceAnonymizationOutput.html";
	public static String ImprovedMInvarianceHTMLOutputFilePath = "Results/ImprovedMInvarianceAnonymizationOutput.html";
	public static String ComparisonHTMLOutputFilePath = "Results/CompareAnonymizationOutput.html";
	public static String HTMLOutputTemplateFilePath = "Results/AnonymizationOutputTemplate.html";
	
	public static String SqlServerPath = "jdbc:mysql://127.0.0.01:3306/test";
	
	private static String dataBaseUserName = "Jasmine";
	private static String databasePassWord = "test";
	private static String dataBaseName = "test";
	private static String dataBaseServerName = "localhost";
	private static String dataBaseServerPortNumber = "3306";
	
	public static Statement GetSqlCommand() throws SQLException {
		return DriverManager.getConnection(
				"jdbc:mysql://" + dataBaseServerName + ":" + dataBaseServerPortNumber + "/" + dataBaseName,
				dataBaseUserName, databasePassWord).createStatement();
	}
	public static String sample1CSVFilePath ="Dataset/sample1.csv";
	public static String sample2CSVFilePath ="Dataset/sample2.csv";
	public static String Sample1SelectQuery = "SELECT distinct * FROM sample1 limit 300";
	public static String Sample2SelectQuery = "SELECT distinct * FROM sample2 limit 600";
	public static String PreviousAnonymizedTableGroupIdQuery = "SELECT  distinct GroupId FROM PreviousAnonymizedSample1";

}
