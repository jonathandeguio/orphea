package io.movetodata.platform.library.models;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "datamart_config_model")
public class DataMartModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;

    private boolean isEnabled;
    @ElementCollection
    private List<UUID> projects;

    private String server;
    private Integer port;
    private String database;
    private String username;
    private String password;

    private Long limitRows = -1L;
    private Long limitSize = -1L;

    private UUID updatedBy;
    private UUID createdBy;
    private Date updatedAt;
    private Date createdAt;
}
