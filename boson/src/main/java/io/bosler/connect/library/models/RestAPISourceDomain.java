package io.bosler.connect.library.models;

import io.bosler.connect.library.enums.RestAPIAuthTypeEnum;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rest_api_source_domain")
public class RestAPISourceDomain {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String domain;
    private String protocol;
    @Enumerated(EnumType.STRING)
    private RestAPIAuthTypeEnum authType;
    @Column(columnDefinition = "TEXT")
    private String bearerToken;
    @Column(columnDefinition = "TEXT")
    private String apiKeyName;
    @Column(columnDefinition = "TEXT")
    private String apiKeyValue;
    private Integer port;
}
