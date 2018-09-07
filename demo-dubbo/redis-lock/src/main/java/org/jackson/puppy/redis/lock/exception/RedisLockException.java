package org.jackson.puppy.redis.lock.exception;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class RedisLockException extends Exception {

	private static final long serialVersionUID = -4009664175109486670L;

	public int id;

	public RedisLockException(int id) {
		this.id = id;
	}

	public RedisLockException(String message, int id) {
		super(message);
		this.id = id;
	}

	public RedisLockException(String message, Throwable cause, int id) {
		super(message, cause);
		this.id = id;
	}

	public RedisLockException(Throwable cause, int id) {
		super(cause);
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
