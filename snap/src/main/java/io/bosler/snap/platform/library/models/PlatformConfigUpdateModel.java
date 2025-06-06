package io.bosler.snap.platform.library.models;


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
    boolean cache;
    Long cacheExpiration;
    Integer datasetHistory = 2;
    boolean httpProxy;
    boolean artifactory;
    private String logo;
    private String artifactoryUrl;
    private String httpProxyUrl;
    private String licenseKey;
    private Boolean mfaEnabled;
}
