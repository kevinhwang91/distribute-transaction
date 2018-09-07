package org.jackson.puppy.tcc.transaction.serializer;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface ObjectSerializer<T> {

	/**
	 * Serialize the given object to binary data.
	 *
	 * @param t object to serialize
	 * @return the equivalent binary data
	 */
	byte[] serialize(T t);

	/**
	 * Deserialize an object from the given binary data.
	 *
	 * @param bytes object binary representation
	 * @return the equivalent object instance
	 */
	T deserialize(byte[] bytes);
}
