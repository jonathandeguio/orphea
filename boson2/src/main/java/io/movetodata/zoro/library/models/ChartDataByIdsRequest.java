package io.movetodata.zoro.library.models;

import io.movetodata.kepler.library.models.DashboardFilterModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChartDataByIdsRequest {
    private List<UUID> chartIds;
    private Boolean FetchCachedData = true;
    private List<FiltersInChartData> filters;
}
