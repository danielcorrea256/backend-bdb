package dev.danielcorrea.backbdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackBdbApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackBdbApplication.class, args);
	}

}
