package io.orphea.fractal.library.Exceptions;

import org.eclipse.jgit.api.errors.GitAPIException;

public class NoSuchBranchException extends GitAPIException {
    public NoSuchBranchException() {
        super("No such branch");
    }

    /**
     * Constructs a {@code UnauthorizedException}, saving a reference
     * to the error message string {@code s} for later retrieval by the
     * {@code getMessage} method.
     *
     * @param   s   the detail message.
     */
    public NoSuchBranchException(String s) {
        super(s);
    }
}
