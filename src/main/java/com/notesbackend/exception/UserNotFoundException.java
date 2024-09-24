package com.notesbackend.exception;

public class UserNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public UserNotFoundException(Long uid) {
		super("User with ID " + uid + " not found.");
	}
}
