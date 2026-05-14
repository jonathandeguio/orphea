package io.orphea.fractal.library.Exceptions;

import org.eclipse.jgit.api.errors.GitAPIException;

public class GitOutOfSyncException extends GitAPIException {
    public GitOutOfSyncException() {
        super("Git is out of sync");
    }

    /**
     * Constructs a {@code UnauthorizedException}, saving a reference
     * to the error message string {@code s} for later retrieval by the
     * {@code getMessage} method.
     *
     * @param   s   the detail message.
     */
    public GitOutOfSyncException(String s) {
        super(s);
    }
}
