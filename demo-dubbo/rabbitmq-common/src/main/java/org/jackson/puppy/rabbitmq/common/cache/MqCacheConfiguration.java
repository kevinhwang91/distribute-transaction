package org.jackson.puppy.rabbitmq.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jackson.puppy.rabbitmq.common.dto.CallBackContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class MqCacheConfiguration {

	@Bean
	public Cache<String, CallBackContext> mqCallBackCache() {
		return Caffeine.newBuilder()
				.maximumSize(10000)
				.expireAfterAccess(600 * 1000, TimeUnit.MILLISECONDS)
				.build();
	}


}
