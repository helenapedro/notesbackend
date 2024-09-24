package com.notesbackend.controller;

import com.notesbackend.model.User;
import com.notesbackend.service.UserService;
import com.notesbackend.dto.AuthenticateUserDto;
import com.notesbackend.dto.JwtResponse;
import com.notesbackend.dto.RegisterUserDto;
import com.notesbackend.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Fetch authenticated user
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }

    // Fetch all users (only for admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Fetch a user by ID (only admin or the authenticated user can access their own data)
    @GetMapping("/{uid}")
    @PreAuthorize("hasRole('ADMIN') or #uid == authentication.principal.uid")
    public ResponseEntity<User> getUserById(@PathVariable Long uid) {
        Optional<User> user = userService.getUserById(uid);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserDto userDto) {
        if (userService.userExists(userDto.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already in use.");
        }

        // Create a new user and save
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setBirthday(userDto.getBirthday());
        user.setGender(userDto.getGender());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());

        userService.createUser(user);
        return ResponseEntity.ok(user);
    }

    // Authenticate user and issue a JWT
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticateUserDto userDto) {
        User user = userService.getUserByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getEmail(), user.getUid());
            return ResponseEntity.ok(new JwtResponse(token));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    // Update user (only the user themselves or an admin can update their info)
    @PutMapping("/{uid}")
    @PreAuthorize("hasRole('ADMIN') or #uid == authentication.principal.uid")
    public ResponseEntity<User> updateUser(@PathVariable Long uid, @RequestBody User updatedUser, Authentication authentication) {
        // Ensure user can only update their own info
        String email = authentication.getName();
        Optional<User> userOptional = userService.getUserById(uid);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getEmail().equals(email) && !authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).body(null); // Forbidden if not admin and not the same user
            }
            User updated = userService.updateUser(updatedUser, uid);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete user (only the user themselves or an admin can delete)
    @DeleteMapping("/{uid}")
    @PreAuthorize("hasRole('ADMIN') or #uid == authentication.principal.uid")
    public ResponseEntity<?> deleteUser(@PathVariable Long uid, Authentication authentication) {
        Optional<User> userOptional = userService.getUserById(uid);

        if (userOptional.isPresent()) {
            String email = authentication.getName();
            User user = userOptional.get();
            if (!user.getEmail().equals(email) && !authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).body("You are not authorized to delete this user.");
            }
            userService.deleteUser(uid);
            return ResponseEntity.ok("User deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }
}
