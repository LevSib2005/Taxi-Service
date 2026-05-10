package com.example.user_service.exception;

public class NoAvailableDriversException extends RuntimeException {
    public NoAvailableDriversException() {
        super("Нет доступных водителей");
    }

    public NoAvailableDriversException(String message) {
        super(message);
    }
}