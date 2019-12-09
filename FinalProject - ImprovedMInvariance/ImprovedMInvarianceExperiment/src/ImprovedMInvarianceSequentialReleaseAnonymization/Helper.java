package ImprovedMInvarianceSequentialReleaseAnonymization;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Helper methods used to print anonymize data to HTML output file
public class Helper {

	Common common = new Common();

	// print user Input Values
	public void PrintUserInput(String mValue, String maxNoOfFakeRowsAllowedThreshold) throws IOException {
		String headingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";
		common.PrintResults(headingTag + "<b><h1>M-Invariance Anonymization with m Value as : " + mValue
				+ "</b></h1></span> " + headingTag + "<b><h1>Max No Of Fake Rows Allowed Threshold Value as : "
				+ maxNoOfFakeRowsAllowedThreshold + "</b></h1></span><br />", "", false, true);
	}

	// Print sequential release Input data sets fetched from the database
	public void PrintSequentialReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent, List<EmployeeHealthCareInfo> dPreviousAnonymized)
			throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		common.PrintResults("<b><h1>Initial Stage : Get All Input Data Sets  :</b></h1> " + "<br />", "", false, true);
		String dPreviousHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dPrevious,
				tableHeadingTag + "Previous Data</span>", "blueTable", true, false);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		String dPreviousAnonymizedHtmlString = common.ConvertAnonymizedListIntoHTMLTableString(dPreviousAnonymized,
				tableHeadingTag + "Previous Anonymized Data</span>", "blueTable", true, false);
		common.PrintResults(dPreviousHtmlString + dCurrentHtmlString + dPreviousAnonymizedHtmlString + "<br />",
				"Initial Stage : Get All Input Data Sets completed", false, true);
	}

	// Print single release Input data sets fetched from the database
	public void PrintSingleReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dCurrent) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		common.PrintResults("<b><h1>Initial Stage : Get Input Data Set  :</b></h1> " + "<br />", "", false, true);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		common.PrintResults(dCurrentHtmlString + "<br />", "Initial Stage : Get Input Data Set completed", false, true);
	}

	// print Common DataSet and Unbalanced Buckets
	public void PrintCommonDataSetAndUnbalancedBucketsData(List<EmployeeHealthCareData> dCommon,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults(
				"<b><h1>Intermediatory Stage 1 : Division pahse : Create Unbalanced Buckets with Common Dataset : </b></h1> "
						+ "<br />",
				"", false, true);
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";

		String dCommonHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCommon,
				tableHeadingTag + "Common DataSet with Same Signature as Previously Anonymized Data</span>",
				"pinkTable", false, false);
		common.PrintResults(dCommonHtmlString, "", false, true);

		String dUnbalancedBucketsHtmlString = common.ConvertBucketIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Unbalanced Buckets</span>", "pinkTable", false, false);

		common.PrintResults(dUnbalancedBucketsHtmlString,
				"Intermediatory Stage 1 : Create Buckets with Common Dataset completed", false, true);
	}

	// print Disjoint data set
	public void PrintDisjointDataSet(List<EmployeeHealthCareData> dDisjoint) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";
		common.PrintResults(
				"<b><h1>Intermediatory Stage 2 : Balance the buckets with remaining Disjoint Dataset and Fake Rows : </b></h1> "
						+ "<br />",
				"", false, true);

		if (dDisjoint != null) {
			String dDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dDisjoint,
					tableHeadingTag + "Disjoint DataSet: New Tuples That are Added</span>", "pinkTable", false, false);
			common.PrintResults(dDisjointHtmlString, "", false, true);
		}
	}

	// print the balanced buckets
	public void PrintBalancedBucketsOutput(List<EmployeeHealthCareInfo> balancedBuckets) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";

		String dAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(balancedBuckets,
				tableHeadingTag + "Balanced Buckets</span>", "pinkTable", false, false);
		common.PrintResults(dAnonymizedHtmlString + "<br />",
				"Intermediatory Stage 2 : Balance the buckets with  Disjoint Dataset and Fake Rows completed", false,
				true);

	}

	// print new buckets if created
	public void PrintNewBuckets(List<EmployeeHealthCareInfo> newBuckets) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";
		common.PrintResults(
				"<b><h1>Intermediatory Stage 3 : Bucket Creation Phase : Create New Buckets with Disjoint Dataset : </b></h1> "
						+ "<br />",
				"", false, true);

		String dNewBucketsStringHtmlString;
		if (newBuckets != null && newBuckets.stream().count() > 0) {
			dNewBucketsStringHtmlString = common.ConvertBucketIntoHTMLTableString(newBuckets,
					tableHeadingTag + "New Buckets</span>", "pinkTable", false, false);
			common.PrintResults(dNewBucketsStringHtmlString + "<br />",
					"Intermediatory Stage 3: Create New Buckets with remaining Disjoint Dataset completed", false,
					true);
		}
	}

	// try adjusting new buckets tuples to existing ones
	public void PrintNewBucketsAndExistingBuckets(List<EmployeeHealthCareInfo> newBuckets,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults(
				"<b><h1>Intermediatory Stage 4 : Try Adjusting New Bucket tuples to Existing Buckets : </b></h1> "
						+ "<br />",
				"", false, true);
		String tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		if (newBuckets.stream().count() > 0) {

			String dNewBucketsString = common.ConvertBucketIntoHTMLTableString(newBuckets,
					tableHeadingTag + "Remaining New Buckets</span>", "greenTable", false, false);
			common.PrintResults(dNewBucketsString, "", false, true);
		}
		String dAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Anonymized Data</span>", "greenTable", false, false);
		common.PrintResults(dAnonymizedHtmlString,
				"Intermediatory Stage 4 : Try adjusting new bucket tuples to the existing ones completed", false, true);
	}

	// print remaining disjoint data set left if any and the final anonymized
	// m-invariance improved output
	public void PrintRemainingDisjointDataSetAnonymizedData(List<EmployeeHealthCareData> dDisjoint,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults(
				"<b><h1>Final Stage : Create Anonymized Datset by merging Balanced Buckets and New Buckets </b></h1> "
						+ "<br />",
				"", false, true);
		String tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		if (dDisjoint != null && dDisjoint.stream().count() > 0) {

			String dDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dDisjoint,
					tableHeadingTag + "Remaining Unpaired Data</span>", "blueTable", false, true);
			common.PrintResults(dDisjointHtmlString, "", false, true);
		}
		String dAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Anonymized Data</span>", "greenTable", false, true);
		common.PrintResults(dAnonymizedHtmlString,
				"Final Stage : Create Anonymized Datset by merging Balanced Buckets and New Buckets completed", false,
				true);
	}

	// print the final analysis result for anonymized m-invariance improved output
	public void PrintAnalysisResults(int totalNoOfFakeRowsCount, int mValue,
			int initialNoOfFakeRowsCount, double percentageofFakeRows,
			double percentageOfRecordsPublished,
			int totalNumberOfRecordsPublished,
			int maxNumberOfFakeRowsAllowedThreshold,
			int totalPublishedRecordsOfCurrentRelease,
			double meanSquareError, int duration) throws IOException {
		common.PrintResults("<b><h1>Analysis Stage for anonymized m-invariance improved output</b></h1> ", "", false,
				true);

		String tableHeadingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";

		common.PrintResults(tableHeadingTag + "<b>Input M-Invariance Value  : " + mValue + "</b></span><br />", "",
				false, true);
		common.PrintResults(tableHeadingTag + "<b>Threshold for Maximum Number of Fake Rows Allowed  : "
				+ maxNumberOfFakeRowsAllowedThreshold + "</b></span><br />", "", false, true);
		
		common.PrintResults(
				tableHeadingTag + "<b>Intital Total Number of Fake Rows :  " + initialNoOfFakeRowsCount + "</b></span><br />", "",
				false, true);
		
		common.PrintResults(
				tableHeadingTag + "<b>Total Number of Fake Rows :  " + totalNoOfFakeRowsCount + "</b></span><br />", "",
				false, true);
		common.PrintResults(
				tableHeadingTag + "<b>Percentage Of Fake Rows Reduced :  " + percentageofFakeRows + "</b></span><br />", "",
				false, true);
		common.PrintResults(tableHeadingTag + "<b>Mean Square Error: " + meanSquareError + "</b></span><br />", "",
				false, true);
		common.PrintResults(tableHeadingTag + "<b>Duration for Improved MInvariance: " + (duration / 1000000.0)
				+ " milli	 seconds</b></span><br />", "", false, true);
		common.PrintResults(tableHeadingTag + "<b>Total Number of Records Published Rows: "
				+ totalNumberOfRecordsPublished + "</b></span><br />", "", false, true);
		common.PrintResults(
				tableHeadingTag + "<b>Total Records: "
						+ totalPublishedRecordsOfCurrentRelease + "</b></span><br />",
				"", false, true);
		common.PrintResults(tableHeadingTag + "<b>Percentage Of Rescords Published: " + percentageOfRecordsPublished
				+ "</b></span><br />", "Analysis Stage completed", false, true);
	}

	// check if anonymized m-invariance improved output satisfies threshold
	// constraints
	public List<List> CheckThreshold(List<EmployeeHealthCareInfo> dAnonymized, int mValue,
			int maxNumberOfFakeRowsAllowedThreshold) {
		List<EmployeeHealthCareInfo> anonymizedData = new ArrayList<EmployeeHealthCareInfo>();
		List<EmployeeHealthCareData> remainingRecords = new ArrayList<EmployeeHealthCareData>();
		for (EmployeeHealthCareInfo empInfo : dAnonymized) {
			int noOfFakeRowsInAnonymizedDataSetBucket = (int) (empInfo.GroupInfo.stream()
					.filter(emp -> emp.EmployeeId.equals("Fake")).count());
			if (noOfFakeRowsInAnonymizedDataSetBucket <= maxNumberOfFakeRowsAllowedThreshold)
				anonymizedData.add(empInfo);
			else {
				for (EmployeeHealthCareData empData : empInfo.GroupInfo) {
					if (!empData.EmployeeId.equalsIgnoreCase("Fake"))
						remainingRecords.add(empData);
				}

			}
		}

		List<List> result = new ArrayList<List>();
		result.add(remainingRecords);
		result.add(anonymizedData);
		return result;
	}

}
