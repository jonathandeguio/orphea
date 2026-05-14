package io.orphea.kepler.library.models;

import io.orphea.sharedutils.OrpheaUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "kepler_chart_config")
public class ChartConfigModel implements OrpheaUtils {
    @Id
    @GeneratedValue
    private UUID id;

    private String xAxis;
    private String xAxisSort = "asc";
    private String chartType;
    private String xAxisTimeGrain = "date";
    private String longitude;
    private String latitude;
    private String mapZoom;
    private String mapCenter;

    private UUID datasetId;
    private String branch;

    private String sortingMethod= "xaxis";
    private String sortingDirection = "asc";
    private String customColumnName;
    private String customAggregateFunction;

    private int rowLimit = 500;

    private String mapSeries;

    @OneToMany(targetEntity = SeriesModel.class, cascade = CascadeType.ALL)
    private List<SeriesModel> series;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> dimensions;

    @OneToMany(targetEntity = DatasetFilterModel.class, mappedBy = "", cascade = CascadeType.ALL)
    private List<DatasetFilterModel> filter;

    @OneToMany(targetEntity = ParametersModel.class, cascade = CascadeType.ALL)
    private List<ParametersModel> parameters;

    public ChartConfigModel(ChartConfigModel original) {
        this.xAxis = original.xAxis;
        this.xAxisSort = original.xAxisSort;
        this.chartType = original.chartType;
        this.xAxisTimeGrain = original.xAxisTimeGrain;
        this.longitude = original.longitude;
        this.latitude = original.latitude;
        this.mapZoom = original.mapZoom;
        this.mapCenter = original.mapCenter;
        this.datasetId = original.datasetId;
        this.branch = original.branch;
        this.rowLimit = original.rowLimit;
        this.mapSeries = original.mapSeries;

        // Copy the series list by creating new instances of SeriesModel
        if (original.series != null) {
            this.series = new ArrayList<>();
            for (SeriesModel originalSeries : original.series) {
                this.series.add(new SeriesModel(originalSeries));
            }
        }

        // Copy the filter list by creating new instances of DatasetFilterModel
        if (original.filter != null) {
            this.filter = new ArrayList<>();
            for (DatasetFilterModel originalFilter : original.filter) {
                this.filter.add(new DatasetFilterModel(originalFilter));
            }
        }
    }
}

