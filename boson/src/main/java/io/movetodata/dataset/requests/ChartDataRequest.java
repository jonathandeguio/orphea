package io.movetodata.dataset.requests;

import io.movetodata.dataset.library.DTOs.DatasetFiltersDTO;
import io.movetodata.kepler.library.models.ParametersModel;
import io.movetodata.sharedutils.MoveToDataUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartDataRequest implements MoveToDataUtils {
    private UUID id;
    private UUID chartUUID;
    private UUID datasetId;
    private String branch;
    private UUID transactionId;
    private String chartType;
    private Integer rowLimit;

    private String xAxis;
    private String xAxisTimeGrain = "date";
    private String longitude;
    private String latitude;
    private String mapZoom;
    private String mapCenter;

    private String sortingMethod;
    private String sortingDirection;
    private String customColumnName;
    private String customAggregateFunction;

    private String mapSeries;
    private List<ChartSeriesRequest> series = new ArrayList<>();
    private List<String> dimensions = new ArrayList<>();
    private List<ParametersModel> parameters;
    private List<DatasetFiltersDTO> filter;

    private boolean fetchCachedData = true;
    private boolean saveInCache = true;
    private String userLocale = "fr";

    public void setSeries(List<ChartSeriesRequest> series) {
        if(Objects.nonNull(series)) {
            this.series = series.stream().filter(s -> Objects.nonNull(s) && Objects.nonNull(s.getColumnName()) && !s.getColumnName().isEmpty()).collect(Collectors.toList());
        } else {
            this.series = new ArrayList<>();
        }
    }

    public void setDimensions(List<String> dimensions) {
        if(Objects.nonNull(dimensions)) {
            this.dimensions = dimensions.stream().filter(dim -> Objects.nonNull(dim) && !dim.isEmpty()).collect(Collectors.toList());
        } else {
            this.dimensions = new ArrayList<>();
        }
    }
}
