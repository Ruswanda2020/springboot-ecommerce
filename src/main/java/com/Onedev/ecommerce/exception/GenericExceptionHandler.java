package com.Onedev.ecommerce.exception;

import com.Onedev.ecommerce.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleResourceNotFoundException(HttpServletRequest request,
                                                                       ResourceNotFoundException exception){
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse HandleBadRequestException(HttpServletRequest request,
                                                                       BadRequestException exception){
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse HandleGenericException(HttpServletRequest request,
                                                                 Exception exception){
        log.error("terjadi error. status code {}error message {}", HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse HandleValidationException(MethodArgumentNotValidException exception){

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors()
                .forEach(objectError -> {
                    String fieldName = ((FieldError) objectError).getField();
                    String errorMessage = objectError.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        return ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(errors.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
