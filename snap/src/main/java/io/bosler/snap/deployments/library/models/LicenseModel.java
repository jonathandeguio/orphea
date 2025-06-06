package io.bosler.snap.deployments.library.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.bosler.snap.deployments.library.Enums.ConfigurationState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "license")
public class LicenseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

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

    private Date createdAt;
    private Date updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    @Enumerated(EnumType.STRING)
    private ConfigurationState state;

    @ManyToOne
    @JoinColumn(name = "deployments_id")
    @JsonBackReference
    private DeploymentModel deploymentModel;

    @Column(columnDefinition = "TEXT")
    private String licenseKey;
}