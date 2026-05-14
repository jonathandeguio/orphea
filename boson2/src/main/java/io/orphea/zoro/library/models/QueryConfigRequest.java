package io.orphea.zoro.library.models;

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
public class QueryConfigRequest {
    private boolean fetchCachedData;
    private UUID chartUUID;
    private UUID datasetId;
    private String branch;
    private String chartId;
    private Integer rowSelect;
    private List<MetricModel> metric;
    private List<String> dimension;
    private List<MetricModel> sortBy;
    private List<SqlFilterModel> filter;
    private TimeRequest time;
    private String userLocale = "en";
}
