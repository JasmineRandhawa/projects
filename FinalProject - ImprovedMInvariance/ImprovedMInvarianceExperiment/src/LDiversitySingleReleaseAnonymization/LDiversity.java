package LDiversitySingleReleaseAnonymization;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import DataBase.FetchDatabaseInfo;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Improved m - invariance
public class LDiversity {

	// create anonymized data set
	public static List<EmployeeHealthCareInfo> AnonymizeData(Integer lValue) {

		try {
			List<EmployeeHealthCareData>  dCurrent = new ArrayList<EmployeeHealthCareData>();

			// fetch input data sets from the database
			FetchDatabaseInfo fetchData = new FetchDatabaseInfo();
			//dCurrent = fetchData.GetCurrentData();
			dCurrent = fetchData.GetPreviousData();

			if (!dCurrent.isEmpty())
				return SingleReleaseLDiversityAnonymization.GetAnonymizedData(dCurrent, lValue);
			else
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
