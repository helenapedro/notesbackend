package com.notesbackend.service;

import com.notesbackend.model.User;
import com.notesbackend.model.Role;
import com.notesbackend.repository.UserMapper;
import com.notesbackend.repository.UserRepository;
import com.notesbackend.dto.RegisterUserDto;
import com.notesbackend.exception.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final RoleService roleService;
    
    @Autowired
    private final UserMapper userMapper;

    public UserService(
    		UserRepository userRepository, 
    		RoleService roleService, 
    		PasswordEncoder passwordEncoder,
    		UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
		this.userMapper = userMapper;
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize()
        ));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleService.getRoleByName("ROLE_USER");
        user.setRoles(List.of(userRole));
        return userRepository.save(user);
    }

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateUser(
    		RegisterUserDto updateUserDto, 
    		Long uid, 
    		Long requestingUserId) {
    	
        Optional<User> userOptional = userRepository.findById(uid);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!user.getUid().equals(requestingUserId) && 
            		!requestingUserHasAdminRole(requestingUserId)) {
                LOGGER.warn("Unauthorized update attempt for user ID: {}", uid);
                throw new AccessDeniedException("Unauthorized attempt to update user");
            }
            
            userMapper.updateUserFromDto(updateUserDto, user);
            
            // If the password is provided, encode and update it
            if (updateUserDto.getPassword() != null && 
            		!updateUserDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
            }

            User savedUser = userRepository.save(user);
            LOGGER.info("User with ID: {} updated successfully", uid);
            return savedUser;
        } else {
            LOGGER.warn("User with ID: {} not found", uid);
            throw new UserNotFoundException(uid);
        }
    }

    public boolean deleteUser(Long uid, Long requestingUserId) {
        Optional<User> userOptional = userRepository.findById(uid);
        if (userOptional.isPresent()) {
            if (!userOptional.get().getUid().equals(requestingUserId) && !requestingUserHasAdminRole(requestingUserId)) {
                LOGGER.warn("Unauthorized deletion attempt for user ID: {}", uid);
                throw new AccessDeniedException("Unauthorized attempt to delete user");
            }

            userRepository.deleteById(uid);
            LOGGER.info("User with ID: {} deleted successfully", uid);
            return true;
        } else {
            LOGGER.warn("User with ID: {} not found", uid);
            throw new UserNotFoundException(uid);
        }
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Using Spring Security's User class
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    public boolean requestingUserHasAdminRole(Long requestingUserId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return false;
    }
}
