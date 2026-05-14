package io.orphea.sharedutils.Exceptions;

public class MaxLimitExceededException extends RuntimeException {
    public MaxLimitExceededException(String message) {
        super(message);
    }
}