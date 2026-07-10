package io.movetodata.connect.library.models;

import io.movetodata.connect.library.enums.RestAPIMethodEnum;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "webhook_call_data")
public class WebhookCallData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String apiTitle;
    private String url;
    @Column(columnDefinition = "TEXT")
    private String fullUrl;
    @Enumerated(EnumType.STRING)
    private RestAPIMethodEnum method;
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;
    @Column(columnDefinition = "TEXT")
    private String requestBody;
    @Column(columnDefinition = "TEXT")
    private String responseHeaders;
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String extraErrors;
    private Date executionStartedAt;
    private Date executionEndedAt;
    private UUID executedBy;
}
