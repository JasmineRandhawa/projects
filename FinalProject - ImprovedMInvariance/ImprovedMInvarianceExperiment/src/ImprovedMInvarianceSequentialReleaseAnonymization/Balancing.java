package ImprovedMInvarianceSequentialReleaseAnonymization;

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

		// initialize balanced buckets

		// keep track of disjoint data set tuples already consumed while balancing
		List<Integer> consumedDisjoints = new ArrayList<Integer>();

		// traverse through unbalancedBuckets
		for (EmployeeHealthCareInfo bucket : unBalancedBuckets) {

			List<EmployeeHealthCareData> possibleMatchesInBuckets = GetPossibleMatchesFromBuckets(unBalancedBuckets,
					bucket.GroupId);

			// get all tuples in the bucket which have blank identifier or fake row
			List<EmployeeHealthCareData> groupInfo = new ArrayList<EmployeeHealthCareData>();
			groupInfo = bucket.GroupInfo.stream().filter(
					(empInfo) -> empInfo.EmployeeId == null || empInfo.EmployeeId == "" || empInfo.EmployeeId == "Fake")
					.collect(Collectors.toList());

			// compute the maximum and minimum Value of the Quasi identifiers in the bucket
			// to find closed match from disjoint data set
			int maxAge = common.GetMaxAgeValue(bucket.GroupInfo);
			int minAge = common.GetMinAgeValue(bucket.GroupInfo);
			Boolean isFakeRowNeeded = false;
			// if there is such row which has blank employeeId or is a fake row, try to
			// balance it
			if (groupInfo.stream().count() > 0) {

				if (dRemainingDisjoint.stream().count() > 0)
					for (EmployeeHealthCareData empInfo : groupInfo) {

						// get all the disjoint data rows that are not yet consumed and which have same
						// occupation as the one that we are trying to balance from the bucket
						List<EmployeeHealthCareData> filteredDisjoint = dRemainingDisjoint.stream().filter(
								dis -> !consumedDisjoints.contains(dis.Id) && dis.Occupation.equals(empInfo.Occupation))
								.collect(Collectors.toList());

						// if such tuples exists in the disjoint data set, find closest match to the
						// unbalanced tuple in the bucket
						if (filteredDisjoint.stream().count() > 0) {
							EmployeeHealthCareData closestMatch = GetClosestMatch(filteredDisjoint, dRemainingDisjoint,
									possibleMatchesInBuckets, maxAge, minAge, empInfo.Occupation);
							if (closestMatch != null) {
								// assign closest match values to the unbalanced row in the bucket
								empInfo.setId(closestMatch.Id);
								empInfo.setEmployeeId(closestMatch.EmployeeId);
								empInfo.setAge(closestMatch.Age);
								consumedDisjoints.add(closestMatch.Id);
								isFakeRowNeeded = false;
							} else {
								empInfo.setEmployeeId("Fake");
								empInfo.setAge(minAge + "");
								bucket.NoOfFakeRows = bucket.NoOfFakeRows + 1;
								isFakeRowNeeded = true;
							}
						}
						// if no such suitable match is found in disjoint data set assign fake row if
						// not already assigned
						else {
							empInfo.setEmployeeId("Fake");
							empInfo.setAge(minAge + "");
							bucket.NoOfFakeRows = bucket.NoOfFakeRows + 1;
							isFakeRowNeeded = true;
						}

					}
			}

			// get all remaining disjoint data set tuples
			dRemainingDisjoint = dRemainingDisjoint.stream().filter(dis -> !consumedDisjoints.contains(dis.Id))
					.collect(Collectors.toList());
			// get list of all unique sensitive attribute values present in the whole data
			List<String> sensitiveAttributes = GetAllUniqueSenstiveAttributeValues(unBalancedBuckets,
					dRemainingDisjoint);
			if (!isFakeRowNeeded) {
				// get list of all unique sensitive attribute values present in the bucket
				List<String> sensitiveAttributeValuesInBucket = bucket.GroupInfo.stream()
						.map(EmployeeHealthCareData::getOccupation).distinct().collect(Collectors.toList());

				List<String> sensitiveAttrInBuckets = bucket.GroupInfo.stream()
						.map(EmployeeHealthCareData::getOccupation).distinct().collect(Collectors.toList());

				// get list of all unique sensitive attribute values present in the remaining
				// disjoint
				List<String> sensitiveAttributesInDisjoint = dRemainingDisjoint.stream()
						.map(EmployeeHealthCareData::getOccupation).distinct()
						.filter(dis -> !sensitiveAttrInBuckets.contains(dis)).collect(Collectors.toList());

				// try to fit the remaining disjoint with different sensitive values in the
				// buckets
				List<Integer> consumedDisjointIds = new ArrayList<Integer>();
				if (bucket.GroupInfo.stream().count() < mValue) {
					for (String sensitiveAttribute : sensitiveAttributesInDisjoint) {
						if (bucket.GroupInfo.stream().count() == mValue) {
							break;
						}
						if (dRemainingDisjoint.stream().count() > 0) {
							List<String> sensitiveAttrInBucket = bucket.GroupInfo.stream()
									.map(EmployeeHealthCareData::getOccupation).distinct().collect(Collectors.toList());
							List<EmployeeHealthCareData> filteredDisjoint = dRemainingDisjoint.stream()
									.filter(dis -> !consumedDisjointIds.contains(dis.Id)
											&& sensitiveAttribute.equals(dis.Occupation)
											&& !sensitiveAttrInBucket.contains(sensitiveAttribute))
									.collect(Collectors.toList());
							if (filteredDisjoint.stream().count() > 0) {
								EmployeeHealthCareData match = GetClosestMatch(dRemainingDisjoint, dRemainingDisjoint,
										possibleMatchesInBuckets, maxAge, minAge, sensitiveAttribute);
								// assign closest match values to the unbalanced row in the bucket
								if (match != null) {
									EmployeeHealthCareData data = new EmployeeHealthCareData();
									data.setId(match.Id);
									data.setEmployeeId(match.EmployeeId);
									data.setAge(match.Age);
									data.setOccupation(match.Occupation);
									consumedDisjointIds.add(match.Id);
									dRemainingDisjoint = dRemainingDisjoint.stream()
											.filter(dis -> !consumedDisjointIds.contains(dis.Id))
											.collect(Collectors.toList());
									bucket.GroupInfo.add(data);
								}
							}
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
						bucket.GroupInfo.add(empData);
					}

				}

			}

			// assign equal range of quasi identifiers to tuples of all balanced buckets
			maxAge = common.GetMaxAgeValue(bucket.GroupInfo);
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

	// get closest match from disjoint data set for the unbalanced tuple in the
	// bucket
	public EmployeeHealthCareData GetClosestMatch(List<EmployeeHealthCareData> dDisjoint,
			List<EmployeeHealthCareData> dWholeDisjoint, List<EmployeeHealthCareData> possibleMatchesInBuckets,
			int maxAge, int minAge, String sensitiveAttribute) {
		EmployeeHealthCareData closestMatch = new EmployeeHealthCareData();

		double averageAge = (double) (minAge + maxAge) / 2;
		int averageAgeInInt = (int) averageAge;

		List<Integer> consumedDisjointIds = new ArrayList<Integer>();
		// compute the difference between the closest match and the unbalanced bucket
		// quasi identifier values
		Integer minAgeDifference = 100;
		for (EmployeeHealthCareData empDisjoint : dDisjoint) {
			Integer mindiff = 200;
			if (empDisjoint.Occupation.toUpperCase().equals(sensitiveAttribute.toUpperCase())) {
				mindiff = ReturnDifferenceBetweenValues(Integer.parseInt(empDisjoint.Age), averageAgeInInt);
			}
			List<EmployeeHealthCareData> filteredDisjoint = dWholeDisjoint.stream()
					.filter(dis -> !consumedDisjointIds.contains(dis.Id) && empDisjoint.Id != dis.Id
							&& !empDisjoint.Occupation.toUpperCase().equals(dis.Occupation.toUpperCase()))
					.collect(Collectors.toList());
			if (filteredDisjoint.stream().count() > 0) {
				String[] result = GetMinDifferenceInDisjoint(filteredDisjoint, empDisjoint, averageAgeInInt,
						possibleMatchesInBuckets).split(",");
				int minDiffInDisjoint = Integer.parseInt(result[0]);
				int matchId = Integer.parseInt(result[1]);
				int relativeDiff = ReturnDifferenceBetweenValues(minDiffInDisjoint, mindiff);
				if (relativeDiff > 15 && (minDiffInDisjoint < mindiff || (matchId == 0))) {
					if (matchId != 0) {
						consumedDisjointIds.add(matchId);
						consumedDisjointIds.add(empDisjoint.Id);
					}
					mindiff=200;
					continue;
				}
			}
			// check if any other element in disjoint data set is more closer than first
			// initialized one by comparing difference in quasi identifier values
			if (mindiff < minAgeDifference) {
				minAgeDifference = mindiff;
				closestMatch = empDisjoint;
			}
		}
		if (closestMatch.Id == 0)
			return null;
		return closestMatch;
	}

	public String GetMinDifferenceInDisjoint(List<EmployeeHealthCareData> dDisjoint, EmployeeHealthCareData disjoint,
			int averageAgeInInt, List<EmployeeHealthCareData> possibleMatchesInBuckets) {
		EmployeeHealthCareData closestMatch = new EmployeeHealthCareData();

		// randomly assign first element of disjoint data set as the closest match
		closestMatch = disjoint;
		// compute the difference between the closest match and the unbalanced bucket
		// quasi identifier values
		Integer minAgeDifference = 100;
		for (EmployeeHealthCareData empDisjoint : dDisjoint) {
			Integer mindiff = ReturnDifferenceBetweenValues(Integer.parseInt(empDisjoint.Age),
					Integer.parseInt(disjoint.Age));
			// check if any other element in disjoint data set is more closer than first
			// initialized one by comparing difference in quasi identifier values
			if (mindiff < minAgeDifference) {
				minAgeDifference = mindiff;
				closestMatch = empDisjoint;
			}
		}
		// check if unbalanced buckets have more closer match
		for (EmployeeHealthCareData emp : possibleMatchesInBuckets) {
			if (emp.Occupation.equals(disjoint.Occupation)) {
				Integer mindiff = ReturnDifferenceBetweenValues(Integer.parseInt(disjoint.Age),
						Integer.parseInt(emp.Age));
				if (mindiff <= minAgeDifference) {
					minAgeDifference = mindiff;
					closestMatch = null;
				}
			}
		}
		return minAgeDifference + "," + (closestMatch == null ? 0 : closestMatch.Id);
	}

	// Get List of All Possible Matches from Unbalanced Data set to evaluate closest
	// match for for the Disjoint data tuples
	public static List<EmployeeHealthCareData> GetPossibleMatchesFromBuckets(List<EmployeeHealthCareInfo> dAnonymized,
			int groupId) {
		List<EmployeeHealthCareData> possibleMatchesInBuckets = new ArrayList<EmployeeHealthCareData>();
		for (EmployeeHealthCareInfo empInfo : dAnonymized) {
			if (empInfo.GroupId > groupId) {
				List<EmployeeHealthCareData> groupInfo = empInfo.GroupInfo.stream()
						.filter(emp -> (emp.EmployeeId == null || emp.EmployeeId == "" || emp.EmployeeId == "Fake"))
						.collect(Collectors.toList());
				if (groupInfo.stream().count() > 0) {
					for (EmployeeHealthCareData empData : groupInfo) {
						Integer minAge = Integer.parseInt(empData.Age.toString().split("-")[0]);
						Integer maxAge = Integer.parseInt(empData.Age.toString().split("-")[1]);
						double average = (double) (minAge + maxAge) / 2;
						int averageInInt = (int) average;
						EmployeeHealthCareData emp = new EmployeeHealthCareData();
						emp.Id = empData.Id;
						emp.EmployeeId = empData.EmployeeId;
						emp.Occupation = empData.Occupation;
						emp.Age = averageInInt + "";
						possibleMatchesInBuckets.add(emp);
					}
				}
			}
		}
		return possibleMatchesInBuckets;
	}

	// returns relative difference between two integers
	public Integer ReturnDifferenceBetweenValues(int intValue1, int intValue2) {
		if (intValue1 > intValue2)
			return intValue1 - intValue2;
		else
			return intValue2 - intValue1;

	}

}
