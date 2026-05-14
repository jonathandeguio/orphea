package io.orphea.connect.library.models;

import java.util.List;

public class Result {
    private final long updateCount;
    private final List<String> columns;
    private final List<List<String>> data;
    private final String sql;

    public Result(long updateCount, List<String> columns, List<List<String>> data, String sql) {
        this.updateCount = updateCount;
        this.columns = columns;
        this.data = data;
        this.sql = sql;
    }

    public long getUpdateCount() {
        return this.updateCount;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public List<List<String>> getData() {
        return this.data;
    }

    public String getSql() {
        return this.sql;
    }
}
