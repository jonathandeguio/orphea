package io.movetodata.snap.logging.library.models;

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
public class ApplicationLogModel {
    private String deploymentId;
    private String level;
    private Long timestamp;
    private String requestId;
    private String logger;
    private String message;
}
