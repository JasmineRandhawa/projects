package MInvarianceSequentialReleaseAnonymization;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import Common.Common;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//To balance the unbalanced buckets
public class Balancing {

	Common common = new Common();

	// Balance the unbalanced buckets with remaining disjoint data set or fake rows
	// to m-Invariance
	public List<List> BalanceBuckets(List<EmployeeHealthCareInfo> unBalancedBuckets,
			List<EmployeeHealthCareData> dRemainingDisjoint, Integer mValue) {

		// initialize balanced buckets
		List<EmployeeHealthCareInfo> balancedBuckets = new ArrayList<EmployeeHealthCareInfo>();

		// keep track of disjoint data set tuples already consumed while balancing
		List<Integer> consumedDisjoints = new ArrayList<Integer>();

		// traverse through unbalancedBuckets
		for (EmployeeHealthCareInfo bucket : unBalancedBuckets) {

			// get all tuples in the bucket which have blank identifier
			List<EmployeeHealthCareData> groupInfo = new ArrayList<EmployeeHealthCareData>();
			groupInfo = bucket.GroupInfo.stream()
					.filter((empInfo) -> empInfo.EmployeeId == null || empInfo.EmployeeId == "")
					.collect(Collectors.toList());

			int minAge = common.GetMinAgeValue(bucket.GroupInfo);

			// if there is such row which has blank employeeid or is a fake row, try to
			// balance it
			if (groupInfo.stream().count() > 0) {
				for (EmployeeHealthCareData empInfo : groupInfo) {
					// get all the disjoint data rows that are not yet consumed and which have same
					// Occupation as the one that we are trying to balance from the bucket

					List<EmployeeHealthCareData> filteredDisjoint = dRemainingDisjoint.stream().filter(
							dis -> !consumedDisjoints.contains(dis.Id) && dis.Occupation.equals(empInfo.Occupation))
							.collect(Collectors.toList());

					// if such tuples exists in the disjoint data set, find random match to the
					// unbalanced tuple in the bucket
					if (filteredDisjoint.stream().count() > 0) {
						EmployeeHealthCareData match = filteredDisjoint.get(0);
						// assign random match values to the unbalanced row in the bucket
						empInfo.setId(match.Id);
						empInfo.setEmployeeId(match.EmployeeId);
						empInfo.setAge(match.Age);
						consumedDisjoints.add(match.Id);
					}
					// if no such suitable match is found in disjoint data set assign fake row if
					// not already assigned
					else {
						empInfo.setEmployeeId("Fake");
						empInfo.setAge(minAge + "");
						bucket.NoOfFakeRows = bucket.NoOfFakeRows + 1;
					}
					// get all remaining disjoint data set tuples
					dRemainingDisjoint = dRemainingDisjoint.stream().filter(dis -> !consumedDisjoints.contains(dis.Id))
							.collect(Collectors.toList());
				}
			}

			// get all remaining disjoint data set tuples
			dRemainingDisjoint = dRemainingDisjoint.stream().filter(dis -> !consumedDisjoints.contains(dis.Id))
					.collect(Collectors.toList());

			// get list of all unique sensitive attribute values present in the bucket
			List<String> sensitiveAttributeValuesInBucket = bucket.GroupInfo.stream().distinct()
					.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList());

			// get list of all unique sensitive attribute values present in the whole data
			List<String> sensitiveAttributes = GetAllUniqueSenstiveAttributeValues(unBalancedBuckets,
					dRemainingDisjoint);

			// get list of all unique sensitive attribute values present in the remaining
			// disjoint
			List<String> sensitiveAttributesInDisjoint = dRemainingDisjoint.stream()
					.map(EmployeeHealthCareData::getOccupation).distinct().collect(Collectors.toList());
			List<Integer> consumedDisjointIds = new ArrayList<Integer>();
			// try to fit the remaining disjoint with different sensitive values in the
			// buckets
			if (bucket.GroupInfo.stream().count() < mValue) {
				for (String sensitiveAttribute : sensitiveAttributesInDisjoint) {
					if (bucket.GroupInfo.stream().count() == mValue) {
						break;
					}
					if (dRemainingDisjoint.stream().count() > 0) {
						List<String> sensitiveAttrInBucket = bucket.GroupInfo.stream().distinct()
								.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList());
						List<EmployeeHealthCareData> filteredDisjoint = dRemainingDisjoint.stream()
								.filter(dis -> !consumedDisjointIds.contains(dis.Id)
										&& sensitiveAttribute.equals(dis.Occupation)
										&& !sensitiveAttrInBucket.contains(sensitiveAttribute))
								.collect(Collectors.toList());
						if (filteredDisjoint.stream().count() > 0) {
							EmployeeHealthCareData match = filteredDisjoint.get(0);
							EmployeeHealthCareData empData = new EmployeeHealthCareData();
							empData.setId(match.Id);
							empData.setEmployeeId(match.EmployeeId);
							empData.setAge(match.Age);
							empData.setOccupation(match.Occupation);
							consumedDisjointIds.add(match.Id);
							dRemainingDisjoint = dRemainingDisjoint.stream()
									.filter(dis -> !consumedDisjointIds.contains(dis.Id)).collect(Collectors.toList());
							bucket.GroupInfo.add(empData);
						}
					}

				}

			}
			// if the bucket count is less then m Value, add fake rows to achieve m
			// Invariance
			int count = (int) bucket.GroupInfo.stream().count();
			if (count < mValue) {
				List<String> sensitiveAttributeValuesInGroup = bucket.GroupInfo.stream()
						.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList());
				// To achieve m-uniqueness find any random sensitive attribute value that is not
				// already present in bucket tuples and assign fake row for that sensitive
				// attribute
				List<String> sensitiveAttributesListForFakeRows = sensitiveAttributes.stream()
						.filter(empOccupation -> !sensitiveAttributeValuesInGroup.contains(empOccupation))
						.collect(Collectors.toList());
				for (Integer i = count; i <= mValue - 1; i++) {
					EmployeeHealthCareData empData = new EmployeeHealthCareData();
					if (sensitiveAttributesListForFakeRows.stream().count() > 0) {
						empData.setEmployeeId("Fake");
						empData.setAge(minAge + "");
						bucket.NoOfFakeRows = bucket.NoOfFakeRows + 1;
						empData.Occupation = sensitiveAttributesListForFakeRows.get(0);
						sensitiveAttributesListForFakeRows.remove(0);
					}
					bucket.GroupInfo.add(empData);
				}

			}

			// assign equal range of quasi identifiers to tuples of all balanced buckets
			int maxAge = common.GetMaxAgeValue(bucket.GroupInfo);
			minAge = common.GetMinAgeValue(bucket.GroupInfo);
			for (EmployeeHealthCareData emp : bucket.GroupInfo) {
				if (minAge == maxAge)
					emp.Age = minAge + "-" + (minAge + 5);
				else if (maxAge-minAge <=5)
					emp.Age = minAge + "-" + (maxAge + 5);
				else
					emp.Age = minAge + "-" + maxAge;
			}

		}

		balancedBuckets = unBalancedBuckets;

		// get all remaining disjoint data set tuples
		dRemainingDisjoint = dRemainingDisjoint.stream().filter(dis -> !consumedDisjoints.contains(dis.Id))
				.collect(Collectors.toList());

		// return list of balanced buckets and the remaining disjoint data set
		ArrayList<List> bucketsAndRemainingDisjointList = new ArrayList<List>();
		bucketsAndRemainingDisjointList.add(dRemainingDisjoint);
		bucketsAndRemainingDisjointList.add(balancedBuckets);
		return bucketsAndRemainingDisjointList;
	}

	public List<String> GetAllUniqueSenstiveAttributeValues(List<EmployeeHealthCareInfo> dAnonymized,
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
