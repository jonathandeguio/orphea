package io.movetodata.snap.deployments.library.repository;

import io.movetodata.snap.build.library.models.TriggerArtifactModel;
import io.movetodata.snap.deployments.library.Enums.ConfigurationState;
import io.movetodata.snap.deployments.library.models.ConfigurationComponentsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConfigurationComponentsRepository extends JpaRepository<ConfigurationComponentsModel, UUID> {

    List<ConfigurationComponentsModel> findByStateAndFrontendAndBosonAndParlerAndJuliaAndCallistoAndCaptureAndMoveToDataDocsAndSparkHistoryServerAndBranch(
            ConfigurationState state,
            String frontend,
            String boson,
            String parler,
            String julia,
            String callisto,
            String capture,
            String movetodataDocs,
            String sparkHistoryServer,
            String branch
    );

    // Correct method name to handle a list of states
    List<ConfigurationComponentsModel> findAllByStateInAndBranch(List<ConfigurationState> states, String branch);

    List<ConfigurationComponentsModel> findByGlobalVersion(String globalVersion);
}
