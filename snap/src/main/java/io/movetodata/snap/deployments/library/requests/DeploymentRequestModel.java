package io.movetodata.snap.deployments.library.requests;

import io.movetodata.snap.deployments.library.Enums.DeploymentMethod;
import io.movetodata.snap.deployments.library.models.ConfigurationComponentsModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DeploymentRequestModel {
    private String name;
    private String location;
    private String address;

    private String contactDetails;
    private String email;

    @Enumerated(EnumType.STRING)
    private DeploymentMethod deploymentMethod; // Automatic | Manual
    private List<ConfigurationComponentsModel> configurationComponentsModel;

    // Deployment will be paused out of the Time below and Override will add the time to deploy within the timeWindow
    private Long timeWindowStart;
    private Long timeWindowEnd;
    private Long overRideTimeWindow;
    private Integer overRideHours;

    private Date pausedUntil;
    private String branch;
}
