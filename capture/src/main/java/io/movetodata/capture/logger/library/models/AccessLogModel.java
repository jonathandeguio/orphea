package io.movetodata.capture.logger.library.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.movetodata.capture.logger.library.enums.BosonComponent;
import io.movetodata.capture.logger.library.enums.LogType;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessLogModel {
    private Long date;
    private String requestId;
    private String method;
    private String requestUri;
    private String httpVersion;
    private String statusCode;
    private String clientIp;
    private String responseSize;
    private String responseTime;
    private String referer;
    private String userAgent;
    private String userName;
}
