package io.movetodata.connect.library.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Results {
    public static final org.flywaydb.core.internal.jdbc.Results EMPTY_RESULTS = new org.flywaydb.core.internal.jdbc.Results();
    private final List<Result> results = new ArrayList();
    private final List<Error> errors = new ArrayList();
    private SQLException exception = null;

    public Results() {
    }

    public void addResult(Result result) {
        this.results.add(result);
    }


    public void addError(Error error) {
        this.errors.add(error);
    }

    public List<Result> getResults() {
        return this.results;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public SQLException getException() {
        return this.exception;
    }

    public void setException(SQLException exception) {
        this.exception = exception;
    }
}
