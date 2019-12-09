package LDiversitySingleReleaseAnonymization;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.stream.Collectors;
import Common.*;
import DataBase.InsertResultsIntoDatabase;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

public class SingleReleaseLDiversityAnonymization {

	public static List<EmployeeHealthCareInfo> GetAnonymizedData(List<EmployeeHealthCareData> dCurrent, Integer lValue)
			throws IOException, SQLException {

		LocalDateTime startTime = LocalDateTime.now();
		Helper helper = new Helper();
		Common common = new Common();

		// Initialize output HTML file
		common.IntitalizeFileOutput(false, false);

		helper.PrintUserInput(lValue);

		// Print Input Data set on output HTML file
		helper.PrintSingleReleaseInputDataSetFromDatabase(dCurrent);

		List<EmployeeHealthCareInfo> dAnonymized = new ArrayList<EmployeeHealthCareInfo>();
		List<EmployeeHealthCareData> unConsumedData = new ArrayList<EmployeeHealthCareData>();
		// sort list by Quasi Identifiers
		List<String> ages = dCurrent.stream().map(EmployeeHealthCareData::getAge).distinct()
				.collect(Collectors.toList());
		List<Integer> sortedAgeslist = common.SortedStringList(ages);
		List<EmployeeHealthCareData> sortedData = new ArrayList<EmployeeHealthCareData>();
		for (Integer age : sortedAgeslist) {
			sortedData.addAll(
					dCurrent.stream().filter(curr -> curr.Age.equals(age.toString())).collect(Collectors.toList()));
		}
		dCurrent = new ArrayList<EmployeeHealthCareData>();
		dCurrent.addAll(sortedData);
		// computer total number of distinct sensitive attribute values
		List<String> sensitiveAttributes = dCurrent.stream().map(EmployeeHealthCareData::getOccupation)
				.collect(Collectors.toList()).stream().map(String::toUpperCase).distinct().collect(Collectors.toList());

		int noOfDistinctSensitiveAttributes = (int) sensitiveAttributes.stream().count();
		int count = (int) dCurrent.stream().count();

		double div = (double) count / lValue;
		int numberOfGroups = (int) Math.ceil(div);
		// check if L-diversity is feasible
		List<EmployeeHealthCareData> currList = new ArrayList<EmployeeHealthCareData>();
		for (EmployeeHealthCareData emp : dCurrent) {
			List<String> empIdlist = currList.stream().map(EmployeeHealthCareData::getEmployeeId).distinct()
					.collect(Collectors.toList());
			if (!empIdlist.contains(emp.EmployeeId))
				currList.add(emp);
		}
		if (noOfDistinctSensitiveAttributes < lValue) {
			common.PrintConclusionForReleasePublication(false, false, false);
		} else {
			unConsumedData.addAll(currList);
			// create equivalent Groups satisfying uniqueness l diversity
			for (int i = 1; i <= numberOfGroups; i++) {
				// computer total number of distinct sensitive attribute values
				int noOfDistinctSensitiveAttributesLeft = (int) unConsumedData.stream()
						.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList()).stream()
						.map(String::toUpperCase).distinct().count();
				if ((noOfDistinctSensitiveAttributesLeft >= lValue)) {
					EmployeeHealthCareInfo empInfo = new EmployeeHealthCareInfo();
					empInfo.GroupId = i;
					List<List> result = helper.GetEquivalentSet(lValue, unConsumedData);
					List<EmployeeHealthCareData> groupInfo = result.get(0);
					unConsumedData = result.get(1);
					if (groupInfo != null && groupInfo.stream().count() > 0) {
						empInfo.GroupInfo = groupInfo;
						empInfo.GroupInfoCount = (int) groupInfo.stream().count();
						dAnonymized.add(empInfo);
					}
				}
			}
		}

		for (EmployeeHealthCareInfo empinfo : dAnonymized) {
			// assign equal range of quasi identifiers to tuples of all balanced buckets
			int maxAge = common.GetMaxAgeValue(empinfo.GroupInfo);
			int minAge = common.GetMinAgeValue(empinfo.GroupInfo);
			for (EmployeeHealthCareData emp : empinfo.GroupInfo) {
				if (minAge == maxAge)
					emp.Age = minAge + "-" + (minAge + 5);
				else
					emp.Age = minAge + "-" + maxAge;
			}
		}

		// write output to HTML file
		helper.PrintUnconsumedInputDataSetAnonymizedData(unConsumedData, dAnonymized);

		// store results in db
		InsertResultsIntoDatabase db = new InsertResultsIntoDatabase();
		db.InsertAnonymizedIntoDb(dAnonymized, "PreviousAnonymizedSample1");

		return dAnonymized;

	}

}
