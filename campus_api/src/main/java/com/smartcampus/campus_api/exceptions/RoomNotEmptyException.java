package com.smartcampus.campus_api.exceptions; 

// It is ONLY triggered when someone tries to delete a room that still has active sensors inside it
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}