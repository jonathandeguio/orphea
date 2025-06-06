package io.bosler.platform.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartSeriesRequest {
    private String seriesName;
    private String columnName;
    private String aggregate;
    private ArrayList<String> groupBy;
    private String sort;
    private String seriesType;
    private String seriesIndex;
    private Boolean reversed;
}
