package io.orphea.kepler.library.models;

import io.orphea.kepler.enums.LogicalOperator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_filter")
public class DatasetFilterModel {
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    private String columnName;

    @NotNull
    private String columnType;

    private String key;
    private UUID datasetId;

    @NotNull
    private LogicalOperator logicalOperator;

    @NotNull
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FilterModel> filters;

    public DatasetFilterModel(DatasetFilterModel other) {
        this.columnName = other.columnName;
        this.columnType = other.columnType;
        this.logicalOperator = other.logicalOperator;

        ArrayList<FilterModel> deepCopy = new ArrayList<>();
        for (FilterModel obj : other.filters) {
            FilterModel filterModel = new FilterModel(obj);
            deepCopy.add(filterModel);
        }

        this.filters = deepCopy;
    }

    public DatasetFilterModel(String columnName, String columnType, String key, UUID datasetId, LogicalOperator logicalOperator, List<FilterModel> filters) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.key = key;
        this.datasetId = datasetId;
        this.logicalOperator = logicalOperator;

        ArrayList<FilterModel> deepCopy = new ArrayList<>();
        for (FilterModel obj : filters) {
            deepCopy.add(new FilterModel(obj));
        }

        this.filters = deepCopy;
    }
}
