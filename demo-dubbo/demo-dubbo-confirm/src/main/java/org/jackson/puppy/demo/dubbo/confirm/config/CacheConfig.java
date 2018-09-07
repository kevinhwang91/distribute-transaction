package org.jackson.puppy.demo.dubbo.confirm.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class CacheConfig {

	@Bean
	public Cache<String, Integer> cacheKStringVInteger() {
		return Caffeine.newBuilder()
				.maximumSize(10000)
				.expireAfterAccess(600 * 1000, TimeUnit.MILLISECONDS)
				.build();
	}
}
