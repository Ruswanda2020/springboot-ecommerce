package com.oneDev.ecommerce.exception;

import com.oneDev.ecommerce.enumaration.ExceptionType;
import com.oneDev.ecommerce.model.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @ExceptionHandler({
            ApplicationException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse  handleApplicationException(HttpServletRequest req,
                                                                       HttpServletResponse resp,
                                                                       ApplicationException exception) {

        ExceptionType type = exception.getType();
        HttpStatus status = HttpStatus.resolve(type.getHttpCode());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Atur status HTTP pada response
        resp.setStatus(status.value());

        return  ErrorResponse.builder()
                .code(status.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errors.toString())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
