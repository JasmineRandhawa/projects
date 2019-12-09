package MInvarianceSequentialReleaseAnonymization;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

// Create New Buckets
public class NewBucketCreation {

	// Create New Buckets from remaining disjoint Data Set
	public List<List> AddNewBucketsForDisjointSets(List<EmployeeHealthCareData> dDisjoint, Integer maxGroupId,
			Integer mValue) {
		// initialize variables
		List<List> newBucketsAndRemainingDisjointList = new ArrayList<List>();
		// initialize new buckets list
		List<EmployeeHealthCareInfo> newBuckets = new ArrayList<EmployeeHealthCareInfo>();

		// keep track of disjoint data tuples already consumed
		List<String> consumedDisjoints = new ArrayList<String>();

		// initialize group Id as next Group Id with respect to max group id of the
		// previously created unbalanced buckets
		maxGroupId = maxGroupId + 1;
		// traverse disjoint data set to create new buckets
		for (EmployeeHealthCareData disjoint : dDisjoint) {

			// check if disjoint data tuple is not already assigned to some bucket
			if (!consumedDisjoints.contains(disjoint.EmployeeId)) {
				// initialize new bucket
				EmployeeHealthCareInfo empInfo = new EmployeeHealthCareInfo();

				if (newBuckets.stream().count() > 0) {
					// increment max group id to next bucket
					List<Integer> groupIds = newBuckets.stream().map(EmployeeHealthCareInfo::getGroupId).distinct()
							.collect(Collectors.toList());
					Collections.sort(groupIds);
					maxGroupId = groupIds.get(groupIds.size() - 1);
					
				}
				
				// filter disjoint data set that is not already consumed in the new bucket
				List<EmployeeHealthCareData> dFilteredDisjoint = dDisjoint.stream()
						.filter(dis -> !consumedDisjoints
								.contains(dis.EmployeeId)).collect(Collectors.toList());
				// create new bucket
				List<EmployeeHealthCareData> groupInfo = CreateNewBucket(disjoint, dFilteredDisjoint, mValue);
				// if new bucket is formed add it to bucket list
				if (groupInfo.stream().count() > 0) {
					maxGroupId = maxGroupId + 1;
					empInfo.setGroupId(maxGroupId);
					empInfo.GroupInfo = groupInfo;
					List<String> Ids = groupInfo.stream().map(EmployeeHealthCareData::getEmployeeId)
							.collect(Collectors.toList());

					// compute and assign quasi identifier value range to all the tuples in the new
					// bucket
					String ageRange = GetAgeRange(groupInfo);
					for (EmployeeHealthCareData emp : groupInfo) {
						emp.Age = ageRange;
					}
					consumedDisjoints.addAll(Ids);

					empInfo.GroupInfoCount = (int) groupInfo.stream().count();
					newBuckets.add(empInfo);
				}
			}
		}
		List<EmployeeHealthCareData> dFilteredDisjoint = dDisjoint.stream()
				.filter(dis -> !consumedDisjoints.contains(dis.EmployeeId)).collect(Collectors.toList());
		newBucketsAndRemainingDisjointList.add(dFilteredDisjoint);
		newBucketsAndRemainingDisjointList.add(newBuckets);
		return newBucketsAndRemainingDisjointList;
	}

	public List<EmployeeHealthCareData> CreateNewBucket(EmployeeHealthCareData disjoint,
			List<EmployeeHealthCareData> dDisjoint, Integer mValue) {
		List<EmployeeHealthCareData> groupInfo = new ArrayList<EmployeeHealthCareData>();
		List<Integer> consumedDisjoints = new ArrayList<Integer>();
		groupInfo.add(disjoint);
		if (dDisjoint.stream().count() > 0) {
			// create groups with m unique values
			for (Integer i = 2; i <= mValue; i++) {
				List<String> sensitiveAttributes = groupInfo.stream().map(EmployeeHealthCareData::getOccupation)
						.collect(Collectors.toList());
				List<EmployeeHealthCareData> dFilteredDisjoint = dDisjoint.stream()
						.filter(empDis -> (!sensitiveAttributes.contains(empDis.Occupation)
								&& !consumedDisjoints.contains(empDis.Id)))
						.collect(Collectors.toList());
				if (dFilteredDisjoint.stream().count() > 0) {
					EmployeeHealthCareData closestMatch = GetClosestMatchEmployeeIdOutOfDisjointDataSet(disjoint,
							dFilteredDisjoint);
					if (closestMatch != null) {
						groupInfo.add(closestMatch);
						consumedDisjoints.add(closestMatch.Id);
					}
				}

			}
		}
		int count = (int) groupInfo.stream().count();
		if (count == mValue)
			return groupInfo;
		else
			groupInfo = new ArrayList<EmployeeHealthCareData>();
		return groupInfo;
	}

	// get closest match from disjoint set
	public EmployeeHealthCareData GetClosestMatchEmployeeIdOutOfDisjointDataSet(EmployeeHealthCareData disjoint,
			List<EmployeeHealthCareData> dDisjoint) {
		EmployeeHealthCareData closestMatch = new EmployeeHealthCareData();
		// initialize with first element in disjoint set as closest match
		closestMatch = dDisjoint.get(0);
		dDisjoint.remove(0);
		Integer minAgeDifference = ReturnDifferenceBetweenValues(disjoint.Age, closestMatch.Age);
		for (EmployeeHealthCareData empDisjoint : dDisjoint) {
			Integer mindiff = ReturnDifferenceBetweenValues(disjoint.Age, empDisjoint.Age);
			// check if any other element in disjoint data set is more closer than first
			// initialized one
			if (mindiff < minAgeDifference) {
				minAgeDifference = mindiff;
				closestMatch = empDisjoint;
			}
		}
		return closestMatch;
	}

	// return relative difference between two integers
	public Integer ReturnDifferenceBetweenValues(String value1, String value2) {
		Integer intValue1 = Integer.parseInt(value1);
		Integer intValue2 = Integer.parseInt(value2);
		if (intValue1 > intValue2)
			return intValue1 - intValue2;
		else
			return intValue2 - intValue1;

	}

	// compute quasi identifier range for the new bucket
	public String GetAgeRange(List<EmployeeHealthCareData> groupInfo) {
		Common common = new Common();
		List<String> ages = groupInfo.stream().map(EmployeeHealthCareData::getAge).collect(Collectors.toList()).stream()
				.distinct().collect(Collectors.toList());
		List<Integer> sortedList = common.SortedStringList(ages);
		if (sortedList.stream().count() == 1)
			return sortedList.get(0) + "-" + (sortedList.get(0) + 2);
		else
			return sortedList.get(0) + "-" + (sortedList.get(sortedList.size() - 1));

	}

}
