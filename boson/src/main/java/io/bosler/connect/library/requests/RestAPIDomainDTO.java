package io.bosler.connect.library.requests;

import io.bosler.connect.library.enums.RestAPIAuthTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.UUID;

@Getter
@Setter
public class RestAPIDomainDTO {
    // Rest API Connector Related
    private UUID id;
    private String domain;
    private String protocol;
    @Enumerated(EnumType.STRING)
    private RestAPIAuthTypeEnum authType;
    private String bearerToken;
    private String apiKeyName;
    private String apiKeyValue;
    private Integer port;
}


