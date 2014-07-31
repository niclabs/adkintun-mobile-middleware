package cl.niclabs.adkmobile.utils;

public class StringUtil {
	/**
	 * Converts strings in java notation ('thisIsAVariable') to SQL notation 'this_is_a_variable'
	 * 
	 * Copied from https://github.com/satyan/sugar/releases/tag/v1.3
	 * 
	 * @param javaNotation
	 * @return
	 */
	public static String toSQLName(String javaNotation) {
		if (javaNotation.equalsIgnoreCase("_id"))
			return "_id";

		StringBuilder sb = new StringBuilder();
		char[] buf = javaNotation.toCharArray();

		for (int i = 0; i < buf.length; i++) {
			char prevChar = (i > 0) ? buf[i - 1] : 0;
			char c = buf[i];
			char nextChar = (i < buf.length - 1) ? buf[i + 1] : 0;
			boolean isFirstChar = (i == 0);

			if (isFirstChar || Character.isLowerCase(c) || Character.isDigit(c)) {
				sb.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				if (Character.isLetterOrDigit(prevChar)) {
					if (Character.isLowerCase(prevChar)) {
						sb.append('_').append(Character.toUpperCase(c));
					} else if (nextChar > 0 && Character.isLowerCase(nextChar)) {
						sb.append('_').append(Character.toUpperCase(c));
					} else {
						sb.append(c);
					}
				} else {
					sb.append(c);
				}
			}
		}

		return sb.toString();
	}
	
	/**
	 * Converts strings from SQL notation to java notation
	 * @param sqlNotation
	 * @return
	 */
	public static String fromSQLName(String sqlNotation) {
		if (sqlNotation.equalsIgnoreCase("_id"))
			return "_id";

		StringBuilder sb = new StringBuilder();
		char[] buf = sqlNotation.toCharArray();

		for (int i = 0; i < buf.length; i++) {
			char prevChar = (i > 0) ? buf[i - 1] : 0;
			char c = buf[i];
			boolean isFirstChar = (i == 0);
			
			if (c == '_') // Ignore underscore
				continue;
			
			if (isFirstChar) {
				sb.append(Character.toLowerCase(c));
			}
			else {
				// Append character in UpperCase unless is the first character of the string
				if (prevChar == '_' && sb.length() > 0) {
					sb.append(Character.toUpperCase(c));
				}
				else { 
					sb.append(Character.toLowerCase(c));
				}
			}
		}

		return sb.toString();
	}
}
