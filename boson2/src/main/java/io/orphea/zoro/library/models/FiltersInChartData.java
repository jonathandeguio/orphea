package io.orphea.zoro.library.models;

import io.orphea.kepler.library.models.DashboardFilterModel;
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
