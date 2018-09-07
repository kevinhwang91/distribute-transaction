package org.jackson.puppy.demo.dubbo.bill.config;

import org.jackson.puppy.tcc.transaction.repository.JdbcAbstractTransactionRepository;
import org.jackson.puppy.tcc.transaction.serializer.KryoTransactionSerializer;
import org.jackson.puppy.tcc.transaction.spring.recover.DefaultRecoverConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Configuration
public class TccConfig {

	@Bean
	@ConfigurationProperties("tcc.repository")
	public JdbcAbstractTransactionRepository jdbcAbstractTransactionRepository(@Qualifier("tccDataSource") DataSource tccDataSource) {
		JdbcAbstractTransactionRepository jdbcAbstractTransactionRepository = new JdbcAbstractTransactionRepository();
		jdbcAbstractTransactionRepository.setDataSource(tccDataSource);
		jdbcAbstractTransactionRepository.setSerializer(new KryoTransactionSerializer());
		return jdbcAbstractTransactionRepository;
	}

	@Bean
	@ConfigurationProperties("tcc.recover")
	public DefaultRecoverConfig defaultRecoverConfig() {
		return new DefaultRecoverConfig();
	}

	@Bean
	public Executor tccExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(16);
		executor.setMaxPoolSize(32);
		executor.setQueueCapacity(1000);
		executor.setThreadNamePrefix("tcc-pool-");
		executor.initialize();
		return executor;
	}
}
