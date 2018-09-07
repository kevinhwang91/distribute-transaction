package org.jackson.puppy.demo.dubbo.bill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@SpringBootApplication
@ImportResource({"classpath:application-context-dubbo.xml"})
@ComponentScan({
		"org.jackson.puppy.demo.dubbo.bill",
		"org.jackson.puppy.tcc.transaction.spring"
})
public class BillApp {

	private static final Logger logger = LoggerFactory.getLogger(BillApp.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext configurableApplicationContext = new SpringApplicationBuilder(BillApp.class)
				.web(WebApplicationType.NONE)
				.run(args);
		Arrays.stream(configurableApplicationContext.getBeanDefinitionNames())
				.sorted().forEach(beanName -> logger.info("Loaded Bean: {}", beanName));
	}
}
