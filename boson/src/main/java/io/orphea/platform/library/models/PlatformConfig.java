package io.orphea.platform.library.models;


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
    boolean download;
    Integer rowLimit;
    Long sizeLimit;
    Integer sparkDataLimit = 500;
    boolean cache;
    Long cacheExpiration = 2592000L;
    boolean upload;
    String timezone = "Europe/Paris";
    Integer datasetHistory = 2;
    Integer logsValidDays = 30;
    boolean httpProxy;
    boolean artifactory;
    @Column(columnDefinition = "TEXT")
    private String logo;
    @Column(columnDefinition = "TEXT")
    private String artifactoryUrl;
    @Column(columnDefinition = "TEXT")
    private String httpProxyUrl;

    @Column(columnDefinition = "TEXT")
    private String licenseKey;

    @Column(columnDefinition = "TEXT")
    private String customTheme = "[]";

    private String defaultBranch;

    private Boolean mfaEnabled;
    private Boolean mfaEnforced;
    private Boolean dataMartEnabled;

    public void setLogsValidDays(Integer days) {
        if (days == null || days < 2) {
            throw new IllegalArgumentException("Days cannot be null or < 2");
        }
        this.logsValidDays = days;
    }

    public void setDatasetHistory(Integer validHistoricalData) {
        if (validHistoricalData == null || validHistoricalData < 2) {
            throw new IllegalArgumentException("Minimum 2 dataset history must be present.");
        }
        this.datasetHistory = validHistoricalData;
    }
}
