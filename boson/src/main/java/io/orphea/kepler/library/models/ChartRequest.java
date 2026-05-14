package io.orphea.kepler.library.models;

import io.orphea.dataset.requests.ChartDataRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartRequest {
    public String name;
    public String description;
    public UUID parent;
    public UUID datasetId;
    public String branch;
    public UUID transactionId;

    public String userLocale = "fr";

    public ChartDataRequest chartConfig;
    public Map<String, Object> chartCustomize;
}