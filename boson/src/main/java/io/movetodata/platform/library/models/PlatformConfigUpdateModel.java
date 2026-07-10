package io.movetodata.platform.library.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformConfigUpdateModel {
    String platformName;
    boolean download;
    Integer rowLimit = 250000;
    Long sizeLimit;
    boolean upload;
    String timezone;
    boolean cache;
    Long cacheExpiration;
    Integer datasetHistory = 2;
    boolean httpProxy;
    boolean artifactory;
    private String logo;
    private String artifactoryUrl;
    private String httpProxyUrl;
    private String licenseKey;
    private String customTheme;
    private String defaultBranch;
    private Boolean mfaEnabled;
    private Boolean mfaEnforced;
    private Boolean dataMartEnabled;
}
