package MInvarianceSequentialReleaseAnonymization;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Helper methods used to print anonymize data to HTML output file
public class Helper {

	Common common = new Common();

	// print user Input Values
	public void PrintUserInput(String mValue) throws IOException {
		String headingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";
		common.PrintResults(headingTag + "<b><h1>M-Invariance Anonymization with m Value as : " + mValue
				+ "</b></h1></span><br /> ", "", true, true);
	}

	// Print sequential release Input data sets fetched from the database
	public void PrintSequentialReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent, List<EmployeeHealthCareInfo> dPreviousAnonymized)
			throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		common.PrintResults("<b><h1>Initial Stage : Get All Input Data Sets  :</b></h1> " + "<br />", "", true, true);
		String dPreviousHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dPrevious,
				tableHeadingTag + "Previous Data</span>", "blueTable", true, false);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		String dPreviousAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(dPreviousAnonymized,
				tableHeadingTag + "Previous Anonymized Data</span>", "blueTable", true, false);
		common.PrintResults(dPreviousHtmlString + dCurrentHtmlString + dPreviousAnonymizedHtmlString + "<br />",
				"Initial Stage : Get All Input Data Sets completed", true, true);
	}

	// Print single release Input data sets fetched from the database
	public void PrintSingleReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dCurrent) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		common.PrintResults("<b><h1>Initial Stage : Get Input Data Set  :</b></h1> " + "<br />", "", true, true);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		common.PrintResults(dCurrentHtmlString + "<br />", "Initial Stage : Get Input Data Set completed", true, true);
	}

	// print Common DataSet and Unbalanced Buckets
	public void PrintCommonDataSetAndUnbalancedBucketsData(List<EmployeeHealthCareData> dCommon,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults(
				"<b><h1>Intermediatory Stage 2 : Division pahse : Create Unbalanced Buckets with Common Dataset : </b></h1> "
						+ "<br />",
				"", true, true);
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";

		String dCommonHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCommon,
				tableHeadingTag + "Common DataSet with Same Signature as Previously Anonymized Data</span>",
				"pinkTable", false, false);
		common.PrintResults(dCommonHtmlString, "", true, true);

		String dUnbalancedBucketsHtmlString = common.ConvertBucketIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Unbalanced Buckets</span>", "pinkTable", false, false);

		common.PrintResults(dUnbalancedBucketsHtmlString,
				"Intermediatory Stage 1 : Create Buckets with Common Dataset completed", true, true);
	}

	// print Disjoint data set
	public void PrintDisjointDataSet(List<EmployeeHealthCareData> dDisjoint) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";
		common.PrintResults(
				"<b><h1>Intermediatory Stage 2 : Bucket Creation Phase : Create New Buckets with Disjoint Dataset : </b></h1> "
						+ "<br />",
				"", true, true);

		if (dDisjoint != null) {
			String dDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dDisjoint,
					tableHeadingTag + "Disjoint DataSet: New Tuples That are Added</span>", "pinkTable", false, false);
			common.PrintResults(dDisjointHtmlString, "", true, true);
		}
	}

	// print new buckets if created
	public void PrintNewBuckets(List<EmployeeHealthCareInfo> newBuckets) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";

		String dNewBucketsStringHtmlString;
		if (newBuckets != null && newBuckets.stream().count() > 0) {
			dNewBucketsStringHtmlString = common.ConvertBucketIntoHTMLTableString(newBuckets,
					tableHeadingTag + "New Buckets</span>", "pinkTable", false, false);
			common.PrintResults(dNewBucketsStringHtmlString + "<br />",
					"Intermediatory Stage 3 : Create New Buckets with Disjoint Dataset completed", true, true);
		}
	}

	// print the balanced buckets
	public void PrintBalancedBucketsOutput(List<EmployeeHealthCareInfo> balancedBuckets) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:Chocolate	;\"" + ">";
		common.PrintResults(
				"<b><h1>Intermediatory Stage 3 : Balance the buckets with remaining Disjoint Dataset and Fake Rows : </b></h1> "
						+ "<br />",
				"", true, true);

		String dAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(balancedBuckets,
				tableHeadingTag + "Balanced Buckets</span>", "pinkTable", false, false);
		common.PrintResults(dAnonymizedHtmlString + "<br />",
				"Intermediatory Stage 3 : Balance the buckets with remaining Disjoint Dataset and Fake Rows completed",
				true, true);

	}

	// print remaining disjoint data set left if any and the final anonymized
	// m-invariance improved output
	public void PrintRemainingDisjointDataSetAnonymizedData(List<EmployeeHealthCareData> dDisjoint,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults(
				"<b><h1>Final Stage : Create Anonymized Datset by merging Balanced Buckets and New Buckets </b></h1> "
						+ "<br />",
				"", true, true);
		String tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		if (dDisjoint.stream().count() > 0) {

			String dDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dDisjoint,
					tableHeadingTag + "Remaining Unpaired Data</span>", "blueTable", false, true);
			common.PrintResults(dDisjointHtmlString, "", true, true);
		}
		String dAnonymizedHtmlString = common.ConvertAnonymizedListIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Anonymized Data</span>", "greenTable", false, true);
		common.PrintResults(dAnonymizedHtmlString,
				"Final Stage : Create Anonymized Datset by merging Balanced Buckets and New Buckets completed", true,
				true);
	}

	// print the final analysis result for anonymized m-invariance output
	public void PrintAnalysisResults(int totalNoOfFakeRowsCount, int mValue, int remainingDisjointDataSetCount,
			int totalPublishedRecordsOfCurrentRelease, double meanSquareError
			,int duration) throws IOException {
		common.PrintResults("<b><h1>Analysis Stage for anonymized m-invariance original output</b></h1> ", "", true,
				true);

		String tableHeadingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";

		common.PrintResults(tableHeadingTag + "<b>Input M-Invariance Value  : " + mValue + "</b></span><br />", "",
				true, true);
		if (remainingDisjointDataSetCount > 0) {
			common.PrintResults(tableHeadingTag + "<b>Number of tuples in remaining Disjoint Data  : "
					+ remainingDisjointDataSetCount + "</b></span><br />", "", true, true);
		}
		common.PrintResults(
				tableHeadingTag + "<b>Total Number of Fake Rows :  " + totalNoOfFakeRowsCount + "</b></span><br />", "",
				true, true);
		common.PrintResults(tableHeadingTag + "<b>Mean Square Error: " + meanSquareError + "</b></span><br />", "",
				true, true);
		common.PrintResults(
				tableHeadingTag + "<b>Duration for Original MInvariance: " + (duration/ 1000000.0) +" milli seconds</b></span><br />",
				"", true, true);
		
		common.PrintResults(
				tableHeadingTag + "<b>Total Records Published of Current Release: "
						+ totalPublishedRecordsOfCurrentRelease + "</b></span><br />",
				"Analysis Stage completed", true, true);

	}

	// check if anonymized m-invariance output satisfies threshold
	// constraints
	public Boolean CheckThreshold(int mValue, List<EmployeeHealthCareData> dDisjoint) {
		int dDisjointCount = (int) dDisjoint.stream().count();
		List<String> sensitiveAttributeValues = dDisjoint.stream().map(EmployeeHealthCareData::getOccupation).distinct()
				.collect(Collectors.toList());
		double div = (double) dDisjointCount / mValue;
		int threshold = (int) Math.ceil(div);
		for (String value : sensitiveAttributeValues) {
			int count = (int) dDisjoint.stream().filter(emp -> emp.Occupation.equalsIgnoreCase(value)).distinct()
					.collect(Collectors.toList()).stream().count();
			if (count > threshold)
				return false;
		}

		return true;
	}

}
