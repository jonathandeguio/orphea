package io.movetodata.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeRequest {
    private String timeColumn;
    private String timeGrain;
    private String timeRange;
}
