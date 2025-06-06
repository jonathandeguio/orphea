package io.bosler.snap.platform.library.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@RequiredArgsConstructor
public class LicenseInfoModel {
      private String client;
      private String product;
      private String baseUrl;
      private Long maximumUsers;
      private Long maximumBuildsPerDay;
      private Long maximumDatasets;
      private Long maximumDashboards;
      private Long maximumCharts;
      private Long maximumRepositories;
      private Date expiresOn;
}
