package io.bosler.fractal.library.Exceptions;

public class GitInitializeException extends RuntimeException{
    public GitInitializeException() {
        super();
    }

    /**
     * Constructs a {@code UnauthorizedException}, saving a reference
     * to the error message string {@code s} for later retrieval by the
     * {@code getMessage} method.
     *
     * @param   s   the detail message.
     */
    public GitInitializeException(String s) {
        super(s);
    }
}
