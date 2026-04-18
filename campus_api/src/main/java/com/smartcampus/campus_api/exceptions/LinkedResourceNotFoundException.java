package com.smartcampus.campus_api.exceptions;

//thrown when a value is assigned to a parent item that doesn't exist (a fake room for example)
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}