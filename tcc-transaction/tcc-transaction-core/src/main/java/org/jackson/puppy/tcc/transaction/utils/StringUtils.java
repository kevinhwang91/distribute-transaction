package org.jackson.puppy.tcc.transaction.utils;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class StringUtils {

	public static boolean isNotEmpty(String value) {

		if (value == null) {
			return false;
		}

		if ("".equals(value)) {
			return false;
		}

		return true;
	}
}
