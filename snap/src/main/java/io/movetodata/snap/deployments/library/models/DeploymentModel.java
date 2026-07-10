package io.movetodata.snap.deployments.library.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.movetodata.snap.deployments.library.Enums.DeploymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "deployments")
public class DeploymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String location;
    private String address;

    private String contactDetails;
    private String email;
    private String branch;

    // Deployment will be paused out of the Time below and Override will add the time to deploy within the timeWindow
    private Date pausedUntil;
    private Long timeWindowStart;
    private Long timeWindowEnd;
    private Long overRideTimeWindow;
    private Integer overRideHours;

    @Enumerated(EnumType.STRING)
    private DeploymentMethod deploymentMethod; // Automatic | Manual | Pause

    @OneToMany(mappedBy = "deploymentModel", cascade = CascadeType.ALL)
    @JsonManagedReference
    @OrderBy("createdAt DESC")
    private List<LicenseModel> licenseModel;

    @OneToMany(mappedBy = "deploymentModel", cascade = CascadeType.ALL)
    @JsonManagedReference
    @OrderBy("deployedAt DESC")
    private List<ConfigurationComponentsModel> configurationComponentsModel;

    // Boson Service Last Log processed
    private Long lastApplicationLogTimestamp;
    private Long lastAccessLogTimestamp;

    // Frontend Service Last Log processed;
    private Long frontendLastApplicationLogTimestamp;

    // Capture Service Last Log Processed
    private Long captureLastApplicationLogTimestamp;

    // SystemMetricTimeStamps
    private Long lastDiskMetricLogTimestamp;
    private Long lastMemoryMetricLogTimestamp;
    private Long lastSwapMetricLogTimestamp;
    private Long lastCpuMetricLogTimestamp;

    private Date createdAt;
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}