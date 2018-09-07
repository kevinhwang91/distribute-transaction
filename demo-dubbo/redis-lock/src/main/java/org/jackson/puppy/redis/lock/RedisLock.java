package org.jackson.puppy.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Component
public class RedisLock {

	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	public RedisLock(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	public boolean tryLock(String key, Long expire, String owner) {
		boolean isLock = false;

		String redisReply = stringRedisTemplate.execute((RedisCallback<String>) connection -> {
			//  等效于redis命令 set key value nx px。
			//  如果key不存在则添加这个键值对，并且设置px毫秒后超时。
			return (String) connection.execute("set", key.getBytes(StandardCharsets.UTF_8),
					owner.getBytes(StandardCharsets.UTF_8), "nx".getBytes(StandardCharsets.UTF_8),
					"px".getBytes(StandardCharsets.UTF_8), String.valueOf(expire).getBytes(StandardCharsets.UTF_8));
		});
		if ("OK".equalsIgnoreCase(redisReply)) {
			isLock = true;
		}
		return isLock;
	}

	public Boolean releaseLock(String key, String owner) {
		boolean isReleaseLock = false;

		//  redis lua脚本，目的是为了保证解锁操作的原子性。
		//  判断这个键对应的值和传递的值是否一致，若一致则删除这个键值对。
		DefaultRedisScript<Long> script = new DefaultRedisScript<>("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
		Long redisReply = stringRedisTemplate.execute(script, Collections.singletonList(key), owner);

		if (Objects.equals(redisReply, 1L)) {
			isReleaseLock = true;
		}
		return isReleaseLock;
	}

}
