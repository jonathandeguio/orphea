package io.bosler.connect.library.models;

import io.bosler.connect.library.enums.SourceAuthTypeEnum;
import io.bosler.connect.library.enums.SourceTypeEnum;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "database_source_config")
public class DatabaseSourceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private SourceTypeEnum dbmsType;
    @Enumerated(EnumType.STRING)
    private SourceAuthTypeEnum authType;

    private String username;
    private String password;

    // Snowflake Key pair auth
    @Column(columnDefinition = "TEXT")
    private String privateKey;
    private String privateKeyPassPhrase;

    // Snowflake specific
    private String userRole;
    private String warehouse;
    private String schema;
    private String database;

    private String server;
    private Integer port;

    // Limitation on database accessing
    private Long limitRows = -1L;
    private Long limitSize = -1L;
}
