package com.notesbackend.controller;

import com.notesbackend.model.User;
import com.notesbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.userExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }
        
        User newUser = userService.createUser(user);
        
        // Return success response with the created user
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }
}
