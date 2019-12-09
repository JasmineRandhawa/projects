package DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import Common.Constants;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Fetch all input data sets ( current. previous and previous anonymized) from the database in form of list of class objects
public class FetchDatabaseInfo {

	// get previous data
	public List<EmployeeHealthCareData> GetPreviousData() throws SQLException {
		ResultSet previousDataresultSet = Constants.GetSqlCommand().executeQuery(Constants.Sample2SelectQuery);
		return ConvertEmployeeHealthCareDataToList(previousDataresultSet);
	}

	// get current data
	public List<EmployeeHealthCareData> GetCurrentData() throws SQLException {
		ResultSet currentDataresultSet = Constants.GetSqlCommand().executeQuery(Constants.Sample1SelectQuery);
		return ConvertEmployeeHealthCareDataToList(currentDataresultSet);
	}

	// Get Previously Anonymized Data List
	public List<EmployeeHealthCareInfo> GetPreviousAnonymizedData() throws SQLException {
		Statement mySqlCommand = Constants.GetSqlCommand();
		ResultSet tPreviousAnonymized = mySqlCommand.executeQuery(Constants.PreviousAnonymizedTableGroupIdQuery);
		List<Integer> groupList = new ArrayList<Integer>();
		List<EmployeeHealthCareInfo> employeeHealthCareInfoList = new ArrayList<EmployeeHealthCareInfo>();

		while (tPreviousAnonymized.next()) {
			if (!groupList.contains(tPreviousAnonymized.getInt("GroupId")))
				groupList.add(tPreviousAnonymized.getInt("GroupId"));
		}
		for (int groupId : groupList) {
			EmployeeHealthCareInfo employeeHealthCareInfo = new EmployeeHealthCareInfo();
			employeeHealthCareInfo.setGroupId(groupId);

			ResultSet groupResultSet = mySqlCommand
					.executeQuery("SELECT distinct * FROM PreviousAnonymizedSample1 where GroupId  = " + groupId);
			List<EmployeeHealthCareData> groupInfo = ConvertEmployeeHealthCareDataToList(groupResultSet);
			groupInfo = groupInfo.stream().filter(Objects::nonNull).collect(Collectors.toList());
			employeeHealthCareInfo.GroupInfo = groupInfo;
			int numberOfFakeRows = (int) groupInfo.stream().filter((emp) -> emp.EmployeeId.equals("Fake")).count();
			employeeHealthCareInfo.setNoOfFakeRows(numberOfFakeRows);
			employeeHealthCareInfoList.add(employeeHealthCareInfo);
		}
		employeeHealthCareInfoList = employeeHealthCareInfoList.stream().filter(Objects::nonNull)
				.collect(Collectors.toList());
		return employeeHealthCareInfoList;
	}

	// convert result set to List
	private List<EmployeeHealthCareData> ConvertEmployeeHealthCareDataToList(ResultSet resultSet) throws SQLException {
		List<EmployeeHealthCareData> groupInfo = new ArrayList<EmployeeHealthCareData>();
		while (resultSet.next()) {
			EmployeeHealthCareData employeeHealthCareData = new EmployeeHealthCareData();
			employeeHealthCareData.setId(Integer.parseInt(resultSet.getString("Id").trim()));
			employeeHealthCareData.setEmployeeId(resultSet.getString("EmployeeId").trim());
			employeeHealthCareData.setAge(resultSet.getString("Age").trim());
			employeeHealthCareData.setOccupation(resultSet.getString("Occupation").trim());
			groupInfo.add(employeeHealthCareData);
		}
		groupInfo = groupInfo.stream().filter(Objects::nonNull).collect(Collectors.toList());
		return groupInfo;
	}

	// check if column is present in result set
	private boolean CheckIfColumnIsPresentInResultSet(ResultSet rs, String columnName) {
		try {
			rs.findColumn(columnName);
			return true;
		} catch (SQLException sqlex) {
			return false;
		}
	}

}
