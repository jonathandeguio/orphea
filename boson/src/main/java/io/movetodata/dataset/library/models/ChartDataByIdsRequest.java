package io.movetodata.dataset.library.models;

import io.movetodata.dataset.library.DTOs.DatasetFiltersDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChartDataByIdsRequest {
    private final Boolean saveInCache = true;
    private final Boolean FetchCachedData = true;
    private List<UUID> chartIds;
    private String userLocale = "fr";

    private List<DatasetFiltersDTO> filters;
}