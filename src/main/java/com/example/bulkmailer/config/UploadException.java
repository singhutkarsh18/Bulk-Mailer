package com.example.bulkmailer.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.mail.MessagingException;
import java.io.IOException;

@ControllerAdvice
public class UploadException {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Max file size limit exceeded");
    }
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<?> handleIOException(MessagingException exc) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error file not found\n1. Maybe file not found\n2. Maybe credentials wrong \n ");
    }

}