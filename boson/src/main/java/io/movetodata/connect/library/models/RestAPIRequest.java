package io.movetodata.connect.library.models;

import io.movetodata.connect.library.enums.RestAPIMethodEnum;
import io.movetodata.connect.library.enums.RestAPITypeEnum;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rest_api_request")
public class RestAPIRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String apiTitle;
    private String path;
    private UUID domainId;
    private Integer requestOrder = 0;
    @Enumerated(EnumType.STRING)
    private RestAPITypeEnum bodyType;
    @Column(columnDefinition = "TEXT")
    private String queryParams;
    @Column(columnDefinition = "TEXT")
    private String headers;
    @Column(columnDefinition = "TEXT")
    private String formData;
    @Column(columnDefinition = "TEXT")
    private String rawBody;
    @Enumerated(EnumType.STRING)
    private RestAPIMethodEnum method;
}
