package ImprovedMInvarianceSequentialReleaseAnonymization;

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

import Common.Common;
import DataBase.InsertResultsIntoDatabase;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//M-Invariance for sequential release publication
//input as the previous release data, current release data , previous anonymized data , m-Value , max number of fake rows allowed threshold
public class SequentialPublicationAnonymization {

	public static List<EmployeeHealthCareInfo> GetAnonymizedData(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent, List<EmployeeHealthCareInfo> dPreviousAnonymized, String mValue,
			String maxNoOfFakeRowsAllowedThreshold) throws IOException, SQLException {

		LocalDateTime startTime = LocalDateTime.now();
		Helper helper = new Helper();
		Common common = new Common();

		// initialize data in output file
		common.IntitalizeFileOutput(false, true);

		// print User input values to file
		helper.PrintUserInput(mValue, maxNoOfFakeRowsAllowedThreshold);

		// Initial Stage : print input data set sets Fetched from the database
		helper.PrintSequentialReleaseInputDataSetFromDatabase(dPrevious, dCurrent, dPreviousAnonymized);

		// Intermediatory Stage One : Division Stage
		Division division = new Division();
		// create common data set for tuples with same signatures as that in previous
		// anonymized version
		List<EmployeeHealthCareData> dCommon = division.CreateCommonDataSet(dPrevious, dCurrent);
		// create unbalanced buckets with the common data set
		// initialize final dAnonymized data
		List<EmployeeHealthCareInfo> dAnonymized = new ArrayList<EmployeeHealthCareInfo>();
		List<EmployeeHealthCareInfo> unBalancedBuckets = new ArrayList<EmployeeHealthCareInfo>();
		dAnonymized = division.CreateBuckets(dCommon, dPreviousAnonymized);
		unBalancedBuckets.addAll(dAnonymized);
		List<EmployeeHealthCareData> remainingCurrent = new ArrayList<EmployeeHealthCareData>();
		List<Integer> consumedCurrent = new ArrayList<Integer>();
		for (EmployeeHealthCareInfo e : dAnonymized) {
			for (EmployeeHealthCareData empData : e.GroupInfo) {
				if (empData.EmployeeId != null) {
					List<EmployeeHealthCareData> cur = dCurrent.stream()
							.filter(em -> !em.EmployeeId.equals(empData.EmployeeId) && !consumedCurrent.contains(em.Id))
							.collect(Collectors.toList());
					remainingCurrent.addAll(cur);
					consumedCurrent.addAll(
							cur.stream().map(EmployeeHealthCareData::getId).distinct().collect(Collectors.toList()));
				}

			}
		}

		helper.PrintCommonDataSetAndUnbalancedBucketsData(dCommon, dAnonymized);
		// Intermediatory Stage Two : New Bucket Creation from disjoint data set
		// create disjoint data set
		List<EmployeeHealthCareData> dDisjoint = division.CreateDisjointDataSets(dPrevious, dCurrent);
		List<Integer> maxDisjointIdList = dDisjoint.stream().map(EmployeeHealthCareData::getId).distinct()
				.collect(Collectors.toList());
		common.SortedList(maxDisjointIdList);
		int maxDisjointId = maxDisjointIdList.get(maxDisjointIdList.size() - 1);
		for (EmployeeHealthCareData empData : remainingCurrent) {
			empData.Id = maxDisjointId + 1;
			maxDisjointId = maxDisjointId + 1;
		}
		List<Integer> currList = new ArrayList<Integer>();
		List<EmployeeHealthCareData> l = new ArrayList<EmployeeHealthCareData>();
		for (EmployeeHealthCareData disjoint : dDisjoint) {
			List<EmployeeHealthCareData> cur = remainingCurrent.stream()
					.filter(e -> !e.EmployeeId.equals(disjoint.EmployeeId) && !currList.contains(e.Id))
					.collect(Collectors.toList());
			if (cur.stream().count() > 0) {
				l.addAll(cur);
				currList.addAll(
						cur.stream().map(EmployeeHealthCareData::getId).distinct().collect(Collectors.toList()));
			}

		}
		dDisjoint.addAll(l);
		helper.PrintDisjointDataSet(dDisjoint);

		List<String> uniqueSenstiveAttributeValues = GetAllUniqueSenstiveAttributeValues(dAnonymized, dDisjoint);
		// Intermediatory Stage Two : Balance the unbalanced buckets with remaining
		// disjoint data set or fake Rows
		Balancing balance = new Balancing();
		List<List> balancedBucketsAndRemainingDisjointSet = balance.BalanceBuckets(dAnonymized, dDisjoint,
				Integer.parseInt(mValue));
		dDisjoint = balancedBucketsAndRemainingDisjointSet.get(0);
		dAnonymized = balancedBucketsAndRemainingDisjointSet.get(1);
		helper.PrintBalancedBucketsOutput(dAnonymized);

		// create new buckets if there is tuples in disjoint data set
		List<EmployeeHealthCareInfo> newBuckets = new ArrayList<EmployeeHealthCareInfo>();
		if (dDisjoint.stream().count() > 0) {
			List<Integer> groupIds = dAnonymized.stream().map(EmployeeHealthCareInfo::getGroupId)
					.collect(Collectors.toList());
			Collections.sort(groupIds);
			int maxGroupId = groupIds.get(groupIds.size() - 1);
			// create new buckets for disjoint DataSet
			NewBucketCreation newBucketCreation = new NewBucketCreation();

			List<List> newBucketsAndRemainingDisjointSet = newBucketCreation.AddNewBucketsForDisjointSets(dDisjoint,
					maxGroupId, Integer.parseInt(mValue), uniqueSenstiveAttributeValues);
			dDisjoint = newBucketsAndRemainingDisjointSet.get(0);
			newBuckets = newBucketsAndRemainingDisjointSet.get(1);

			helper.PrintNewBuckets(newBuckets);
		}
		helper.PrintNewBucketsAndExistingBuckets(newBuckets, dAnonymized);

		// Final Stage - Merge New Buckets with balanced Buckets to form final
		// anonymized data
		if (newBuckets.stream().count() > 0)
			dAnonymized.addAll(newBuckets);

		// check if anonymized m-invariance improved output satisfies threshold
		// constraints
		List<List> thresholdData = helper.CheckThreshold(dAnonymized, Integer.parseInt(mValue),
				Integer.parseInt(maxNoOfFakeRowsAllowedThreshold));

		List<EmployeeHealthCareData> remRecords = thresholdData.get(0);
		List<EmployeeHealthCareInfo> anonymizedData = thresholdData.get(1);
		if (remRecords.stream().count() > 0) {
			dDisjoint.addAll(remRecords);
		}

		// Analysis Stage- Evaluate anonymized m-invariance improved output
		// calculate number of distinct sensitive attribute values in remaining disjoint
		// set
		int noOfDistinctSensitiveAttributesInRemainingDisjointdata = (int) dDisjoint.stream()
				.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList()).stream().distinct().count();
		// calculate total number of fake rows
		int totalNoOfFakeRowsCount = 0;
		int initialNoOfFakeRowsCount = 0;
		int totalNumberOfEligibleRecordsPublished = 0;
		int totalPublishedRecordsOfCurrentRelease = 0;
		for (EmployeeHealthCareInfo empInfo : dAnonymized) {
			totalPublishedRecordsOfCurrentRelease = totalPublishedRecordsOfCurrentRelease
					+ (int) (empInfo.GroupInfo.stream().filter(emp -> !emp.EmployeeId.equals("Fake")).count());
			initialNoOfFakeRowsCount = (int) (initialNoOfFakeRowsCount
					+ (empInfo.GroupInfo.stream().filter(emp -> emp.EmployeeId.equals("Fake")).count()));

		}

		for (EmployeeHealthCareInfo empInfo : anonymizedData) {
			totalNumberOfEligibleRecordsPublished = totalNumberOfEligibleRecordsPublished
					+ (int) (empInfo.GroupInfo.stream().filter(emp -> !emp.EmployeeId.equals("Fake")).count());
			totalNoOfFakeRowsCount = (int) (totalNoOfFakeRowsCount
					+ (empInfo.GroupInfo.stream().filter(emp -> emp.EmployeeId.equals("Fake")).count()));
		}
		double div = ((double) totalNumberOfEligibleRecordsPublished / (double) totalPublishedRecordsOfCurrentRelease)
				+ 0.00;
		double percentageOfRecordsPublished = div * 100.00;

		div = ((double) totalNoOfFakeRowsCount / (double) initialNoOfFakeRowsCount) + 0.00;
		double percentageofFakeRows = 100.00 - (div * 100.00);

		helper.PrintRemainingDisjointDataSetAnonymizedData(dDisjoint, anonymizedData);
		double meanSquareError = common.CalculateMeanSquareError(anonymizedData);
		LocalDateTime endTime = LocalDateTime.now();

		Duration dur = Duration.between(startTime, endTime);
		System.out.println("Duration for Original MInvariance: " + (dur.getNano() / 1000000.0) + " milli seconds");
		helper.PrintAnalysisResults(totalNoOfFakeRowsCount, Integer.parseInt(mValue), initialNoOfFakeRowsCount,
				percentageofFakeRows, percentageOfRecordsPublished, totalNumberOfEligibleRecordsPublished,
				Integer.parseInt(maxNoOfFakeRowsAllowedThreshold), totalPublishedRecordsOfCurrentRelease,
				meanSquareError, dur.getNano());

		// Conclusion Stage- Conclude if anonymized data can be published or not
		common.PrintConclusionForReleasePublication(true, false, true);

		return dAnonymized;

	}

	// Get List of Unique Sensitive Attribute Values
	public static List<String> GetAllUniqueSenstiveAttributeValues(List<EmployeeHealthCareInfo> dAnonymized,
			List<EmployeeHealthCareData> dDisjoint) {
		List<String> sensitiveAttributeValues = dDisjoint.stream().map(EmployeeHealthCareData::getOccupation)
				.collect(Collectors.toList());
		for (EmployeeHealthCareInfo empInfo : dAnonymized) {
			sensitiveAttributeValues.addAll(
					empInfo.GroupInfo.stream().map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList()));
		}
		sensitiveAttributeValues = sensitiveAttributeValues.stream().distinct().collect(Collectors.toList());
		return sensitiveAttributeValues;
	}
}
