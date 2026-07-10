package io.movetodata.fractal.library.Exceptions;

import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitCheckoutConflict extends GitAPIException {
    public GitCheckoutConflict() {
        super("You have unsaved changes.");
    }

    /**
     * Constructs a {@code UnauthorizedException}, saving a reference
     * to the error message string {@code s} for later retrieval by the
     * {@code getMessage} method.
     *
     * @param   s   the detail message.
     */
    public GitCheckoutConflict(String s) {
        super(s);
    }
}
