package io.movetodata.sharedutils.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GeneralConfigurationException extends RuntimeException {
    public GeneralConfigurationException(String message) {
        super(message);
    }
}
