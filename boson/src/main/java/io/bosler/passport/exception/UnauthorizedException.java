package io.bosler.passport.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException() {
        super();
    }

    /**
     * Constructs a {@code UnauthorizedException}, saving a reference
     * to the error message string {@code s} for later retrieval by the
     * {@code getMessage} method.
     *
     * @param   s   the detail message.
     */
    public UnauthorizedException(String s) {
        super(s);
    }
}
