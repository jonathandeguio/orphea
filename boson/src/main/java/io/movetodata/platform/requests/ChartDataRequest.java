package io.movetodata.platform.requests;

import io.movetodata.kepler.library.models.DatasetFilterModel;
import io.movetodata.dataset.requests.ChartSeriesRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartDataRequest {
    private boolean fetchCachedData = true;
    private UUID chartUUID;
    private UUID datasetId;
    private String branch;
    private String chartType;
    private Integer rowLimit;

    private String xAxis;
    private String xAxisSort;
    private String xAxisTimeGrain;

    private List<ChartSeriesRequest> series;
    private List<DatasetFilterModel> filter; // TODO

    private String userLocale;
}
