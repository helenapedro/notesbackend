package com.notesbackend.dto;

import lombok.Data;

@Data
public class AuthenticateUserDto {

	private String email;
    private String password;
}
