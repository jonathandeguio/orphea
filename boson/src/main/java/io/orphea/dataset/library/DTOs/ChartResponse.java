package io.orphea.dataset.library.DTOs;

import io.orphea.dataset.requests.ChartDataRequest;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {
    @Builder.Default
    public long rows = 0;

    @Builder.Default
    public boolean trimmedData = false;

    @Builder.Default
    public long totalRows = 0;

    @Builder.Default
    public boolean cachedData = false;

    public Object data;

    public Object stats;

    public ChartDataRequest request;
}