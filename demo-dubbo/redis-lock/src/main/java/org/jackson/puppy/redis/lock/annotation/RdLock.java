package org.jackson.puppy.redis.lock.annotation;

import java.lang.annotation.*;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RdLock {

	String preName() default "_:";

	int keyIndex() default 0;

	long expire() default 0;

	int retry() default 1;

	long retryInterval() default 1000;
}
