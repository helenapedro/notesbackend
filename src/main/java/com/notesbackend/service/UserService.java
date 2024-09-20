package com.notesbackend.service;

import com.notesbackend.model.User;
import com.notesbackend.model.Role;
import com.notesbackend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
	
	@Autowired
	private final UserRepository userRepository;
	
	@Autowired
	private final PasswordEncoder passwordEncoder;
	
	private final RoleService roleService;
	
	public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
	     this.userRepository = userRepository;
	     this.roleService = roleService;
	     this.passwordEncoder = passwordEncoder;
	 }
	
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	public Optional<User> getUserByEmail(String email) {
		return	 userRepository.findByEmail(email);
	}
	
	public Optional<User> getUserById(Long id) {
		return userRepository.findById(id);
	}
	
	public User createUser(User user) {
	     user.setPassword(passwordEncoder.encode(user.getPassword()));
	     // Gets the role 'ROLE_USER'
	     Role userRole = roleService.getRoleByName("ROLE_USER");
	     user.setRoles(List.of(userRole)); 
	     return userRepository.save(user);
	 }
	
	public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
	
	public User updateUser(User updatedUser, Long uid) {
        Optional<User> userOptional = userRepository.findById(uid);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            user.setPassword(updatedUser.getPassword());
            user.setAddress(updatedUser.getAddress());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setGender(updatedUser.getGender());
            
            return userRepository.save(user);
        }
        return null;
    }
 
	public boolean deleteUserById(Long uid) {
		if (userRepository.existsById(uid)) {
			userRepository.deleteById(uid);
			return true;
		}
		return false;
	}
}
