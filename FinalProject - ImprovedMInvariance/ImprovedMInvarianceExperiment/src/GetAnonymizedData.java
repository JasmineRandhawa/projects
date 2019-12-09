import java.util.ArrayList;
import java.util.List;
import DataBase.*;
import Common.Common;
import Common.Constants;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;
import MInvarianceSequentialReleaseAnonymization.*;
import LDiversitySingleReleaseAnonymization.LDiversity;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class GetAnonymizedData {

	public static void main(String[] args) throws IOException, SQLException {

		// truncate all the tables to store results
		System.out.println("Truncation Started");
		new TruncateTables().TruncateData();
		System.out.println("Truncation completed");

		// One time insert for the dataset for previous and current release for
		// m-Invariance
		System.out.println("InsertData Started");
		InsertResultsIntoDatabase.InsertFileData(Constants.sample1CSVFilePath, "sample1");
		InsertResultsIntoDatabase.InsertFileData(Constants.sample2CSVFilePath, "sample2");
		System.out.println("InsertData completed");

		// set the m or l value and the threshold
		String mOrLValue = "2";
		String threshold = "1";

		// create a l-diverse previous anonymized release dataset to be used as input
		// for m-Invariance
		// Output file created at ~/Results/LDiversityAnonymizationOutput.html
		System.out.println("LDiversity Previously Anonymized Table populated Started");
		List<EmployeeHealthCareInfo> lDiversityAnonymized = LDiversity.AnonymizeData(Integer.parseInt(mOrLValue));
		System.out.println("LDiversity Previously Anonymized Table Data insert Completed");

		// Original m-Invariance results
		// Output file created at ~/Results/OriginalMInvarianceAnonymizationOutput.html
		System.out.println("Original Minvarinace Anonymization Started");
		List<List> originalMInvarianceAnonymizedresult = new MInvariance().AnonymizeData(mOrLValue);
		List<EmployeeHealthCareData> originalMInvarianceRemainingDisjoint = originalMInvarianceAnonymizedresult.get(0);
		List<EmployeeHealthCareInfo> originalMInvarianceAnonymized = originalMInvarianceAnonymizedresult.get(1);
		System.out.println("Original Minvariance Anonymization Completed");

		// Improved m-Invariance results
		// Output file created at ~/Results/ImprovedMInvarianceAnonymizationOutput.html
		System.out.println("Improved Minvarinace Anonymization Started");
		List<EmployeeHealthCareInfo> improvedMInvarianceAnonymized = new ImprovedMInvarianceSequentialReleaseAnonymization.MInvariance()
				.AnonymizeData(mOrLValue, threshold);
		System.out.println("Improved Minvarinace Anonymization Completed");

	}

}
