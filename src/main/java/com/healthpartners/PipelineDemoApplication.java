package com.healthpartners;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PipelineDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PipelineDemoApplication.class, args);
	}
	
	@GetMapping("/hello")
	public ResponseEntity<String> hello() {
		return new ResponseEntity<>("Hello World!", HttpStatus.OK);
	}
}
