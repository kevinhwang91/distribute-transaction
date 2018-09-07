package org.jackson.puppy.tcc.transaction.utils;

import java.util.Collection;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public final class CollectionUtils {

	private CollectionUtils() {

	}

	public static boolean isEmpty(Collection collection) {
		return (collection == null || collection.isEmpty());
	}
}