package MInvarianceSequentialReleaseAnonymization;

import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import DataBase.InsertResultsIntoDatabase;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//create common and disjoint data set by comparing two sequential consecutive release and 
//creating unbalanced buckets using common data set and previous anonymized data set
public class Division {

	// create common data set with common data set by matching current and previous
	// data sets
	public List<EmployeeHealthCareData> CreateCommonDataSet(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent) {
		List<EmployeeHealthCareData> dCommon = new ArrayList<EmployeeHealthCareData>();
		for (EmployeeHealthCareData current : dCurrent) {
			for (EmployeeHealthCareData previous : dPrevious)
				// extract the tuples which have same employeeid and same signature (list of
				// distinct
				// sensitive attributes)
				if (current.EmployeeId.toUpperCase().equals(previous.EmployeeId.toUpperCase())
						&& current.Occupation.toUpperCase().equals(previous.Occupation.toUpperCase()))
					dCommon.add(current);
		}
		return dCommon;
	}

	// create disjoint data set by computing matches between current and previous
	// data sets
	public List<EmployeeHealthCareData> CreateDisjointDataSets(List<EmployeeHealthCareData> dPrevious,
			List<EmployeeHealthCareData> dCurrent) {
		List<EmployeeHealthCareData> dDisjoint = new ArrayList<EmployeeHealthCareData>();
		List<String> previousEmployeeIds = dPrevious.stream().map(EmployeeHealthCareData::getEmployeeId)
				.collect(Collectors.toList());
		for (EmployeeHealthCareData current : dCurrent) {
			// check in current data for new rows with different EmployeeIds from previous
			// data
			if (!previousEmployeeIds.contains(current.EmployeeId))
				dDisjoint.add(current);

			// check in current data for rows with same employeeIds and different occupation
			// from
			// previous data
			else {
				EmployeeHealthCareData previousOccupationForSameEmployee = dPrevious.stream()
						.filter((previous) -> (previous.EmployeeId.equals(current.EmployeeId)
								&& !previous.Occupation.equals(current.Occupation)))
						.findFirst().orElse(null);
				if (previousOccupationForSameEmployee != null)
					dDisjoint.add(current);
			}
		}
		return dDisjoint;
	}

	// create buckets using common data for tuples with same signature as the
	// previously anonymized data set
	public List<EmployeeHealthCareInfo> CreateBuckets(List<EmployeeHealthCareData> dCommon,
			List<EmployeeHealthCareInfo> dPreviousAnonymized) throws SQLException {
		// match the signature of these two data sets and create buckets based on
		// employeeId
		List<EmployeeHealthCareInfo> buckets = new ArrayList<EmployeeHealthCareInfo>();
		int groupId = 1;
		for (EmployeeHealthCareInfo prevAnonymized : dPreviousAnonymized) {

			EmployeeHealthCareInfo bucket = new EmployeeHealthCareInfo();
			// assign group id to each bucket

			bucket.GroupInfo = new ArrayList<EmployeeHealthCareData>();

			InsertResultsIntoDatabase db = new InsertResultsIntoDatabase();

			// assign group info to each bucket with same signature as the previous
			// anonymized data set

			for (EmployeeHealthCareData common : dCommon) {

				List<EmployeeHealthCareData> commonSignatureRows = prevAnonymized.GroupInfo.stream()
						.filter((emp) -> (emp.EmployeeId.toUpperCase().equals(common.EmployeeId.toUpperCase())
								&& emp.Occupation.toUpperCase().equals(common.Occupation.toUpperCase())))
						.collect(Collectors.toList());
				if (commonSignatureRows.stream().count() > 0) {
					for (EmployeeHealthCareData e : commonSignatureRows)
						e.Age = common.Age;
					bucket.GroupInfo.addAll(commonSignatureRows);
				}

			}
			// create space for fake rows only when any one tuple of this group in Previous
			// anonymized data matches with the common data
			if (bucket.GroupInfo.stream().count() > 0) {
				// find out rows with different signature and also add them to bucket with only
				// sensitive attribute populated
				List<String> distinctSensitiveAttributes = prevAnonymized.GroupInfo.stream()
						.map(EmployeeHealthCareData::getOccupation).collect(Collectors.toList());
				List<String> commonSensitiveAttributes = bucket.GroupInfo.stream()
						.map(EmployeeHealthCareData::getOccupation).map(String::toUpperCase)
						.collect(Collectors.toList());

				// create space to maintain same structure as previous anonymized data set for
				// rows which have been deleted
				for (String sensitiveAttributeValue : distinctSensitiveAttributes) {
					if (!commonSensitiveAttributes.contains(sensitiveAttributeValue.toUpperCase())) {
						EmployeeHealthCareData emp = new EmployeeHealthCareData();
						emp.Occupation = sensitiveAttributeValue;
						// copy same quasi identifiers and sensitive values to below empty row
						emp.Age = bucket.GroupInfo.stream().map(EmployeeHealthCareData::getAge).findFirst()
								.orElseGet(null);
						bucket.GroupInfo.add(emp);
					}
				}
				bucket.GroupInfoCount = (int) bucket.GroupInfo.stream().count();
				bucket.setGroupId(groupId);
				groupId = groupId + 1;
				buckets.add(bucket);
			}

		}
		int grpId = 1;
		for (EmployeeHealthCareInfo empInfo : buckets) {
			empInfo.setGroupId(grpId);
			grpId = grpId + 1;
		}
		return buckets;
	}

}
