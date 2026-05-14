package io.orphea.snap.sharedutils.Exceptions;

public class EnvConfigurationException extends RuntimeException {
    public EnvConfigurationException(String message) {
        super(message);
    }

    public EnvConfigurationException() {
        super();
    }

    public EnvConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnvConfigurationException(Throwable cause) {
        super(cause);
    }
}
