package com.notesbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NotesApp {

	public static void main(String[] args) {
		SpringApplication.run(NotesApp.class, args);
	}

}
