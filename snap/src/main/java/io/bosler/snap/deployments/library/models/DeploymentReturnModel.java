package io.bosler.snap.deployments.library.models;

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
public class DeploymentReturnModel {
    private @Id
    UUID id;

    public UUID deploymentId;
    public String accessToken;
}