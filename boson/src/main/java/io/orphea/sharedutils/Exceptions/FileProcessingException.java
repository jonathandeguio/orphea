package io.orphea.sharedutils.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }
}