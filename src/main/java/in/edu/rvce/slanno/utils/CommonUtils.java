package in.edu.rvce.slanno.utils;

import org.apache.commons.lang3.StringUtils;

public class CommonUtils {

	public static String removeUnnecessaryCharacters(String before) {
		String after;
		//String temp1=before.replaceAll("\\u000D\\u000A"," ");   //replace "\r\n" with " "
		//String temp2=temp1.replaceAll("[\\u000A]"," ");
		//String temp3=temp2.replaceAll("\\P{Print}"," ");
		//String temp4=temp3.replaceAll("\\\\u000D"," ");
		String temp5=StringUtils.normalizeSpace(before);
		after=StringUtils.normalizeSpace(temp5);
		return after;
	}
}
