package io.movetodata.snap.build.library.repository;

import io.movetodata.snap.build.library.models.TriggerManagerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TriggerRepository extends JpaRepository<TriggerManagerModel, UUID> {

    Optional<TriggerManagerModel> findByName(String name);
    TriggerManagerModel getById(UUID Id);
    Boolean existsByName(String name);

    Optional<TriggerManagerModel> findByNameAndBranch(String name, String branch);
    Optional<TriggerManagerModel> findByRepoNameAndBranch(String name, String branch);

    Optional<TriggerManagerModel> findByIdOrderByTriggerArtifactModelsStartedAtDesc(UUID triggerId);

    List<TriggerManagerModel> findAllByOrderByNameAsc();

    @Query("SELECT tm.latestTag FROM TriggerManagerModel tm WHERE tm.name = :name AND tm.branch = :branch ORDER BY tm.buildAt DESC")
    String findTopByNameAndBranchOrderByLastBuildAtDesc(@Param("name") String name, @Param("branch") String branch);

    @Query("Select Distinct a.branch from TriggerManagerModel a")
    List<String> findAllBranch();

    @Query("Select Distinct a.harborProjectName from TriggerManagerModel a")
    List<String> findAllHarborProjectName();

    @Query("SELECT a FROM TriggerManagerModel a WHERE ((:harborProjectName) IS NULL OR a.harborProjectName IN (:harborProjectName)) AND ((:branch) IS NULL OR a.branch IN (:branch))")
    List<TriggerManagerModel> findByRegistryAndBranch(
            @Param("harborProjectName") List<String> harborProjectName,
            @Param("branch") List<String> branch
    );
}