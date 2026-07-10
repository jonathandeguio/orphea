package io.movetodata.capture.logger.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing Boson metrics data.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsModel {
    private boolean process = false;
    private Long time;
    private String metricName;
    private Object total;
    private Object free;
    private Object used;
}
