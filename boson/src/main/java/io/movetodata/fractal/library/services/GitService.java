package io.movetodata.fractal.library.services;

import io.movetodata.fractal.library.Exceptions.GitCheckoutConflict;
import io.movetodata.fractal.library.Exceptions.GitInitializeException;
import io.movetodata.fractal.library.Exceptions.GitOutOfSyncException;
import io.movetodata.fractal.library.Exceptions.NoSuchBranchException;
import io.movetodata.fractal.library.models.*;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.platform.library.models.GitConfigModel;
import io.movetodata.platform.library.repository.GitConfigRepository;
import io.movetodata.sharedutils.Exceptions.BadRequestException;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class GitService {
    private final ResourceService resourceService;
    private final UserService userService;
    private final Lock lock = new ReentrantLock();

    @Autowired
    GitConfigRepository gitConfigRepository;

    @Autowired
    public GitService(ResourceService resourceService, UserService userService) {
        this.resourceService = resourceService;
        this.userService = userService;
    }

    public void gitCommit(Git git, UUID userId, String message) throws GitAPIException {
        User user = userService.getUserById(userId);
        GitCommit gitCommit = new GitCommit();
        gitCommit.setMessage(message);
        gitCommit.setEmail(user.getEmail());
        gitCommit.setUsername(user.getUsername());

        CommitCommand commitCommand = git.commit().setMessage(gitCommit.getMessage()).setCommitter(gitCommit.getUsername(), gitCommit.getEmail());
        commitCommand.call();
    }

    public void pull(Git git, UUID userId, String branch) throws GitAPIException {
        if (!((Status)this.trackingStatus(git, branch).get("gitStatus")).isClean()) {
            git.add().addFilepattern(".").call();
            this.gitCommit(git, userId, "Pull request commit " + new Date());
        }

        git.pull().setRemote("origin").setRemoteBranchName(branch).call();
    }

    /**
     * Saves a file in the repository (This is only in the cloned repository, not comitted and pushed to Julia)
     *
     * @param userId
     * @param repositoryId
     * @param userInput
     * @throws Exception
     */
    public void saveFile(UUID userId, UUID repositoryId, ArrayList<Map<String, Object>> userInput) throws Exception {
        Git git = getGitRepository(userId, repositoryId);

        for (Map<String, Object> contents : userInput) {
            String fileContent = (String) contents.get("fileContent");
            byte[] valueDecoded = Base64.decodeBase64(fileContent.getBytes());
            String folderPath = (String) contents.get("filePath");
            int index = folderPath.lastIndexOf('/');
            File file;
            if (index != -1) {
                folderPath = folderPath.substring(0, index);
                file = new File(git.getRepository().getDirectory().getParent(), folderPath);
                file.mkdirs();
            }
            file = new File(git.getRepository().getDirectory().getParent(), (String) contents.get("filePath"));
            FileWriter fileWriter = new FileWriter(file.getPath());
            fileWriter.write(new String(valueDecoded));
            fileWriter.close();
        }
    }

    /**
     * Returns the Git object, Safe (gitRef and object database checks has been applied)
     *
     * @param userId       userId used to prepare the repository path
     * @param repositoryId repositoryId to access
     * @return Git Object
     */
    public Git getGitRepository(UUID userId, UUID repositoryId) throws GitAPIException, IOException, InterruptedException {
        Boolean isLockAcquired = lock.tryLock(30, TimeUnit.SECONDS);
        if (!isLockAcquired) {
            // Handle the case when the lock couldn't be acquired (e.g., retry or throw an exception)
            throw new GitInitializeException("Could not acquire the lock within the specified time.");
        } else {
            try {
                // Making the method synchronized (solves the issue of locks)
                String repoPath = System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId + "/" + ".git";

                File file = new File(repoPath);
                if (!file.exists()) {
                    GitConfigModel gitConfigModel = gitConfigRepository.findByConfig("platform");
                    Git.cloneRepository().setURI("http://" + gitConfigModel.getHost() + ":" + gitConfigModel.getPort() + "/julia/" + repositoryId + "/.git").setDirectory(new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId)).call();
                }
                // 1. PREPARE FOR THE PATH
                Repository repository = new FileRepositoryBuilder().setGitDir(new File(repoPath)).readEnvironment().findGitDir().build();

                // 2. CHECK IF THE GIT OBJECT DATABASE EXIST ( responsible for all the objects, including commits, trees, blobs, and tags)
                if (!repository.getObjectDatabase().exists()) {
                    throw new GitInitializeException();
                }

                // 3. Create the git repository
                return new Git(repository);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Get the list of the branches for the repo
     *
     * @param git Git Object
     * @return
     * @throws GitAPIException
     */
    public static List<String> getBranchesList(Git git) throws GitAPIException {
        final List<Ref> branchRefs = git.branchList().call();
        return branchRefs.stream().map((Ref::getName)).collect(Collectors.toList());
    }

    /**
     * Check if a branch exists or not
     *
     * @param git
     * @param branch
     * @return boolean
     * @throws GitAPIException
     */
    public boolean hasBranch(Git git, String branch) throws GitAPIException {
        List<String> branches = getBranchesList(git);

        List<String> filteredBranches = branches.stream().filter(s -> s.substring(s.lastIndexOf('/') + 1).equals(branch)).collect(Collectors.toList());

        return !filteredBranches.isEmpty();
    }

    public List<GitDiffDTO> getCommitDetails(UUID userId, UUID repositoryId, String gitRef) throws IOException, GitAPIException, InterruptedException {
        Git git = getGitRepository(userId, repositoryId);

        Repository repository = git.getRepository();

        // Resolve ObjectId for the commit
        ObjectId commitObjectId = repository.resolve(gitRef);

        List<GitDiffDTO> diffDTOS = new ArrayList<>(3);
        // Get the commit object
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            // Check if the commit has a parent
            if (commit.getParentCount() > 0) {
                RevCommit parentId = commit.getParent(0);
                RevCommit parent = revWalk.parseCommit(parentId.getId());

                // Get the list of differences between the commit and its parent
                List<DiffEntry> diffs = git.diff().setOldTree(prepareTreeParser(repository, parent.getId())).setNewTree(prepareTreeParser(repository, commit.getId())).call();

                diffDTOS = new ArrayList<>(diffs.size());
                // Print or use the differences as needed
                for (DiffEntry entry : diffs) {
                    String diffContent;
                    // Get the content changes
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        try (DiffFormatter diffFormatter = new DiffFormatter(out)) {
                            diffFormatter.setRepository(repository);
                            diffFormatter.format(entry);
                        }
                        diffContent = out.toString();
                    }
                    diffDTOS.add(GitDiffDTO.builder().oldPath(entry.getOldPath()).newPath(entry.getNewPath()).changeType(entry.getChangeType()).content(diffContent).build());
                }
            } else {
                // Handle the case when there is no parent (initial commit)
                // Treat all files as newly created
                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    String newPath = treeWalk.getPathString();
                    diffDTOS.add(GitDiffDTO.builder().oldPath("/dev/null")  // Indicates no old path (newly created)
                            .newPath(newPath).changeType(DiffEntry.ChangeType.ADD).content(new String(repository.open(treeWalk.getObjectId(0)).getBytes()))  // No content change for newly created files
                            .build());
                }

                treeWalk.close();
            }
        }
        return diffDTOS;
    }

    public List<GitLog> getFileHistory(UUID userId, UUID repositoryId, String filePath) throws IOException, GitAPIException, InterruptedException {
        List<GitLog> fileHistory = new ArrayList<>(10);
        Git git = this.getGitRepository(userId, repositoryId);
        Repository repository = git.getRepository();

        try (RevWalk revWalk = new RevWalk(repository)) {
            revWalk.sort(RevSort.COMMIT_TIME_DESC);

            git.log().addPath(filePath).call().forEach(commit -> {
                fileHistory.add(GitLog.builder().id(commit.toObjectId().getName()).username(commit.getCommitterIdent().getName()).email(commit.getCommitterIdent().getEmailAddress()).body(commit.toString()).commitTime(commit.getCommitTime()).message(commit.getFullMessage()).build());
            });
        }

        return fileHistory;
    }

    public FileDiffDTO getCommitDetails(UUID userId, UUID repositoryId, String gitRef, String filePath) throws IOException, GitAPIException, InterruptedException {
        Git git = getGitRepository(userId, repositoryId);

        Repository repository = git.getRepository();

        // Resolve ObjectId for the commit

        // Get the commit object
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId commitObjectId = repository.resolve(gitRef);
            RevCommit commit = revWalk.parseCommit(commitObjectId);

            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());

            String originalContent = "";
            String updatedContent = "";
            ObjectId objectId = repository.resolve(gitRef + ":" + filePath);

            if (objectId != null) {
                byte[] content = repository.open(objectId).getBytes();
                updatedContent = Base64.encodeBase64String(content);
            }

            ObjectId oldObjectId = repository.resolve(parent.getName() + ":" + filePath);
            if (oldObjectId != null) {
                byte[] oldContent = repository.open(oldObjectId).getBytes();
                originalContent = Base64.encodeBase64String(oldContent);

            }

            FileDiffDTO fileDiffDTO = FileDiffDTO.builder().path(filePath).updatedContent(updatedContent).oldContent(originalContent).build();
            return fileDiffDTO;
        }
    }

    public String getFileContent(UUID userId, UUID repositoryId, String gitRef, String filePath, String workingTree) throws IOException, GitAPIException, InterruptedException {
        Repository repository = this.getGitRepository(userId, repositoryId).getRepository();

        if ("workingTree".equalsIgnoreCase(workingTree)) {
            File file = new File(repository.getWorkTree(), filePath);
            return Base64.encodeBase64String(readFile(file));
        } else {
            ObjectId objectId = repository.resolve(gitRef + ":" + filePath);

            byte[] content = repository.open(objectId).getBytes();
            return Base64.encodeBase64String(content);
        }
    }

    public List<BlameResultDTO> gitBlame(User user, UUID repositoryId, String branch, String fileName) {
        List<BlameResultDTO> blameRes = new ArrayList<>();
        try {
            Git git = getGitRepository(user.getId(), repositoryId);

            ObjectId head = git.getRepository().resolve("HEAD");
            BlameResult blameResult = git.blame().setFilePath(fileName).setStartCommit(head).setFollowFileRenames(true).call();

            blameResult.computeAll();
            // Get information about each line
            for (int i = 0; i < blameResult.getResultContents().size(); i++) {
                RevCommit commit = blameResult.getSourceCommit(blameResult.getSourceLine(i));
                if (commit == null) {
                    long lastModified = git.log().addPath(fileName).setMaxCount(1).call().iterator().next().getCommitTime() * 1000L;
                    Date lastModifiedDate = new Date(lastModified);
                    blameRes.add(BlameResultDTO.builder().committer(user.getName()).author(user.getName()).line(i).commitDate(lastModifiedDate).message("Uncommitted Changes").id("0").build());
                } else {
                    blameRes.add(BlameResultDTO.builder().committer(commit.getCommitterIdent().getName()).author(commit.getAuthorIdent().getName()).line(i).commitDate(commit.getCommitterIdent().getWhen()).message(commit.getFullMessage()).id(commit.getName()).build());
                }
            }
        } catch (Exception ignored) {
        }

        return blameRes;
    }

    public Map<String, Object> trackingStatus(Git git, String branch) {
        try {
            git.fetch().setCheckFetchedObjects(true).call();
            Ref branchRef = git.getRepository().findRef(branch);

            if (branchRef == null) {
                throw new ResourceNotFoundException("Branch not found");
            }

            BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(git.getRepository(), branch);
            Status statusCommand = git.status().call();

            Map<String, Object> trackRecord = new HashMap<>();
            if (Objects.nonNull(trackingStatus)) {
                trackRecord.put("ahead", trackingStatus.getAheadCount());
                trackRecord.put("behind", trackingStatus.getBehindCount());
                trackRecord.put("inSync", trackingStatus.getBehindCount() == 0);
                trackRecord.put("isRemote", true);
            } else {
                trackRecord.put("ahead", 0);
                trackRecord.put("behind", 0);
                trackRecord.put("isRemote", false);
                trackRecord.put("inSync", true);
            }
            trackRecord.put("gitStatus", statusCommand);

            return trackRecord;
        } catch (Exception e) {
            throw new GitInitializeException();
        }
    }
    @Transactional
    public void pushAll(UUID userId, UUID repositoryId) throws GitAPIException, IOException, InterruptedException {
        Git git = getGitRepository(userId, repositoryId);
        String branch = git.getRepository().getBranch();

        BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(git.getRepository(), branch);

        if(Objects.nonNull(trackingStatus) && trackingStatus.getBehindCount() > 0) {
            throw new GitOutOfSyncException("Failed to push some refs to remote, Pull first, then push again.");
        } else {
            PushCommand pushCommand = git.push();
            pushCommand.call();
        }
    }

    @Transactional
    public Map<String, Object> commitAllActiveChanges(UUID userId, UUID repositoryId, String branch1, String message) {
        try {
            ResourceModel resourceModel = resourceService.findById(repositoryId).orElseThrow(NoSuchBranchException::new);
            resourceModel.setUpdatedAt(new Date());
            resourceModel.setUpdatedBy(userId);
            resourceService.save(resourceModel);

            Git git = getGitRepository(userId, repositoryId);
            String branch = git.getRepository().getBranch();

            git.add().addFilepattern(".").call();
            gitCommit(git, userId, message);

            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);

            ObjectId branchId = git.getRepository().resolve(branch);

            Map<String, Object> saveResponse = new HashMap<>();
            saveResponse.put("message", "Pushed successfully");
            saveResponse.put("lastCommitId", lastCommitId);
            saveResponse.put("branchId", branchId);
            return saveResponse;
        } catch (Exception e) {
            throw new GitInitializeException();
        }
    }

    @Transactional
    public ResourceModel createRepository(UUID userId, ResourceModel newRepository, ResourceSubtype templateType) throws Exception {

        GitConfigModel gitConfigModel = gitConfigRepository.findByConfig("platform");

        ResourceModel createdRepository = resourceService.newResource(newRepository.getName(), newRepository.getDescription(), ResourceType.REPOSITORY, templateType, userId, newRepository.getParent());

        createJuliaRepository(createdRepository.getId()); // TODO: put validations

        File targetDirectory = new File(MessageFormat.format("{0}/{1}/{2}", System.getenv("GIT_CLONED_PATH"), userId, createdRepository.getId()));
        String juliaURI = MessageFormat.format("http://{0}:{1}/julia/{2}/.git", gitConfigModel.getHost(), gitConfigModel.getPort().toString(), createdRepository.getId());

        Git gitJulia = Git.cloneRepository().setURI(juliaURI).setDirectory(targetDirectory).call();

        String fractalTemplatePath = System.getenv("FRACTAL_TEMPLATES_PATH");
        File fractalTemplates = new File(fractalTemplatePath);

        if (!fractalTemplates.exists()) {
            throw new RuntimeException("Fractal Templates not found.");
        }

        File sourceDirectory = new File(fractalTemplatePath + "/" + templateType);

        FileUtils.copyDirectory(sourceDirectory, targetDirectory);

        gitJulia.add().addFilepattern(".").call();

        gitCommit(gitJulia, userId, "Initial Commit");

        StoredConfig config = gitJulia.getRepository().getConfig();
        config.setString("branch", "master", "remote", "origin");
        config.setString("branch", "master", "merge", "refs/heads/master");
        // Set `pull.rebase` to `false`
        config.setBoolean("pull", null, "rebase", false);

        config.save();

        PushCommand pushCommand = gitJulia.push();
        pushCommand.call();


        return createdRepository;
    }

    /**
     * Creates a Julia repository
     *
     * @param repositoryId
     * @return
     * @throws Exception
     */
    public String createJuliaRepository(UUID repositoryId) {

        GitConfigModel gitConfigModel = gitConfigRepository.findByConfig("platform");

        String juliaHost = gitConfigModel.getHost();
        String juliaApiPort = gitConfigModel.getApiPort().toString();
        String url = String.format("http://%s:%s/julia/repository/%s/create", juliaHost, juliaApiPort, repositoryId);

        try {

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet getConfigStatus = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(getConfigStatus);
            HttpEntity responseEntity = response.getEntity();

            return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BadRequestException("Failed to create Julia repository, Please check your configurations.");
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(objectId);
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }
    }

    public static void checkout(@NotNull Git git, @NotNull final String gitRef, Boolean force) throws GitAPIException, IOException {
        Repository repository = git.getRepository();
        Status status = git.status().call();
        Set<String> unsavedChanges = status.getUncommittedChanges();
        if(!force && !unsavedChanges.isEmpty()) {
            throw new GitCheckoutConflict(String.join(", ", new ArrayList<>(unsavedChanges)));
        }
        // Check if the branch exists locally
        if (repository.findRef(gitRef) != null) {
            git.checkout().setForced(force).setName(gitRef).call();
        } else {
            List<String> remoteBranches = getRemoteBranches(git);
            if(remoteBranches.stream().anyMatch(ref -> ref.equals(gitRef))) {
                git.checkout()
                        .setForced(force)
                        .setCreateBranch(true)
                        .setName(gitRef)
                        .setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();

                StoredConfig config = git.getRepository().getConfig();
                config.setString("branch", gitRef, "remote", "origin    ");
                config.setString("branch", gitRef, "merge", "refs/heads/" + gitRef);
                config.save();
            } else {
                throw new NoSuchBranchException("No Such Branch");
            }
        }
    }

    public static String getActiveBranch(Git git) throws IOException {
        return git.getRepository().getBranch();
    }

    public static String getLastCommitId(Git git) throws IOException, GitAPIException {
        Iterable<RevCommit> commits = git.log().add(git.getRepository().resolve(getActiveBranch(git))).setMaxCount(1).call();

        for (RevCommit commit : commits) {
            return commit.getName();  // This is the commit hash
        }
        return null;
    }

    public static List<String> getLocalBranches(Git git) throws GitAPIException {
        List<Ref> localBranches = git.branchList().call();

        return localBranches.stream()
                .map(ref -> ref.getName().replace("refs/heads/", ""))  // Strip the prefix
                .collect(Collectors.toList());
    }
    public static List<String> getRemoteBranches(Git git) throws GitAPIException {
        Collection<Ref> remoteBranches = git.lsRemote()
                .setRemote("origin")
                .setHeads(true)
                .call();

        return remoteBranches.stream()
                .map(ref -> ref.getName().replace("refs/heads/", ""))  // Strip the prefix
                .collect(Collectors.toList());
    }
    public static List<String> getAllBranches(Git git) throws GitAPIException {
        List<String> allBranches = new ArrayList<>();
        allBranches.addAll(getLocalBranches(git));
        allBranches.addAll(getRemoteBranches(git));
        return new ArrayList<>(new HashSet<>(allBranches));
    }
    public static byte[] readFile(File file) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
}