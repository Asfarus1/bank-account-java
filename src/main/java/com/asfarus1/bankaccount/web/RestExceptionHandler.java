package com.asfarus1.bankaccount.web;

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance;
import com.asfarus1.bankaccount.exceptions.NotFoundException;
import com.asfarus1.bankaccount.exceptions.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handle(NotFoundException ex) {
        return status(NOT_FOUND).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler(NotEnoughBalance.class)
    public ResponseEntity<?> handle(NotEnoughBalance ex) {
        return status(FORBIDDEN).body(ex.getLocalizedMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handle(ValidationException ex) {
        return status(BAD_REQUEST).body(ex.getLocalizedMessage());
    }
}
