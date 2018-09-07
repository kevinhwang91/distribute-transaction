package org.jackson.puppy.redis.lock.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jackson.puppy.redis.lock.RedisLock;
import org.jackson.puppy.redis.lock.annotation.RdLock;
import org.jackson.puppy.redis.lock.exception.RedisLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
//  @Order(Ordered.LOWEST_PRECEDENCE)让此增强权限最低，目的为了让TX事务包含这个增强，涉及到redis锁超时事务回滚。
public class RedisLockAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RedisLock redisLock;

	@Around("@annotation(org.jackson.puppy.redis.lock.annotation.RdLock) && @annotation(lock)")
	public Object around(ProceedingJoinPoint pjp, RdLock lock) throws Throwable {
		long retry = lock.retry();
		long interval = lock.retryInterval();
		// redis锁的超时时间。
		long expire = lock.expire();
		Object[] args = pjp.getArgs();
		//  redis锁的key。
		String key = lock.preName() + args[lock.keyIndex()].toString();
		//  redis锁的value。谁加的锁，就由谁解锁。解锁会判断这个value。
		//  若没有这个value，假设A获取锁，处理完业务，但是此时锁已经超时。
		//  在A的锁超时之后，完成业务之前，有B获取锁。
		//  当A执行解锁会把B的锁解掉，其他新请求的线程就会和B有并发竞争。
		String owner = UUID.randomUUID().toString();
		Object ret;
		boolean isLock;
		boolean isReleaseLock;

		while (!(isLock = redisLock.tryLock(key, expire, owner)) && retry > 0) {
			logger.debug("Try lock failed, try again after {} milliseconds.", interval);
			Thread.sleep(interval);
			retry--;
		}

		if (isLock) {
			logger.info("Start to process service.");
			try {
				ret = pjp.proceed(args);
			} finally {
				//  业务有可能抛异常，但锁最终都要解掉，让后面的请求可以正常获取锁
				isReleaseLock = redisLock.releaseLock(key, owner);
			}
			if (!isReleaseLock) {
				logger.info("Please guarantee service execute time less than lock expired time.");
				throw new RedisLockException("Lock had expired.", 1);
			} else {
				logger.info("End to process service.");
			}
		} else {
			throw new RedisLockException("Fail to acquire lock.", 0);
		}
		return ret;
	}
}
