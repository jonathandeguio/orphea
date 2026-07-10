package io.movetodata.snap.deployments.library.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseRequestModel {
    private String client;
    private String product;
    private String baseUrl;
    private boolean displayBlockedFeatures;
    private int maximumUsers;
    private int maximumBuildsPerDay;
    private int maximumDatasets;
    private int maximumDashboards;
    private int maximumCharts;
    private int maximumRepositories;
    private long expiresOn;
}