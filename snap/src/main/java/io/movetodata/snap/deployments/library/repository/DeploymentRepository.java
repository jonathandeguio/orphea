package io.movetodata.snap.deployments.library.repository;

import io.movetodata.snap.deployments.library.models.DeploymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<DeploymentModel, UUID> {
}