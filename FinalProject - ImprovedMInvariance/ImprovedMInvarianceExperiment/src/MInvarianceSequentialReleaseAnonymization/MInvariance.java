package MInvarianceSequentialReleaseAnonymization;

import java.io.Console;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DataBase.FetchDatabaseInfo;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Improved m - invariance
public class MInvariance {

	// create anonymized data set
	public List<List> AnonymizeData(String mValue) {

		try {
			List<EmployeeHealthCareData> dPrevious, dCurrent = new ArrayList<EmployeeHealthCareData>();
			List<EmployeeHealthCareInfo> dPreviousAnonymized = new ArrayList<EmployeeHealthCareInfo>();

			// fetch input data sets from the database
			FetchDatabaseInfo fetchData = new FetchDatabaseInfo();
			dPrevious = fetchData.GetPreviousData();
			dCurrent = fetchData.GetCurrentData();
			dPreviousAnonymized = fetchData.GetPreviousAnonymizedData();

			if (!dPrevious.isEmpty() && !dCurrent.isEmpty() && !dPreviousAnonymized.isEmpty())
			{
				System.out.println("Completed");
			// SequentialReleasePublication
			return SequentialPublicationAnonymization.GetAnonymizedData(dPrevious, dCurrent, dPreviousAnonymized,
					mValue);
			} else
				return null;

		} catch (SQLException ex) {
			System.out.println("Fetch Data Error : " + ex.getMessage());
			return null;
		} catch (Exception ex) {
			System.out.println("Error : " + ex.getMessage());
			return null;
		}

	}
}
