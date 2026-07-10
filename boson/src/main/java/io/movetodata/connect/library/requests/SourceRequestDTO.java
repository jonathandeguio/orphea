package io.movetodata.connect.library.requests;

import io.movetodata.connect.library.enums.SourceAuthTypeEnum;
import io.movetodata.connect.library.enums.SourceTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SourceRequestDTO {
    // Rest API Connector Related
    List<RestAPIDomainDTO> domains;

    private UUID id;
    private String name;
    private UUID sourceConfig;
    private String description;
    private UUID parent;
    private String type;
    private boolean directLoad = false;
    private List<UUID> agentId;
    private String path;
    private SourceTypeEnum dbmsType;
    private String username;
    private String password;
    private String database;
    private String server;
    private Integer port;
    private String warehouse;
    private SourceAuthTypeEnum authType;
    private String schema;
    private String userRole;
    private String privateKey;
    private String privateKeyPassPhrase;


    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String url;
}


