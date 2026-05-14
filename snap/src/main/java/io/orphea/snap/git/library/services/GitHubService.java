package io.orphea.snap.git.library.services;

import io.orphea.snap.git.library.models.GitBranchModel;
import io.orphea.snap.git.library.models.GitRepositoryModel;
import io.orphea.snap.git.library.models.GitShaModel;
import io.orphea.snap.git.library.models.GitTagModel;
import io.orphea.snap.git.library.repository.GitBranchRepository;
import io.orphea.snap.git.library.repository.GitRepositoryRepository;
import io.orphea.snap.git.library.repository.GitShaRepository;
import io.orphea.snap.git.library.repository.GitTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final String gitHubToken = System.getenv("GITHUB_PAT");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final GitRepositoryRepository gitRepositoryRepository;
    private RestTemplate restTemplate;
    private final GitBranchRepository branchRepository;
    private final GitShaRepository shaRepository;
    private final GitTagRepository tagRepository;

    @PostConstruct
    private void init() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Accept", "application/vnd.github+json");
            request.getHeaders().add("Authorization", "Bearer " + gitHubToken);
            request.getHeaders().add("X-GitHub-Api-Version", "2022-11-28");
            return execution.execute(request, body);
        });
    }

    public List<GitRepositoryModel> fetchRepositoryDetailsFromOrganization(String organization) {
        String url = "https://api.github.com/orgs/" + organization + "/repos";
        Object[] repositories = restTemplate.getForObject(url, Object[].class);

        List<GitRepositoryModel> gitRepositoryResponse = new ArrayList<>();
        if (repositories != null) {
            for (Object repositoryObj : repositories) {
                gitRepositoryResponse.add(parseRepository((Map<String, Object>) repositoryObj));
            }
        }
        return gitRepositoryResponse;
    }

    private GitRepositoryModel parseRepository(Map<String, Object> repository) {
        GitRepositoryModel repositoryResponse = new GitRepositoryModel();
        repositoryResponse.setName((String) repository.get("name"));
        repositoryResponse.setFullName((String) repository.get("full_name"));
        repositoryResponse.setIsPrivate((Boolean) repository.get("private"));
        repositoryResponse.setDescription((String) repository.get("description"));
        repositoryResponse.setCloneUrl((String) repository.get("clone_url"));
        repositoryResponse.setTagsUrl((String) repository.get("tags_url"));
        repositoryResponse.setCommitsUrl((String) repository.get("commits_url"));
        repositoryResponse.setRepoSize((Integer) repository.get("size"));
        repositoryResponse.setRepositoryLanguage((String) repository.get("language"));
        repositoryResponse.setDefaultBranch((String) repository.get("default_branch"));

        try {
            repositoryResponse.setCreatedAt(dateFormat.parse((String) repository.get("created_at")));
            repositoryResponse.setUpdatedAt(dateFormat.parse((String) repository.get("updated_at")));
            repositoryResponse.setPushedAt(dateFormat.parse((String) repository.get("pushed_at")));
        } catch (ParseException e) {
            System.err.println("Error parsing dates: " + e.getMessage());
        }
        return repositoryResponse;
    }

    public List<GitBranchModel> fetchBranchesFromRepo(GitRepositoryModel repo) {
        String branchesUrl = "https://api.github.com/repos/" + repo.getFullName() + "/branches";
        Object[] branches = restTemplate.getForObject(branchesUrl, Object[].class);

        List<GitBranchModel> branchModels = new ArrayList<>();
        if (branches != null) {
            for (Object branch : branches) {
                GitBranchModel branchModel = createOrUpdateBranchModel(repo, (Map<String, Object>) branch);
                branchRepository.save(branchModel);

                List<GitShaModel> shaModels = getCommitsByBranch(repo.getFullName(), branchModel.getBranchName());
                saveShaModels(branchModel, shaModels);

                branchModel.setCommits(shaModels);
                branchRepository.save(branchModel);
                branchModels.add(branchModel);
            }
        }
        return branchModels;
    }

    private GitBranchModel createOrUpdateBranchModel(GitRepositoryModel repo, Map<String, Object> branchResp) {
        String branchName = (String) branchResp.get("name");
        GitBranchModel branchModel = branchRepository.findByBranchNameAndRepository(branchName, repo);

        if (branchModel == null) {
            branchModel = new GitBranchModel();
        }

        branchModel.setBranchName(branchName);
        branchModel.setIsDefault(repo.getDefaultBranch().equals(branchName));
        branchModel.setRepository(repo);
        return branchModel;
    }

    public List<GitShaModel> getCommitsByBranch(String repoFullName, String branchName) {
        String commitsUrl = "https://api.github.com/repos/" + repoFullName + "/commits?sha=" + branchName + "&per_page=5";

        try {
            List<Map<String, Object>> commitList = restTemplate.getForObject(commitsUrl, List.class);
            return parseCommitList(commitList);
        } catch (HttpClientErrorException.NotFound e) {
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("An error occurred while fetching commit details.");
            e.printStackTrace();
            throw new RuntimeException("Error fetching commit details.");
        }
    }

    private List<GitShaModel> parseCommitList(List<Map<String, Object>> commitList) throws ParseException {
        List<GitShaModel> shaModels = new ArrayList<>();
        if (commitList != null && !commitList.isEmpty()) {
            for (Map<String, Object> commitResp : commitList) {
                String sha = (String) commitResp.get("sha");
                String commitUrl = (String) commitResp.get("html_url");
                GitShaModel shaModel = createGitShaModel(commitResp, sha, commitUrl);
                shaModels.add(shaModel);
            }
        }
        return shaModels;
    }

    private GitShaModel createGitShaModel(Map<String, Object> commitResp, String sha, String commitUrl) throws ParseException {
        GitShaModel existingSha = shaRepository.findBySha(sha);
        if (existingSha == null) {
            GitShaModel shaModel = new GitShaModel();
            shaModel.setSha(sha);
            shaModel.setUrl(commitUrl);

            Map<String, Object> commitDetails = (Map<String, Object>) commitResp.get("commit");
            Map<String, Object> authorDetails = (Map<String, Object>) commitDetails.get("author");
            shaModel.setCreatedBy((String) authorDetails.get("name"));
            shaModel.setMessage((String) commitDetails.get("message"));
            shaModel.setCreatedAt(dateFormat.parse((String) authorDetails.get("date")));

            return shaModel;
        } else {
            return existingSha;
        }
    }

    private void saveShaModels(GitBranchModel branchModel, List<GitShaModel> shaModels) {
        List<GitShaModel> existingShaModels = branchModel.getCommits();

        for (GitShaModel shaModel : shaModels) {
            if (existingShaModels == null || existingShaModels.stream().noneMatch(existingSha -> existingSha.getSha().equals(shaModel.getSha()))) {
                shaModel.setBranch(branchModel);
                shaRepository.save(shaModel);
            }
        }

        // Fetch updated list after saving new SHAs
        List<GitShaModel> updatedShaModels = shaRepository.findByBranch(branchModel);

        // Delete oldest SHAs if there are more than 5
        if (updatedShaModels.size() > 5) {
            updatedShaModels.sort(Comparator.comparing(GitShaModel::getCreatedAt));
            for (int i = 0; i < updatedShaModels.size() - 5; i++) {
                shaRepository.delete(updatedShaModels.get(i));
            }
        }
    }

    public List<GitTagModel> fetchTop5TagsFromRepo(String repoFullName) throws ParseException {
        String tagsUrl = "https://api.github.com/repos/" + repoFullName + "/tags";
        Object[] tags = restTemplate.getForObject(tagsUrl, Object[].class);

        List<GitTagModel> tagModels = new ArrayList<>();
        GitRepositoryModel repository = gitRepositoryRepository.findByFullName(repoFullName);

        if (tags != null) {
            int count = 0;  // Limit the number of processed tags to 5
            for (Object tagObj : tags) {
                if (count >= 5) break;

                GitTagModel tagModel = parseTagModel(repoFullName, (Map<String, Object>) tagObj);
                tagModel.setRepository(repository);
                tagModels.add(tagModel);
                count++;
            }

            // Save tag models
            for (GitTagModel tagModel : tagModels) {
                tagRepository.save(tagModel);
            }

            // Fetch updated list after saving new tags
            List<GitTagModel> updatedTagModels = tagRepository.findByRepository(repository);

            if (updatedTagModels.size() > 5) {
                updatedTagModels.sort(Comparator.comparing(GitTagModel::getCreatedAt));

                // Delete the oldest records
                for (int i = 0; i < updatedTagModels.size() - 5; i++) {
                    tagRepository.delete(updatedTagModels.get(i));
                }
            }
        }

        return tagModels;
    }

    private GitTagModel parseTagModel(String repoFullName, Map<String, Object> tag) throws ParseException {
        GitTagModel tagModel = new GitTagModel();
        tagModel.setTagName((String) tag.get("name"));

        String commitSha = (String) ((Map<String, Object>) tag.get("commit")).get("sha");
        tagModel.setCommitSha(commitSha);
        tagModel.setRepositoryUrl((String) tag.get("zipball_url"));

        String commitUrl = "https://api.github.com/repos/" + repoFullName + "/commits/" + commitSha;
        Map<String, Object> commitDetails = restTemplate.getForObject(commitUrl, Map.class);

        if (commitDetails != null) {
            Map<String, Object> commitData = (Map<String, Object>) commitDetails.get("commit");
            Map<String, Object> authorData = (Map<String, Object>) commitData.get("author");
            tagModel.setCreatedBy((String) authorData.get("name"));
            tagModel.setCreatedAt(dateFormat.parse((String) authorData.get("date")));
        }

        return tagModel;
    }
}

