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
public class AccessLogModel {
    private String deploymentId;
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
