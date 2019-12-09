package Common;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.stream.Collectors;
import DataModels.EmployeeHealthCareData;
import DataModels.EmployeeHealthCareInfo;

//Helper methods used to print anonymize data to HTML output file
public class Common {

	// print the final analysis result for anonymized m-invariance improved output
	public void PrintConclusionForReleasePublication(Boolean isReleasePublished, Boolean isOriginalMInvariance,
			Boolean isSequentialRelease) throws IOException {
		PrintResults("<b><h1>Conclusion</b></h1> ", "", isOriginalMInvariance, isSequentialRelease);

		String tableHeadingTag = "<span " + "style=\"color:ForestGreen	;\"" + ">";
		if (isReleasePublished)
			PrintResults(tableHeadingTag + "<b>This Anonymized Release can be published<br /><br /> </b></span>",
					"Conclusion Stage completed", isOriginalMInvariance, isSequentialRelease);
		else {
			tableHeadingTag = "<span " + "style=\"color:darkred	;\"" + ">";
			PrintResults(tableHeadingTag + "<b>This Anonymized Release cannot be published<br /><br /> </b></span>",
					"Conclusion Stage completed", isOriginalMInvariance, isSequentialRelease);
		}
	}

	// convert the data tables to HTML Table String for previous , current, common
	// and disjoint data set
	public String ConvertPreviouCurrentListIntoHTMLTableString(List<EmployeeHealthCareData> list, String heading,
			String cssClassName, Boolean isInitialStage, Boolean isFinalStage) {

		String htmlString = "<table class=" + "\"" + cssClassName + "\"" + "><tr><td colspan=\"4\"><h1><center>"
				+ heading + "</center></h1></td></tr><tr><th>EmployeeId</th><th>Age</th><th>Occupation</th></tr>";
		int i = 0;
		for (EmployeeHealthCareData data : list) {
			String trTagString = "<tr>";
			if (isInitialStage) {
				trTagString = "<tr " + "style=\"background-color:#D0E4F5;\"" + ">";
				if (i % 2 != 0)
					trTagString = "<tr " + "style=\"background-color:#EEEEEE;\"" + ">";
			} else if (isFinalStage) {
				trTagString = "<tr " + "style=\"background-color:PaleGoldenrod;\"" + ">";
				if (i % 2 != 0)
					trTagString = "<tr " + "style=\"background-color:DarkKhaki;\"" + ">";
			} else {
				trTagString = "<tr " + "style=\"background-color:NavajoWhite;\"" + ">";
				if (i % 2 != 0)
					trTagString = "<tr " + "style=\"background-color:Cornsilk;\"" + ">";
			}

			htmlString = htmlString + trTagString + "<td>" + data.EmployeeId + "</td><td>" + data.Age + "</td><td>"
					+ data.Occupation + "</td></tr>";
			i = i + 1;
		}
		htmlString = htmlString + "</table>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;";
		return htmlString;
	}

	// convert the data tables to HTML Table String for unbalanced,new balanced and
	// anonymized buckets
	public String ConvertBucketIntoHTMLTableString(List<EmployeeHealthCareInfo> list, String heading,
			String cssClassName, Boolean isInitialStage, Boolean isFinalStage) {

		String htmlString = "<table class=" + "\"" + cssClassName + "\"" + "><tr><td colspan=\"5\"><h1><center>"
				+ heading
				+ "</center></h1></td></tr></tr><tr><th>GroupId</th><th>EmployeeId</th><th>Age</th><th>Occupation</th></tr>";
		List<Integer> consumedGroupIds = new ArrayList<Integer>();
		for (EmployeeHealthCareInfo data : list) {
			if (!consumedGroupIds.contains(data.GroupId)) {
				for (EmployeeHealthCareData groupInfo : data.GroupInfo) {
					String trTagString = (data.GroupId % 2 == 0
							? ("<tr " + "style=\"background-color:Cornsilk;\"" + ">")
							: ("<tr " + "style=\"background-color:NavajoWhite;\"" + ">"));
					if (isFinalStage)
						trTagString = (data.GroupId % 2 == 0
								? ("<tr " + "style=\"background-color:PaleGoldenrod;\"" + ">")
								: ("<tr " + "style=\"background-color:DarkKhaki;\"" + ">"));
					if (isInitialStage)
						trTagString = (data.GroupId % 2 == 0 ? ("<tr " + "style=\"background-color:#EEEEEE;\"" + ">")
								: ("<tr " + "style=\"background-color:#D0E4F5;\"" + ">"));

					htmlString = htmlString + trTagString + "<td>" + data.GroupId + "</td><td>"
							+ ((groupInfo.EmployeeId != "null" && groupInfo.EmployeeId != null
									&& groupInfo.EmployeeId != "")
											? (groupInfo.EmployeeId == "Fake"
													? ("<span " + "style=\"color:red;\"" + "><b>" + groupInfo.EmployeeId
															+ "</b></span>")
													: groupInfo.EmployeeId)
											: "")
							+ "</td><td>" + groupInfo.Age + "</td><td>" + groupInfo.Occupation + "</td></tr>";
				}
			}
			if (!consumedGroupIds.contains(data.GroupId))
				consumedGroupIds.add(data.GroupId);

		}
		htmlString = htmlString + "</table>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;";
		return htmlString;
	}

	// convert the data tables to HTML Table String for unbalanced,new balanced and
	// anonymized buckets
	public String ConvertAnonymizedListIntoHTMLTableString(List<EmployeeHealthCareInfo> list, String heading,
			String cssClassName, Boolean isInitialStage, Boolean isFinalStage) {

		String htmlString = "<table class=" + "\"" + cssClassName + "\"" + "><tr><td colspan=\"5\"><h1><center>"
				+ heading + "</center></h1></td></tr></tr><tr><th>GroupId</th><th>Age</th><th>Occupation</th></tr>";
		List<Integer> consumedGroupIds = new ArrayList<Integer>();
		for (EmployeeHealthCareInfo data : list) {
			if (!consumedGroupIds.contains(data.GroupId)) {
				for (EmployeeHealthCareData groupInfo : data.GroupInfo) {
					String trTagString = (data.GroupId % 2 == 0
							? ("<tr " + "style=\"background-color:Cornsilk;\"" + ">")
							: ("<tr " + "style=\"background-color:NavajoWhite;\"" + ">"));
					if (isFinalStage)
						trTagString = (data.GroupId % 2 == 0
								? ("<tr " + "style=\"background-color:PaleGoldenrod;\"" + ">")
								: ("<tr " + "style=\"background-color:DarkKhaki;\"" + ">"));
					if (isInitialStage)
						trTagString = (data.GroupId % 2 == 0 ? ("<tr " + "style=\"background-color:#EEEEEE;\"" + ">")
								: ("<tr " + "style=\"background-color:#D0E4F5;\"" + ">"));

					htmlString = htmlString + trTagString + "<td>" + data.GroupId + "</td><td>" + groupInfo.Age
							+ "</td><td>" + groupInfo.Occupation + "</td></tr>";
				}
			}
			if (!consumedGroupIds.contains(data.GroupId))
				consumedGroupIds.add(data.GroupId);
		}
		htmlString = htmlString + "</table>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;";
		return htmlString;
	}

	// append the input HTML string to the output HTML file
	public void PrintResults(String htmlString, String consoleMessage, Boolean isOriginalMInvariance,
			Boolean isSequentialRelease) throws IOException {
		String fileLocation = "";
		if (isOriginalMInvariance && isSequentialRelease)
			fileLocation = Constants.OriginalMInvarianceHTMLOutputFilePath;
		else if (!isOriginalMInvariance && isSequentialRelease)
			fileLocation = Constants.ImprovedMInvarianceHTMLOutputFilePath;
		else if (!isOriginalMInvariance && !isSequentialRelease)
			fileLocation = Constants.LDiversityHTMLOutputFilePath;
		String html = new String(Files.readAllBytes(Paths.get(fileLocation)));
		html = html.replace("This is the Output", htmlString + "This is the Output");

		if (consoleMessage.contains("Conclusion Stage")) {
			consoleMessage = consoleMessage + "\nOutput File created at location :" + fileLocation;
			html = html.replace("This is the Output", "");
		}

		PrintWriter out = new PrintWriter(fileLocation);
		out.print(html);
		out.close();

		System.out.println(consoleMessage);
	}

	// initialize output HTML file with HTML file template CSS
	public void IntitalizeFileOutput(Boolean isOriginalMInvariance, Boolean isSequentialRelease) throws IOException {
		String path = "";
		if (isOriginalMInvariance && isSequentialRelease)
			path = Constants.OriginalMInvarianceHTMLOutputFilePath;
		else if (!isOriginalMInvariance && isSequentialRelease)
			path = Constants.ImprovedMInvarianceHTMLOutputFilePath;
		else if (!isOriginalMInvariance && !isSequentialRelease)
			path = Constants.LDiversityHTMLOutputFilePath;
		String templatePath = Constants.HTMLOutputTemplateFilePath;
		String html = new String(Files.readAllBytes(Paths.get(templatePath)));

		PrintWriter out = new PrintWriter(path);
		out.print("");
		out.close();
		out = new PrintWriter(path);
		out.print(html);
		out.close();
	}

	// get Max value of Quasi identifier present in the group
	public int GetMaxAgeValue(List<EmployeeHealthCareData> groupInfo) {
		List<String> ages = groupInfo.stream().map(EmployeeHealthCareData::getAge).collect(Collectors.toList()).stream()
				.distinct().collect(Collectors.toList());
		List<Integer> agesList = new ArrayList<Integer>();
		for (String age : ages) {
			if (age.contains("-")) {
				Integer minAge = Integer.parseInt(age.split("-")[0]);
				Integer maxAge = Integer.parseInt(age.split("-")[1]);
				agesList.add(minAge);
				agesList.add(maxAge);
			} else
				agesList.add(Integer.parseInt(age));

		}
		List<Integer> unsorted = agesList.stream().distinct().collect(Collectors.toList());
		List<Integer> sortedList = SortedList(agesList);
		return sortedList.get(agesList.size() - 1);

	}

	// get min value of Quasi identifier present in the group
	public int GetMinAgeValue(List<EmployeeHealthCareData> groupInfo) {
		List<String> ages = groupInfo.stream().map(EmployeeHealthCareData::getAge).collect(Collectors.toList()).stream()
				.distinct().collect(Collectors.toList());
		List<Integer> agesList = new ArrayList<Integer>();
		for (String age : ages) {
			if (age.contains("-")) {
				Integer minAge = Integer.parseInt(age.split("-")[0]);
				Integer maxAge = Integer.parseInt(age.split("-")[1]);
				agesList.add(minAge);
				agesList.add(maxAge);
			} else
				agesList.add(Integer.parseInt(age));

		}
		List<Integer> unsorted = agesList.stream().distinct().collect(Collectors.toList());
		List<Integer> sortedList = SortedList(agesList);
		return sortedList.get(0);

	}

	public List<Integer> SortedList(List<Integer> list) {
		int array[] = list.stream().mapToInt(i -> i).toArray();

		int n, temp;
		n = array.length;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (array[i] > array[j]) {
					temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
		return Arrays.stream(array).boxed().collect(Collectors.toList());
	}

	public List<Integer> SortedStringList(List<String> stringList) {
		List<Integer> integerList = new ArrayList<Integer>();
		for (String i : stringList)
			integerList.add(Integer.parseInt(i));

		int array[] = integerList.stream().mapToInt(i -> i).toArray();

		int n, temp;
		n = array.length;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (array[i] > array[j]) {
					temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
		return Arrays.stream(array).boxed().collect(Collectors.toList());
	}

	public double CalculateMeanSquareError(List<EmployeeHealthCareInfo> dAnonymized) {
		Common common = new Common();
		int numberOfRecords = 0;
		double totalPow = 0.0;
		for (EmployeeHealthCareInfo empInfo : dAnonymized) {
			numberOfRecords = numberOfRecords + (int) empInfo.GroupInfo.stream().count();
			int maxAge = common.GetMaxAgeValue(empInfo.GroupInfo);
			int minAge = common.GetMinAgeValue(empInfo.GroupInfo);
			int difference = maxAge - minAge;
			totalPow = totalPow + Math.pow(difference, 2);

		}
		double div = totalPow / numberOfRecords;
		return Math.sqrt(div);
	}

}
