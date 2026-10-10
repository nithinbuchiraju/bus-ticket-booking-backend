package com.busbooking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String msg) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), msg));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(SeatAlreadyBookedException.class)
    ResponseEntity<ErrorResponse> seatTaken(SeatAlreadyBookedException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ResponseEntity<ErrorResponse> conflict(Exception e) {
        return build(HttpStatus.CONFLICT, "Seat was just taken by someone else. Please pick another seat.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> unauthorized(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException e) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to do that");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, msg);
    }
}
