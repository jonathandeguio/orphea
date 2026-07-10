package io.movetodata.connect.library.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SourceSQLTableExplanation {
    public String rowsCount;
    public String tableSize;
    public String createdAt;
    public String updatedAt;
    private List<List<String>> columns;
    private String ddlQuery;
}
