package io.movetodata.snap.git.library.services;

import io.movetodata.snap.git.library.models.GitBranchModel;
import io.movetodata.snap.git.library.models.GitRepositoryModel;
import io.movetodata.snap.git.library.models.GitTagModel;
import io.movetodata.snap.git.library.repository.GitRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitRepositoryService {

    @Autowired
    private GitRepositoryRepository gitRepositoryRepository;

    @Autowired
    private GitHubService gitHubService;

    public List<GitRepositoryModel> syncRepositories(String organization) throws ParseException {
        List<GitRepositoryModel> repositories = gitHubService.fetchRepositoryDetailsFromOrganization(organization);
        List<GitRepositoryModel> syncedRepos = new ArrayList<>();

        for (GitRepositoryModel repo : repositories) {
            // Check if the repository already exists
            GitRepositoryModel existingRepo = gitRepositoryRepository.findByFullName(repo.getFullName());
            if (existingRepo != null) {
                repo.setId(existingRepo.getId());  // Set the existing ID to update the repository
            }
            repo = gitRepositoryRepository.save(repo);  // Save or update the repository

            // Sync branches
            List<GitBranchModel> branches = gitHubService.fetchBranchesFromRepo(repo);
            for (GitBranchModel branch : branches) {
                branch.setRepository(repo);
            }
            repo.setBranches(branches);
            gitRepositoryRepository.save(repo);  // Save branches

            // Sync tags (top 5 tags only)
            List<GitTagModel> tags = gitHubService.fetchTop5TagsFromRepo(repo.getFullName());
            for (GitTagModel tag : tags) {
                tag.setRepository(repo);
            }
            repo.setTags(tags);
            gitRepositoryRepository.save(repo);  // Save tags

            syncedRepos.add(repo);
        }

        return syncedRepos;
    }
}

