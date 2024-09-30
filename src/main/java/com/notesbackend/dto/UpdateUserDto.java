package com.notesbackend.dto;

import java.util.Date;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    
    private String currentPassword;

    private String firstname;
    private String lastname;
    private Date birthday;
    private String gender;
    private String phoneNumber;
    private String address;
}
