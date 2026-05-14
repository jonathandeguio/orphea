package io.orphea.snap.logging.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsModel {

    private String deploymentId;
    private Long time;
    private String metricName;
    private String total;  // Changed to String
    private String used;  // Changed to String
    private String free;  // Changed to String
}
