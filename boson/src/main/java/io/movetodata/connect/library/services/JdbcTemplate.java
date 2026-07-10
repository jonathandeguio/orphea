package io.movetodata.connect.library.services;

import io.movetodata.connect.library.models.Result;
import io.movetodata.connect.library.models.Results;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    protected final Connection connection;

    public JdbcTemplate(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Results executeStatement(String sql) throws SQLException {
        Results results = new Results();
        Statement statement = null;

        try {
            statement = this.connection.createStatement();
            statement.setEscapeProcessing(false);
            boolean hasResults = statement.execute(sql);
            this.extractResults(results, statement, sql, hasResults);
//            this.extractWarnings(results, statement);
        } catch (SQLException var8) {
            throw var8;
//            this.extractErrors(results, e);
        } finally {
            JdbcUtils.closeStatement(statement);
        }

        return results;
    }

    private void extractResults(Results results, Statement statement, String sql, boolean hasResults) throws SQLException {
        for (int updateCount = -1; hasResults || (updateCount = statement.getUpdateCount()) != -1; hasResults = statement.getMoreResults()) {
            List<String> columns = null;
            List<List<String>> data = null;
            if (hasResults) {
                ResultSet resultSet = statement.getResultSet();

                try {
                    columns = new ArrayList();
                    ResultSetMetaData metadata = resultSet.getMetaData();
                    int columnCount = metadata.getColumnCount();

                    for (int i = 1; i <= columnCount; ++i) {
                        columns.add(metadata.getColumnName(i));
                    }

                    data = new ArrayList();

                    while (resultSet.next()) {
                        List<String> row = new ArrayList();

                        for (int i = 1; i <= columnCount; ++i) {
                            row.add(resultSet.getString(i));
                        }

                        data.add(row);
                    }
                } catch (Throwable var14) {
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (Throwable var13) {
                            var14.addSuppressed(var13);
                        }
                    }

                    throw var14;
                }

                if (resultSet != null) {
                    resultSet.close();
                }
            }

            results.addResult(new Result(updateCount, columns, data, sql));
        }

    }
}
