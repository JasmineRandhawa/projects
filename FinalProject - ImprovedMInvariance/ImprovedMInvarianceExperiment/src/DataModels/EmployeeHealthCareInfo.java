package DataModels;

import java.util.ArrayList;
import java.util.List;

//Anonymized Employee Health Care Data
public class EmployeeHealthCareInfo {

	// Group Identifier to be assigned to each equivalent Group
	public int GroupId;

	public int getGroupId() {
		return GroupId;
	}

	public void setGroupId(Integer newGroupId) {
		this.GroupId = newGroupId;
	}

	// MValue
	public int MValue;

	public int getMValue() {
		return MValue;
	}

	public void setMValue(Integer newMValue) {
		this.MValue = newMValue;
	}

	// Threshold
	public int Threshold;

	public int getThreshold() {
		return Threshold;
	}

	public void setThreshold(Integer newThreshold) {
		this.Threshold = newThreshold;
	}

	public int GroupInfoCount;

	public int getGroupInfoCount() {
		return GroupInfoCount;
	}

	public void setGroupInfoCount(Integer newGroupInfoCount) {
		this.GroupInfoCount = GroupInfoCount;
	}

	// No Of Fake Rows
	public int NoOfFakeRows;

	public int getNoOfFakeRows() {
		return NoOfFakeRows;
	}

	public void setNoOfFakeRows(Integer newNumberOfFakeRows) {
		this.NoOfFakeRows = newNumberOfFakeRows;
	}

	// Group Information for each equivalent group
	public List<EmployeeHealthCareData> GroupInfo;

	public List<EmployeeHealthCareData> getGroupInfo() {
		return new ArrayList<EmployeeHealthCareData>(GroupInfo);
	}

}
