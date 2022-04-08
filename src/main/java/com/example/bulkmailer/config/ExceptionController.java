package com.example.bulkmailer.config;

import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.mail.MessagingException;


@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Max file size limit exceeded");
    }
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<?> handleIOException(MessagingException exc) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Error file not found\n1. Maybe file not found\n2. Maybe credentials wrong \n ");
    }
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<Error> handleAccessDeniedException(AccessDeniedException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new Error("JWT Token has expired"), HttpStatus.GONE);
    }
    @ExceptionHandler(RuntimeWorkerException.class)
    public ResponseEntity<?> handleRuntimeWorkerException(RuntimeWorkerException exc)
    {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(exc.getLocalizedMessage());
    }


}