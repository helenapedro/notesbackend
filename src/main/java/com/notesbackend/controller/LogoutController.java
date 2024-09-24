package com.notesbackend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LogoutController {

	@PostMapping("/logout")
	public String logout() {
        // Invalidate the token on the client side by removing it from storage.
        return "Logged out successfully";
    }
}
