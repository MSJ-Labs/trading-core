package com.msj.domain.user;

/**
 * Exception thrown when User is not found
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserNotFoundException forId(UserId id) {
        return new UserNotFoundException("User not found with id: " + id.value());
    }

    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found with username: " + username);
    }
}
