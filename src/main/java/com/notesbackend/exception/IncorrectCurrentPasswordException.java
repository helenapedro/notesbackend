package com.notesbackend.exception;

import org.springframework.security.access.AccessDeniedException;

public class IncorrectCurrentPasswordException extends AccessDeniedException {
	private static final long serialVersionUID = 1L;
    public IncorrectCurrentPasswordException(String message) {
        super(message);
    }
}