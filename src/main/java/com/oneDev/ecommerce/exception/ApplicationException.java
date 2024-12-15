package com.oneDev.ecommerce.exception;

import com.oneDev.ecommerce.enumaration.ExceptionType;
import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException{

    private final ExceptionType type;
    public ApplicationException(ExceptionType type) {
        super(type.getMessage());
        this.type = type;
    }

    public ApplicationException(ExceptionType type, String customMessage) {
        super(customMessage);
        this.type = type;
    }

}
