package com.notesbackend.dto;

import java.util.Date;

import lombok.Data;

@Data
public class RegisterUserDto {

    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private Date birthday;
    private String gender;
    private String phoneNumber;
    private String address;
}
