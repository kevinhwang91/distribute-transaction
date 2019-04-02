package org.jackson.puppy.demo.dubbo.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@SpringBootApplication
@ImportResource({"classpath:application-context-dubbo.xml"})
@ComponentScan({
		"org.jackson.puppy.rabbitmq.common",
		"org.jackson.puppy.redis.lock",
		"org.jackson.puppy.demo.dubbo.order",
		"org.jackson.puppy.tcc.transaction.spring"
})
@EnableAsync
public class OrderApp {

	private static final Logger logger = LoggerFactory.getLogger(OrderApp.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = new SpringApplicationBuilder(OrderApp.class)
				.web(WebApplicationType.SERVLET)
				.run(args);
		Arrays.stream(configurableApplicationContext.getBeanDefinitionNames())
				.sorted().forEach(beanName -> logger.info("Loaded Bean: {}", beanName));
	}
}
