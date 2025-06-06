package io.bosler.kepler.library.models;

import io.bosler.zoro.library.models.QueryConfigRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

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

    public QueryConfigRequest chartConfig;
    public ChartCustomizeRequest chartCustomize;
}