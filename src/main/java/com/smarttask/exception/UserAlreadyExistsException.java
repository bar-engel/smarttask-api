package com.smarttask.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String field, String value) {
        super(field + " '" + value + "' already exists");
    }
}
