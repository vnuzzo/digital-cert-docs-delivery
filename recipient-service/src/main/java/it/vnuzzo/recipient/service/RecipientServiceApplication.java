package it.vnuzzo.recipient.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"it.vnuzzo.recipient.service", "it.vnuzzo.shared"})
public class RecipientServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipientServiceApplication.class, args);
	}

}
