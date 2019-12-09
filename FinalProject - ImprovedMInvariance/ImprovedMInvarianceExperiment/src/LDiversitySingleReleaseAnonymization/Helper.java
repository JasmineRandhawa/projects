package LDiversitySingleReleaseAnonymization;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Helper methods used to print anonymize data to HTML output file
public class Helper {

	Common common = new Common();

	// print user Input Values
	public void PrintUserInput(Integer lValue) throws IOException {
		String headingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";
		common.PrintResults(
				headingTag + "<b><h1>L-Diversity Anonymization with l Value as : " + lValue + "</b></h1></span><br />",
				"", false, false);
	}

	// Print single release Input data sets fetched from the database
	public void PrintSingleReleaseInputDataSetFromDatabase(List<EmployeeHealthCareData> dCurrent) throws IOException {
		String tableHeadingTag = "<span " + "style=\"color:darkblue	;\"" + ">";
		common.PrintResults("<b><h1>Initial Stage : Get Input Data Set  :</b></h1> " + "<br />", "", false, false);
		String dCurrentHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dCurrent,
				tableHeadingTag + "Current Data</span>", "blueTable", true, false);
		common.PrintResults(dCurrentHtmlString + "<br />", "Initial Stage : Get Input Data Set completed", false,
				false);
	}

	// print remaining unconsumed input data set left if any and the final
	// anonymized
	// l-diversity output
	public void PrintUnconsumedInputDataSetAnonymizedData(List<EmployeeHealthCareData> dUnConsumed,
			List<EmployeeHealthCareInfo> dAnonymized) throws IOException {
		common.PrintResults("<b><h1>Final Stage : Create Anonymized Datset</b></h1> " + "<br />", "", false, false);
		String tableHeadingTag = "<span " + "style=\"color:darkgreen	;\"" + ">";
		if (dUnConsumed.stream().count() > 0) {

			String dDisjointHtmlString = common.ConvertPreviouCurrentListIntoHTMLTableString(dUnConsumed,
					tableHeadingTag + "Remaining Unconsumed Input Data</span>", "blueTable", true, true);
			common.PrintResults(dDisjointHtmlString, "", false, false);
		}
		String dAnonymizedHtmlString = common.ConvertBucketIntoHTMLTableString(dAnonymized,
				tableHeadingTag + "Anonymized Data</span>", "greenTable", true, true);
		common.PrintResults(dAnonymizedHtmlString, "Final Stage : Create Anonymized Datset completed", false, false);
	}

	public static List<List> GetGroupInfoWithUniqueTuples(List<EmployeeHealthCareData> groupInfo,
			EmployeeHealthCareData empData, List<EmployeeHealthCareData> unConsumedData) {
		String occupation = empData.getOccupation();
		// add empData item to group only if it has sensitive attribute is different
		// from
		// the items already added to group
		List<EmployeeHealthCareData> groupInfoFormed = groupInfo.stream()
				.filter(emp -> !emp.Occupation.toUpperCase().equals(occupation.toUpperCase()))
				.collect(Collectors.toList());
		if (groupInfoFormed != null && groupInfoFormed.stream().count() == groupInfo.stream().count()) {
			groupInfo.add(empData);
			for (EmployeeHealthCareData emp : groupInfo)
				if (unConsumedData.contains(emp))
					unConsumedData.remove(emp);
		}
		List<List> result = new ArrayList<List>();
		result.add(groupInfo);
		result.add(unConsumedData);
		return result;
	}

	public List<List> GetEquivalentSet(int lValue, List<EmployeeHealthCareData> dCurrent) {
		List<List> result = new ArrayList<List>();
		List<EmployeeHealthCareData> groupInfo = new ArrayList<EmployeeHealthCareData>();
		List<EmployeeHealthCareData> unConsumedData = new ArrayList<EmployeeHealthCareData>();

		// check if group can be formed
		unConsumedData.addAll(dCurrent);
		if (dCurrent.stream().count() >= lValue) {
			// add first item of list to group
			groupInfo.add(dCurrent.get(0));
			int count = (int) groupInfo.stream().count();
			int i = 1;
			while (count <= lValue) {
				if (count == lValue)
					break;
				int dCurrentCount = (int) dCurrent.stream().count();
				if (i < dCurrentCount) {
					List<List> res = GetGroupInfoWithUniqueTuples(groupInfo, dCurrent.get(i), unConsumedData);
					groupInfo = res.get(0);
					unConsumedData = res.get(1);
					i = i + 1;
					count = (int) groupInfo.stream().count();
				}
			}
		}
		result.add(groupInfo);
		result.add(unConsumedData);
		return result;
	}

}
