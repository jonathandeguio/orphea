package io.orphea.logger.library.models;

import io.orphea.logger.library.enums.BosonComponent;
import io.orphea.logger.library.enums.LogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogModel {
    private boolean process = false;
    private String metricType;
    private String payload;
    private Date timestamp;
    private BosonComponent component;
    private UUID traceId;
    private String message;
    private String uri;
    private String eventType;
    @Enumerated(EnumType.STRING)
    private LogType logType;
    private String statusCode;
    private String stacktrace;;
}
