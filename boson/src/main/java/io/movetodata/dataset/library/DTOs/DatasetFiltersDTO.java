package io.movetodata.dataset.library.DTOs;

import io.movetodata.kepler.enums.LogicalOperator;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DatasetFiltersDTO {
    private UUID id;
    private UUID datasetId;
    private String key;
    private String columnName;
    private String columnType;
    private LogicalOperator logicalOperator;
    private List<FilterDTO> filters;

    public DatasetFiltersDTO(UUID datasetId, String key, String columnName, String columnType, LogicalOperator logicalOperator, List<FilterDTO> filters) {
        this.datasetId = datasetId;
        this.key = key;
        this.columnName = columnName;
        this.columnType = columnType;
        this.logicalOperator = logicalOperator;

        ArrayList<FilterDTO> deepCopy = new ArrayList<>();
        for (FilterDTO obj : filters) {
            deepCopy.add(new FilterDTO(obj));
        }

        this.filters = deepCopy;
    }
}
