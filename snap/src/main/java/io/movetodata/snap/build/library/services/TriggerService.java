package io.movetodata.snap.build.library.services;

import io.movetodata.snap.build.library.enums.BuildStatus;
import io.movetodata.snap.build.library.models.TriggerArtifactModel;
import io.movetodata.snap.build.library.models.TriggerManagerModel;
import io.movetodata.snap.build.library.repository.TriggerArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@RequiredArgsConstructor
public class TriggerService {

    @Autowired
    private BuildService buildService;
    @Autowired
    private GitService gitService;
    @Autowired
    private TriggerArtifactRepository triggerArtifactRepository;

    public void process(UUID lastBuildUser, TriggerManagerModel triggerManagerModel) throws Exception {
        Hibernate.initialize(triggerManagerModel);
        List<TriggerArtifactModel> modelsToDelete = triggerManagerModel.getTriggerArtifactModels();
        List<TriggerArtifactModel> toDelete = new ArrayList<>();
        for (TriggerArtifactModel modelToDelete : modelsToDelete) {
            if (modelToDelete.buildStatus.equals(BuildStatus.DELETED)) {
                toDelete.add(modelToDelete);
            }
        }
        triggerArtifactRepository.deleteAll(toDelete);
        triggerManagerModel.getTriggerArtifactModels().forEach(Hibernate::initialize);

        String repoPath = Files.createDirectories(
                Path.of(System.getenv("ARTIFACT_STORE") + "/cloned-repo/" + triggerManagerModel.getId())
        ).toString();

        TriggerArtifactModel artifactModel = buildService.initializeArtifactBuild(triggerManagerModel);

        Git git = gitService.getGit(triggerManagerModel.getRepoUrl(), repoPath);
        String imageTag = gitService.cloneRepo(
                lastBuildUser, git, artifactModel, triggerManagerModel
        );

        buildService.buildAndSaveDockerImage(
                lastBuildUser, git, artifactModel, repoPath, triggerManagerModel,
                imageTag
        );
    }
}
