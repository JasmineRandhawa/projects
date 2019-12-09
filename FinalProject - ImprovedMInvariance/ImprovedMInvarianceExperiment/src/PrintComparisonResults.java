import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Common.Common;
import Common.Constants;
import DataBase.FetchDatabaseInfo;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

public class PrintComparisonResults {
	
	public static void PrintResults(List<EmployeeHealthCareInfo> originalMInvarianceAnonymized,
			List<EmployeeHealthCareInfo> improvedMInvarianceAnonymized,
			List<EmployeeHealthCareData> originalMInvarianceRemainingDisjoint, String mValue, String threshold)
			throws SQLException, IOException {
		List<EmployeeHealthCareData> dPrevious, dCurrent = new ArrayList<EmployeeHealthCareData>();
		List<EmployeeHealthCareInfo> dPreviousAnonymized = new ArrayList<EmployeeHealthCareInfo>();

		if (originalMInvarianceAnonymized != null && improvedMInvarianceAnonymized != null) {

			// initialize data in output file
			IntitalizeFileOutput();

			// print User input values to file
			PrintUserInput(Integer.parseInt(mValue), Integer.parseInt(mValue), Integer.parseInt(threshold));

			// fetch input data sets from the database
			FetchDatabaseInfo fetchData = new FetchDatabaseInfo();
			dPrevious = fetchData.GetPreviousData();
			dCurrent = fetchData.GetCurrentData();
			dPreviousAnonymized = fetchData.GetPreviousAnonymizedData();

			// Initial Stage : print input data set sets Fetched from the database
			PrintSequentialReleaseInputDataSetFromDatabase(dPrevious, dCurrent, dPreviousAnonymized);
			PrintBothAnonymizedData(originalMInvarianceAnonymized, improvedMInvarianceAnonymized,
					originalMInvarianceRemainingDisjoint);
		}
	}

	// initialize output HTML file with HTML file template CSS
	public static void IntitalizeFileOutput() throws IOException {
		String path = Constants.ComparisonHTMLOutputFilePath;
		String templatePath = Constants.HTMLOutputTemplateFilePath;
		String html = new String(Files.readAllBytes(Paths.get(templatePath)));

		PrintWriter out = new PrintWriter(path);
		out.print("");
		out.close();
		out = new PrintWriter(path);
		out.print(html);
		out.close();
	}

	// print user Input Values
	public static void PrintUserInput(int mValueForOriginal, int mValueForImproved, int threshold) throws IOException {

		Common common = new Common();
		String headingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";
		PrintResults(headingTag + "<b><h1>M Value for M-Invariance Original Anonymization : " + mValueForOriginal
				+ "</b></h1></span> " + headingTag + "<b><h1>M Value for M-Invariance Improved Anonymization : "
				+ mValueForImproved + "</b></h1></span> " + headingTag
				+ "<b><h1>Max No Of Fake Rows Allowed Threshold Value as : " + threshold + "</b></h1></span><br />",
				"");

	}

	// print remaining disjoint data set left if any and the final anonymized
	// m-invariance improved output
	public static void PrintBothAnonymizedData(List<EmployeeHealthCareInfo> dOriginalAnonymized,
			List<EmployeeHealthCareInfo> dImprovedAnonymized,
			List<EmployeeHealthCareData> dOriginalMInvarianceRemainingDisjoint) throws IOException {

		Common common = new Common();
		String tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		PrintResults("<b><h1>Comparison Stage : Compare Results  :</b></h1> " + "<br />", "");
		String dOriginalMInvarianceHtmlString = common.ConvertBucketIntoHTMLTableString(dOriginalAnonymized,
				tableHeadingTag + "Original Anonymized MInvariance Data</span>", "pinkTable", false, true);
		tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";
		String dOriginalMInvarianceRemainingDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(
				dOriginalMInvarianceRemainingDisjoint,
				tableHeadingTag + "Original Anonymized MInvariance remaining Disjoint Data</span>", "pinkTable", false,
				true);
		tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		String dImprovedMInvarianceHtmlString = common.ConvertBucketIntoHTMLTableString(dImprovedAnonymized,
				tableHeadingTag + "Improved MInvariance Anonymized Data</span>", "blueTable", false, true);
		PrintResults(dOriginalMInvarianceHtmlString + dOriginalMInvarianceRemainingDisjointHtmlString
				+ dImprovedMInvarianceHtmlString + "<br />", "Comparison Stage completed");
	}

	// append the input HTML string to the output HTML file
	public static void PrintResults(String htmlString, String consoleMessage) throws IOException {
		String fileLocation = Constants.ComparisonHTMLOutputFilePath;

		String html = new String(Files.readAllBytes(Paths.get(fileLocation)));
		html = html.replace("This is the Output", htmlString + "This is the Output");

		if (consoleMessage.contains("Comparison Stage")) {
			consoleMessage = consoleMessage + "\nOutput File created at location :" + fileLocation;
			html = html.replace("This is the Output", "");
		}

		PrintWriter out = new PrintWriter(fileLocation);
		out.print(html);
		out.close();

		System.out.println(consoleMessage);
	}

	// Print sequential release Input data sets fetched from the database
	public static void PrintSequentialReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent, List<EmployeeHealthCareInfo> dPreviousAnonymized)
			throws IOException {
		Common common = new Common();
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		PrintResults("<b><h1>Initial Stage : Get All Input Data Sets  :</b></h1> " + "<br />", "");
		String dPreviousHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dPrevious,
				tableHeadingTag + "Previous Data</span>", "blueTable", true, false);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		String dPreviousAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(dPreviousAnonymized,
				tableHeadingTag + "Previous Anonymized Data</span>", "blueTable", true, false);
		PrintResults(dPreviousHtmlString + dCurrentHtmlString + dPreviousAnonymizedHtmlString + "<br />",
				"Initial Stage : Get All Input Data Sets completed");
	}


}
