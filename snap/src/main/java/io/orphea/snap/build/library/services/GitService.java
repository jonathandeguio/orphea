package io.orphea.snap.build.library.services;

import io.orphea.snap.build.library.enums.BuildStatus;
import io.orphea.snap.build.library.models.TriggerArtifactModel;
import io.orphea.snap.build.library.models.TriggerManagerModel;
import io.orphea.snap.build.library.repository.TriggerArtifactRepository;
import io.orphea.snap.build.library.repository.TriggerRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GitService {

    private final TriggerRepository triggerRepository;
    private final TriggerArtifactRepository triggerArtifactRepository;
    private final String token = System.getenv("GITHUB_PAT");

    @Autowired
    private Utils utils;

    public Git getGit(String repositoryUrl, String localRepositoryPath) throws GitAPIException, IOException {
        File repoPath = new File(localRepositoryPath);
        if (repoPath.exists()) {
            utils.deleteDirectory(repoPath.toPath());
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("GitHub token (GITHUB_PAT) is not set.");
        }
        return Git.cloneRepository()
                .setURI(repositoryUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .setDirectory(repoPath)
                .call();
    }

    public String cloneRepo(UUID lastBuildUser, Git git, TriggerArtifactModel artifactModel, TriggerManagerModel triggerManagerModel) throws Exception {
        System.out.println("[PREPARING] : Cloning repository: " + triggerManagerModel.getRepoName());
        artifactModel.setBuildStatus(BuildStatus.ACTIVE);
        triggerArtifactRepository.save(artifactModel);

        try {
            git.fetch()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                    .setRemote("origin")
                    .call();
            System.out.println("[PREPARING] : Fetch completed successfully.");
        } catch (Exception e) {
            utils.onError(lastBuildUser, artifactModel, e, "Failed to fetch from remote. Ensure credentials and repository URL are correct.");
            throw new Exception("Failed to fetch from remote.", e);
        }

        try {
            if (!checkoutBranchOrCommit(git, triggerManagerModel.getBranch())) {
                throw new RefNotFoundException("Branch or commit not found: " + triggerManagerModel.getBranch());
            }
            System.out.println("[PREPARING] : Checked out branch/commit: " + triggerManagerModel.getBranch());
        } catch (RefNotFoundException e) {
            utils.onError(lastBuildUser, artifactModel, e, "The specified branch or commit '" + triggerManagerModel.getBranch() + "' was not found.");
            throw new Exception("Branch or commit not found: " + triggerManagerModel.getBranch(), e);
        } catch (Exception e) {
            utils.onError(lastBuildUser, artifactModel, e, "Error checking out branch or commit '" + triggerManagerModel.getBranch() + "'.");
            throw new Exception("Error checking out branch or commit: " + triggerManagerModel.getBranch(), e);
        }

        try {
            String tag = generateLatestTag(triggerManagerModel.getRepoName(), triggerManagerModel.getBranch());
            return tag;
        } catch (Exception e) {
            utils.onError(lastBuildUser, artifactModel, e, "There was an error in tags. Check if tags are numeric and in the format of 0.0.0.");
            throw new Exception("Tag increment error.", e);
        }
    }

    private boolean checkoutBranchOrCommit(Git git, String branchOrCommit) throws Exception {
        ObjectId commitId = git.getRepository().resolve(branchOrCommit);
        if (commitId != null) {
            git.checkout()
                    .setName(commitId.getName())
                    .setStartPoint(commitId.getName())
                    .call();
            return true;
        }

        Ref ref = git.getRepository().findRef(branchOrCommit);
        if (ref == null) {
            git.checkout()
                    .setCreateBranch(true)
                    .setName(branchOrCommit)
                    .setStartPoint("origin/" + branchOrCommit)
                    .call();
        } else {
            git.checkout()
                    .setName(branchOrCommit)
                    .call();
        }
        return true;
    }

    private String generateLatestTag(String imageName, String branch) {
        String latestTag = triggerRepository.findTopByNameAndBranchOrderByLastBuildAtDesc(imageName + "-" + branch, branch);
        return utils.incrementTag(latestTag);
    }

    public String getLatestCommitId(Git git) throws IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId head = git.getRepository().resolve("HEAD");
            RevCommit commit = revWalk.parseCommit(head);
            return commit.getId().getName();
        }
    }
}
