package io.orphea.snap.deployments.library.repository;

import io.orphea.snap.deployments.library.models.DeploymentModel;
import io.orphea.snap.deployments.library.models.LicenseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<LicenseModel, UUID> {

    Optional<LicenseModel> findByDeploymentModel(DeploymentModel deploymentModel);
    List<LicenseModel> findByDeploymentModelId(UUID deploymentId);
}