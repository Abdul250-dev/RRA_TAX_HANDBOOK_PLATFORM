package com.rra.taxhandbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaxHandbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxHandbookApplication.class, args);
	}
}
