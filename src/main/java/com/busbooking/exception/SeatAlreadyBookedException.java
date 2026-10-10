package com.busbooking.exception;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String message) { super(message); }
}
