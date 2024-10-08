package com.notesbackend.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.notesbackend.exception.CustomAuthenticationException;
import com.notesbackend.exception.IncorrectCurrentPasswordException;
import com.notesbackend.exception.UserNotFoundException;

import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
    		UsernameNotFoundException ex) {
    	LOGGER.error("Username not found", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "The username does not exist.");
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleCustomAuthenticationException(
    		CustomAuthenticationException ex) {
        LOGGER.error("Authentication error", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentication failed. Please check your email and password.");
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
    		AccessDeniedException ex) {
        LOGGER.error("Access denied", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "You do not have permission to access this resource.");
        response.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
    		MethodArgumentNotValidException ex) {
        LOGGER.error("Validation error occurred", ex);
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation error");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
    		ConstraintViolationException ex) {
        LOGGER.error("Constraint violation occurred", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed for one or more fields.");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        LOGGER.error("An error occurred", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "An unexpected error occurred.");
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(IncorrectCurrentPasswordException.class)
    public ResponseEntity<Object> handleIncorrectCurrentPasswordException(IncorrectCurrentPasswordException ex) {
        return new ResponseEntity<>(new ErrorResponse("Current password is incorrect", HttpStatus.FORBIDDEN.value()), HttpStatus.FORBIDDEN);
    }
    
    public static class ErrorResponse {
    	private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
    }
}