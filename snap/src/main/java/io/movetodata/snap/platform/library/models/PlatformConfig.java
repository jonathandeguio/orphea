package io.movetodata.snap.platform.library.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "platform_config")
public class PlatformConfig {

    public @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @CreatedDate
    public Date createdAt;
    @LastModifiedDate
    public Date updatedAt;
    public UUID createdBy;
    public UUID updatedBy;
    public String platformName;
    String name;
    @Column(columnDefinition = "TEXT")
    private String logo;
    @Column(columnDefinition = "TEXT")
    private String artifactoryUrl;
    @Column(columnDefinition = "TEXT")
    private String httpProxyUrl;
    private Boolean mfaEnabled;
}
