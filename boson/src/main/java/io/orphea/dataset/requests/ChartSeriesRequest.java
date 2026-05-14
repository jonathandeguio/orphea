package io.orphea.dataset.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartSeriesRequest {
    private UUID id;
    private UUID seriesId;
    private String seriesName;
    private String columnName;
    private String aggregate;
    private ArrayList<String> groupBy = new ArrayList<>();
    private String sort;
    private String seriesType;
    private String seriesIndex;
    private Boolean reversed;
}
