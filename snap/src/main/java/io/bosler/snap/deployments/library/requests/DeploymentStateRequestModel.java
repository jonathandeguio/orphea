package io.bosler.snap.deployments.library.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeploymentStateRequestModel {
    String frontend;
    String boson;
    String parler;
    String julia;
    String callisto;
    String capture;
    String boslerDocs;
    String sparkHistoryServer;
    String branch;
}
