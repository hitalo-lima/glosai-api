package com.hitalo.glosai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GlosaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlosaiApplication.class, args);
	}

}
