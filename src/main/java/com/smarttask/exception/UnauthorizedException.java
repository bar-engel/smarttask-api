package com.smarttask.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Access denied to this resource");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
