package io.bosler.snap.artifact.library.services;

import io.bosler.snap.build.library.enums.BuildStatus;
import io.bosler.snap.build.library.models.TriggerArtifactModel;
import io.bosler.snap.build.library.repository.TriggerArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TriggerArtifactService {

    @Autowired
    private TriggerArtifactRepository triggerArtifactRepository;

    @Transactional
    public void deleteOldArtifacts(String triggerManagerName, int keepArtifacts, String branch) {
        List<TriggerArtifactModel> artifacts = fetchAndSortArtifacts(triggerManagerName, branch);

        int numberOfArtifactsToDelete = artifacts.size() - keepArtifacts;
        if (numberOfArtifactsToDelete > 0) {
            List<TriggerArtifactModel> artifactsToDelete = artifacts.subList(0, numberOfArtifactsToDelete);
            deleteArtifacts(artifactsToDelete);
        }
    }

    private List<TriggerArtifactModel> fetchAndSortArtifacts(String triggerManagerName, String branch) {
        List<TriggerArtifactModel> artifacts = triggerArtifactRepository.findArtifactsByTriggerManagerName(triggerManagerName, branch);
        artifacts.sort(Comparator.comparing(TriggerArtifactModel::getStartedAt));
        return artifacts;
    }

    private void deleteArtifacts(List<TriggerArtifactModel> artifactsToDelete) {
        for (TriggerArtifactModel artifact : artifactsToDelete) {
            try {
                artifact.setBuildStatus(BuildStatus.DELETED);
                deleteArtifactDirectory(artifact.getId());
            } catch (IOException e) {
                // Log the exception
                System.err.println("Failed to delete artifact directory for ID: " + artifact.getId());
                e.printStackTrace();
            }
        }
        triggerArtifactRepository.deleteAll(artifactsToDelete);
    }

    private void deleteArtifactDirectory(UUID artifactId) throws IOException {
        Path artifactDirectory = Path.of(System.getenv("ARTIFACT_STORE"), "artifacts", artifactId.toString());
        if (artifactDirectory.toFile().exists()) {
            Files.walk(artifactDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            // Log failure to delete
                            System.err.println("Failed to delete " + file);
                        }
                    });
        }
    }
}
