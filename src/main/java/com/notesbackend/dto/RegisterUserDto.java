package com.notesbackend.dto;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDto {

	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
    private String email;
    
	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    
	@NotBlank(message = "First name is required")
    private String firstname;
	
	@NotBlank(message = "First name is required")
    private String lastname;
	
	@Past(message = "Birthday must be in the past")
	@NotNull(message = "Birthday is required")
    private Date birthday;
	
    private String gender;
    private String phoneNumber;
    private String address;
}