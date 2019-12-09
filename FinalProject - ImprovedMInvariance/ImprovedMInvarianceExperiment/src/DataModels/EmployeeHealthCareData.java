package DataModels;

public class EmployeeHealthCareData {

	public int Id;

	public int getId() {
		return Id;
	}

	public void setId(Integer newId) {
		this.Id = newId;
	}

	// Employee Identifier EmployeeIds
	public String EmployeeId;

	public String getEmployeeId() {
		return EmployeeId;
	}

	public void setEmployeeId(String newEmployeeId) {
		this.EmployeeId = newEmployeeId;
	}

	// Employee Quasi Identifier Age
	public String Age;

	public String getAge() {
		return Age;
	}

	public void setAge(String newAge) {
		this.Age = newAge;
	}

	// Employee Sensitive Attribute Occupation
	public String Occupation;

	public String getOccupation() {
		return Occupation;
	}

	public void setOccupation(String newOccupation) {
		this.Occupation = newOccupation;
	}


}
