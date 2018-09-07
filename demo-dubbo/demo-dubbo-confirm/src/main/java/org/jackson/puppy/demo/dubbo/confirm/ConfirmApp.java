package org.jackson.puppy.demo.dubbo.confirm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@SpringBootApplication
@ComponentScan({
		"org.jackson.puppy.rabbitmq.common",
		"org.jackson.puppy.demo.dubbo.confirm"
})
public class ConfirmApp {

	private static final Logger logger = LoggerFactory.getLogger(ConfirmApp.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = new SpringApplicationBuilder(ConfirmApp.class)
				.web(WebApplicationType.SERVLET)
				.run(args);
		Arrays.stream(configurableApplicationContext.getBeanDefinitionNames())
				.sorted().forEach(beanName -> logger.info("Loaded Bean: {}", beanName));
	}
}
