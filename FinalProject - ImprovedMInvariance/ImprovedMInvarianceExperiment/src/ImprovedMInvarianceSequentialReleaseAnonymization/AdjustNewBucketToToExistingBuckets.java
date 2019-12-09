package ImprovedMInvarianceSequentialReleaseAnonymization;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

// Create New Buckets
public class AdjustNewBucketToToExistingBuckets {

	Common common = new Common();

	// Adjust new bucket tuples to existing ones
	public List<List> AdjustNewBuckets(List<EmployeeHealthCareInfo> newBuckets,
			List<EmployeeHealthCareInfo> existingBuckets, Integer mValue, List<String> sensitiveAttributes) {
		List<List> result = new ArrayList<List>();
		List<EmployeeHealthCareInfo> buckets = new ArrayList<EmployeeHealthCareInfo>();
		for (EmployeeHealthCareInfo bucket : existingBuckets) {
			EmployeeHealthCareInfo latestBucket = new EmployeeHealthCareInfo();
			latestBucket.GroupId = bucket.GroupId;
			latestBucket.GroupInfoCount = bucket.GroupInfoCount;
			latestBucket.NoOfFakeRows = bucket.NoOfFakeRows;
			latestBucket.GroupInfo = new ArrayList<EmployeeHealthCareData>();
			latestBucket.GroupInfo.addAll(bucket.GroupInfo);
			for (EmployeeHealthCareData empData : bucket.GroupInfo) {
				List uniqueSensitiveValues = latestBucket.GroupInfo.stream().map(EmployeeHealthCareData::getOccupation)
						.distinct().collect(Collectors.toList());

				EmployeeHealthCareData newBucketData = GetMatchingNewBucketTuple(empData, newBuckets,
						uniqueSensitiveValues);
				if (newBucketData != null) {
					if (empData.EmployeeId.equalsIgnoreCase("FAKE")
							&& empData.Occupation.equalsIgnoreCase(newBucketData.Occupation)) {
						latestBucket.GroupInfo.remove(empData);
						latestBucket.GroupInfo.add(newBucketData);
						latestBucket.NoOfFakeRows = latestBucket.NoOfFakeRows - 1;
					} else if (empData.EmployeeId.equalsIgnoreCase("FAKE")
							&& !empData.Occupation.equalsIgnoreCase(newBucketData.Occupation)) {
						latestBucket.GroupInfo.add(newBucketData);
						latestBucket.GroupInfoCount = latestBucket.GroupInfoCount + 1;
					}
					List<String> Ids = latestBucket.GroupInfo.stream().map(EmployeeHealthCareData::getEmployeeId)
							.distinct().collect(Collectors.toList());
					if (!empData.EmployeeId.equalsIgnoreCase("FAKE") && !Ids.contains(newBucketData.EmployeeId)) {
						latestBucket.GroupInfo.add(newBucketData);
						latestBucket.GroupInfoCount = latestBucket.GroupInfoCount + 1;
					}

					newBuckets = GetLatestNewBuckets(newBucketData, newBuckets);
				}
			}
			// assign equal range of quasi identifiers to tuples of all balanced buckets
			int maxAge = common.GetMaxAgeValue(latestBucket.GroupInfo);
			int minAge = common.GetMinAgeValue(latestBucket.GroupInfo);
			for (EmployeeHealthCareData emp : latestBucket.GroupInfo) {
				if (minAge == maxAge)
					emp.Age = minAge + "-" + (minAge + 5);
				else
					emp.Age = minAge + "-" + maxAge;
			}

			buckets.add(latestBucket);
		}
		List<String> fakeRowSensitiveValues = new ArrayList<String>();
		int iterator = 0;
		List<String> sensitiveAttributesListForFakeRows= new ArrayList<String>();
		for (EmployeeHealthCareInfo newBucket : newBuckets) {
			List<String> fakeRowValues = new ArrayList<String>();
			fakeRowValues.addAll(fakeRowSensitiveValues);
			List<String> sensitiveAttributeValuesInGroup = newBuckets.get(iterator).GroupInfo.stream()
					.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList());
			// To achieve m-uniqueness find any random sensitive attribute value that is not
			// already present in bucket tuples and assign fake row for that sensitive
			// attribute
			sensitiveAttributesListForFakeRows = sensitiveAttributes.stream()
					.filter(empOccupation -> !sensitiveAttributeValuesInGroup.contains(empOccupation)
							&& !fakeRowValues.contains(empOccupation))
					.collect(Collectors.toList());
			if (sensitiveAttributesListForFakeRows.stream().count() == 0) {
				fakeRowSensitiveValues = new ArrayList<String>();
				sensitiveAttributesListForFakeRows = sensitiveAttributes.stream()
						.filter(empOccupation -> !sensitiveAttributeValuesInGroup.contains(empOccupation))
						.collect(Collectors.toList());
			}
			int maxAge = common.GetMaxAgeValue(newBucket.GroupInfo);
			int minAge = common.GetMinAgeValue(newBucket.GroupInfo);
			int count = (int) newBucket.GroupInfo.stream().count();
			if (count < mValue) {

				for (Integer i = count; i <= mValue - 1; i++) {
					EmployeeHealthCareData empData = new EmployeeHealthCareData();
					if (sensitiveAttributesListForFakeRows.stream().count() > 0) {
						empData.setEmployeeId("Fake");
						empData.setAge(minAge + "-" + maxAge);
						newBucket.NoOfFakeRows = newBucket.NoOfFakeRows + 1;
						empData.Occupation = sensitiveAttributesListForFakeRows.get(0);
						fakeRowSensitiveValues.add(sensitiveAttributesListForFakeRows.get(0));
						sensitiveAttributesListForFakeRows.remove(0);
						newBucket.GroupInfo.add(empData);
					}

				}
			}
			iterator = iterator + 1;
		}
		int maxGroupId = 0;
		result.add(buckets);
		if (buckets.stream().count() > 0) {
			// increment max group id to next bucket
			List<Integer> groupIds = buckets.stream().map(EmployeeHealthCareInfo::getGroupId).distinct()
					.collect(Collectors.toList());
			Collections.sort(groupIds);
			maxGroupId = groupIds.get(groupIds.size() - 1);
			maxGroupId = maxGroupId + 1;
		}
		for (EmployeeHealthCareInfo empInfo : newBuckets) {
			empInfo.setGroupId(maxGroupId);
			maxGroupId = maxGroupId + 1;
		}

		result.add(newBuckets);
		return result;

	}

	public List<EmployeeHealthCareInfo> GetLatestNewBuckets(EmployeeHealthCareData newBucketData,
			List<EmployeeHealthCareInfo> newBuckets) {
		List<EmployeeHealthCareInfo> latestBuckets = new ArrayList<EmployeeHealthCareInfo>();
		for (EmployeeHealthCareInfo empInfo : newBuckets) {
			EmployeeHealthCareInfo latestNewBucket = new EmployeeHealthCareInfo();
			latestNewBucket.GroupId = empInfo.GroupId;
			latestNewBucket.GroupInfo = new ArrayList<EmployeeHealthCareData>();
			for (EmployeeHealthCareData empData : empInfo.GroupInfo) {
				if (newBucketData.Id != empData.Id && !empData.EmployeeId.equalsIgnoreCase("Fake")) {
					latestNewBucket.GroupInfo.add(empData);
					latestNewBucket.GroupInfoCount = latestNewBucket.GroupInfoCount + 1;
				}
			}
			if (latestNewBucket.GroupInfo.stream().count() > 0)
				latestBuckets.add(latestNewBucket);
		}
		return latestBuckets;

	}

	// returns relative difference between two integers
	public Integer ReturnDifferenceBetweenValues(int intValue1, int intValue2) {
		if (intValue1 > intValue2)
			return intValue1 - intValue2;
		else
			return intValue2 - intValue1;

	}

	public EmployeeHealthCareData GetMatchingNewBucketTuple(EmployeeHealthCareData bucketTuple,
			List<EmployeeHealthCareInfo> newBuckets, List<String> uniqueSensitiveValues) {
		List<EmployeeHealthCareData> possibleMatches = new ArrayList<EmployeeHealthCareData>();
		List<EmployeeHealthCareData> bucketTupleGroup = new ArrayList<EmployeeHealthCareData>();
		bucketTupleGroup.add(bucketTuple);
		EmployeeHealthCareData match = new EmployeeHealthCareData();
		int maxAge = common.GetMaxAgeValue(bucketTupleGroup);
		int minAge = common.GetMinAgeValue(bucketTupleGroup);
		int minAgeDifference = 100, maxAgeDifference = 100;

		for (EmployeeHealthCareInfo empInfo : newBuckets) {
			int maxAgeInGroup = common.GetMaxAgeValue(empInfo.GroupInfo);
			int minAgeInGroup = common.GetMinAgeValue(empInfo.GroupInfo);
			int minAgeDifferenceWithinGroup = ReturnDifferenceBetweenValues(maxAgeInGroup, maxAge);
			int maxAgeDifferenceWithinGroup = ReturnDifferenceBetweenValues(minAgeInGroup, minAge);

			for (EmployeeHealthCareData empData : empInfo.GroupInfo) {
				if (((bucketTuple.EmployeeId.equalsIgnoreCase("Fake")
						&& bucketTuple.Occupation.equalsIgnoreCase(empData.Occupation))
						|| (!empData.EmployeeId.equalsIgnoreCase("Fake")
								&& !uniqueSensitiveValues.contains(empData.Occupation)))
						&& minAgeDifferenceWithinGroup <= minAgeDifference
						&& maxAgeDifferenceWithinGroup <= maxAgeDifference) {
					minAgeDifference = minAgeDifferenceWithinGroup;
					maxAgeDifference = maxAgeDifferenceWithinGroup;
					match = empData;
				}
			}

		}
		if (minAgeDifference <= 5 && maxAgeDifference <= 5)
			return match;
		else
			return null;
	}

}
