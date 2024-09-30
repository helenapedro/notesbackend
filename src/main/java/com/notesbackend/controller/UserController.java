package com.notesbackend.controller;

import com.notesbackend.model.User;

import com.notesbackend.service.UserService;
import com.notesbackend.dto.AuthenticateUserDto;
import com.notesbackend.dto.JwtResponse;
import com.notesbackend.dto.RegisterUserDto;
import com.notesbackend.dto.UpdateUserDto;
import com.notesbackend.exception.CustomAuthenticationException;
import com.notesbackend.mapper.UserMapper;
import com.notesbackend.util.JwtUtil;
import com.notesbackend.controller.UserController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return userService.getAllUsers(pageable);
    }

    // Fetch authenticated user
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{uid}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long uid) {
        Optional<User> user = userService.getUserById(uid);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserDto userDto) {
        if (userService.userExists(userDto.getEmail())) {
            LOGGER.warn("Registration attempt failed. Email {} is already in use.", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
        }

        // Registration logic
        User user = userMapper.toUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userService.createUser(user);
        LOGGER.info("User registered successfully with email: {}", userDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // Authenticate user and issue a JWT
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthenticateUserDto userDto) {
        Optional<User> optionalUser = userService.getUserByEmail(userDto.getEmail());

        if (optionalUser.isPresent() && passwordEncoder.matches(
                userDto.getPassword(),
                optionalUser.get().getPassword())) {
            User user = optionalUser.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getUid());
            LOGGER.info("Authentication successful for email: {}", userDto.getEmail());
            return ResponseEntity.ok(new JwtResponse(token));
        }

        LOGGER.warn("Authentication failed for email: {}", userDto.getEmail());
        throw new CustomAuthenticationException("Invalid email or password");
    }

    @PutMapping("/{uid}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long uid,
            @Valid @RequestBody UpdateUserDto updatedUserDto,
            Authentication authentication) {

        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!currentUser.getUid().equals(uid) && !userService.requestingUserHasAdminRole(currentUser.getUid())) {
            throw new AccessDeniedException("You are not allowed to update this user.");
        }

        User updated = userService.updateUser(updatedUserDto, uid, currentUser.getUid());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<?> deleteUser(@PathVariable Long uid, Authentication authentication) {
        // Fetch the authenticated user based on their email
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if the user is trying to delete their own account or has admin
        // privileges
        if (!currentUser.getUid().equals(uid) && !userService.requestingUserHasAdminRole(currentUser.getUid())) {
            throw new AccessDeniedException("You are not allowed to delete this user.");
        }

        LOGGER.info("Attempting to delete user with ID: {}", uid);

        return userService.getUserById(uid)
                .map(user -> {
                    userService.deleteUser(uid, currentUser.getUid());
                    LOGGER.info("User with ID: {} deleted successfully", uid);
                    return ResponseEntity.ok("User deleted successfully.");
                })
                .orElseGet(() -> {
                    LOGGER.warn("User with ID: {} not found", uid);
                    return ResponseEntity.notFound().build();
                });
    }
}
