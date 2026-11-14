package com.nhd.processor;

import com.nhd.processor.config.ProcessorProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootApplication
public class NhdProcessorApplication {
	private static final Logger log = LoggerFactory.getLogger(NhdProcessorApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(NhdProcessorApplication.class, args);
	}

	@Bean
    public RestTemplate restTemplate(ProcessorProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getLoaderClientConnectTimeout());
        requestFactory.setReadTimeout(properties.getLoaderClientReadTimeout());
        return new RestTemplate(requestFactory);
    }

	@Bean
	@Order(1)
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("Test run");
		};
	}
}
