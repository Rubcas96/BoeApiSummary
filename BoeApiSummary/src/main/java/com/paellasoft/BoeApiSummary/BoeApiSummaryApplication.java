package com.paellasoft.BoeApiSummary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BoeApiSummaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoeApiSummaryApplication.class, args);
	}

}
