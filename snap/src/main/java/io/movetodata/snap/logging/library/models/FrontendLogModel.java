package io.movetodata.snap.logging.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrontendLogModel {

    private UUID deploymentId;
    private Long date;
    private String name;
    private String message;
    private String stack;
    private String componentStack;
}
