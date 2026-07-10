package io.movetodata.snap.build.library.repository;

import io.movetodata.snap.build.library.enums.BuildStatus;
import io.movetodata.snap.build.library.models.TriggerArtifactModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface TriggerArtifactRepository extends JpaRepository<TriggerArtifactModel, UUID> {
    List<TriggerArtifactModel> findByTagAndBuildStatus(String tag, BuildStatus buildStatus);

    List<TriggerArtifactModel> findByIdOrderByStartedAt(UUID id);

    List<TriggerArtifactModel> findByTriggerManagerModelIdOrderByStartedAtDesc(UUID triggerManagerModelId);

    List<TriggerArtifactModel> findByTriggerManagerModelId(UUID triggerManagerId);

    @Query("SELECT t FROM TriggerArtifactModel t WHERE t.createdAt < :date ORDER BY t.createdAt DESC")
    List<TriggerArtifactModel> findOldArtifacts(Date date);


    @Query("SELECT t FROM TriggerArtifactModel t WHERE t.triggerManagerModel.name = :name AND t.branch = :branch ORDER BY t.startedAt ASC")
    List<TriggerArtifactModel> findArtifactsByTriggerManagerName(@Param("name") String name, @Param("branch") String branch);
}