package io.bosler.zoro.library.models;

import io.bosler.kepler.library.models.DashboardFilterModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class FiltersInChartData {
    DashboardFilterModel filter;
    String filterValue;
}
