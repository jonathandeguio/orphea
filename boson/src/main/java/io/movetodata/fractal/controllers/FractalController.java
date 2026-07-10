package io.movetodata.fractal.controllers;

import io.movetodata.fractal.library.Exceptions.NoSuchBranchException;
import io.movetodata.fractal.library.models.BlameResultDTO;
import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.models.User;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.AuthUser;
import io.movetodata.platform.library.models.GitConfigModel;
import io.movetodata.platform.library.repository.GitConfigRepository;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.Exceptions.BadRequestException;
import io.movetodata.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileDeleteStrategy;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/fractal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fractal", description = "Code Repository management service endpoints")
public class FractalController {
    private final UserService userService;
    private final AuthzService authzService;
    private final GitService gitService;
    private final ResourceService resourceService;
    private final GitConfigRepository gitConfigRepository;
    private final PlatformConfigService platformConfigService;

    @Operation(summary = "This can be used to create repository.")
    @PostMapping("/{templateType}/createRepository")
    public ResponseEntity<?> newRepository(@AuthenticationPrincipal AuthUser authUser,
                                           @RequestBody ResourceModel newRepository,
                                           @PathVariable("templateType") ResourceSubtype templateType) throws Exception {
        if(platformConfigService.isResourceCreationLimitReached(ResourceType.REPOSITORY)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorDTO(HttpStatus.TOO_MANY_REQUESTS.value(), "Maximum No. of Repository Limit Reached.","Please Contact Platform Admin."));
        }

        ResourceModel createdRepository = gitService.createRepository(authUser.getId(), newRepository, templateType);
        return new ResponseEntity<>(createdRepository, HttpStatus.OK);
    }

    
    @Operation(summary = "Auto save by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/autoSave")
    ResponseEntity<Object> autoSave(Principal principal,
                                    @PathVariable("repositoryId") UUID repositoryId,
                                    @PathVariable("branch") String branch,
                                    @RequestBody ArrayList<Map<String, Object>> userInput) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        resourceService.findById(repositoryId).orElseThrow(NoSuchElementException::new);

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException("Access Denied to " + repositoryId);
        }

        gitService.saveFile(userId, repositoryId, userInput);
        return ResponseEntity.ok().body("Save successful");
    }


    // TODO, FIXME
    
    @Operation(summary = "Get fresh clone by repositoryId and branch.")
    @GetMapping(value = "/{repositoryId}/{branch}/cloneAfresh")
    ResponseEntity<Object> cloneAfresh(Principal principal,
                                       @PathVariable("repositoryId") String repositoryId,
                                       @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).getId();

        GitConfigModel gitConfigModel = gitConfigRepository.findByConfig("platform");

//      TODO : unable to delete repository's/.git/objects folder which is causing error

        File file = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId);
        try {
            if (file.exists()) {
                FileDeleteStrategy.FORCE.delete(file);
            }
            Git.cloneRepository()
                    .setURI("http://" + gitConfigModel.getHost() + ":" + gitConfigModel.getHost() + "/julia/" + repositoryId + "/.git")
                    .setDirectory(new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId))
                    .call().getRepository().close();

        } catch (Exception e) {

        }

        return ResponseEntity.ok().body("Repository with repoId " + repositoryId + " cloned successfully");
    }

    
    @Operation(summary = "Checkout by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/checkout")
    ResponseEntity<Object> checkout(Principal principal,
                                    @PathVariable("repositoryId") UUID repositoryId,
                                    @PathVariable("branch") String branch) throws GitAPIException, IOException, InterruptedException {
        UUID userId = userService.getUser(principal.getName()).getId();

        ResourceModel resourceModel = resourceService.findById(repositoryId).orElseThrow(NoSuchBranchException::new);

        if (!authzService.isViewer(userId, resourceModel.getId())) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);
        GitService.checkout(git, branch, false);

        return ResponseEntity.ok().body("Checkout successful");

    }

    
    @Operation(summary = "Auto save by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/makeCopy")
    ResponseEntity<Object> makeCopy(Principal principal,
                                    @PathVariable("repositoryId") UUID repositoryId,
                                    @PathVariable("branch") String branch,
                                    @RequestBody Map<String, Object> userInput) throws GitAPIException, IOException, InterruptedException {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        File SourceFile = new File(git.getRepository().getDirectory().getParent(), userInput.get("filePath").toString());

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(now);

        String[] fileParts = userInput.get("filePath").toString().split("\\.(?=[^\\.]+$)");
        File TargetFile = new File(git.getRepository().getDirectory().getParent(), fileParts[0] + "_" + timestamp + (fileParts.length > 1 ? "." + fileParts[1] : ""));

        recursiveCopy(SourceFile, TargetFile);

        return ResponseEntity.ok().body("File duplicated with copy");

    }


    // COMMIT
    @Operation(summary = "Save by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/save")
    ResponseEntity<Object> save(Principal principal,
                                @PathVariable("repositoryId") UUID repositoryId,
                                @PathVariable("branch") String branch, @RequestBody HashMap<String, String> commitMessage) throws NoSuchBranchException {

        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        ResourceModel resourceModel = resourceService.findById(repositoryId).orElseThrow(NoSuchBranchException::new);

        if (!authzService.isEditor(userId, resourceModel.getId())) {
            throw new UnauthorizedException();
        }

        Map<String, Object> saveResponse = gitService.commitAllActiveChanges(userId, repositoryId, branch, commitMessage.get("commitMessage"));

        return new ResponseEntity<>(saveResponse, HttpStatus.OK);
    }
    // COMMIT
    @Operation(summary = "Push Active branch by repositoryId.")
    @PostMapping("/{repositoryId}/push")
    ResponseEntity<Object> pushCode(Principal principal, @PathVariable("repositoryId") UUID repositoryId) throws GitAPIException, IOException, InterruptedException {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        ResourceModel resourceModel = resourceService.findById(repositoryId).orElseThrow(NoSuchBranchException::new);

        if (!authzService.isEditor(userId, resourceModel.getId())) {
            throw new UnauthorizedException();
        }

        gitService.pushAll(userId, repositoryId);

        return new ResponseEntity<>("Pushed Successfully", HttpStatus.OK);
    }

    
    @Operation(summary = "Merge by repositoryId, mergeBranch and mergeInto.")
    @GetMapping("/{repositoryId}/{mergeBranch}/{mergeInto}/merge")
    public ResponseEntity<Object> merge(Principal principal,
                                        @PathVariable("repositoryId") UUID repositoryId,
                                        @PathVariable("mergeBranch") String mergeBranch,
                                        @PathVariable("mergeInto") String mergeBranchInto) throws GitAPIException, IOException, InterruptedException {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        GitService.checkout(git, mergeBranchInto, false);

        Ref ref = git.getRepository().findRef("refs/heads/" + mergeBranch);

        MergeCommand mergeCommand = git.merge().include(ref);
        mergeCommand.call();

        return ResponseEntity.ok().body("Merge successful");
    }

    
    @Operation(summary = "Get Git blame for repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/gitBlame")
    public ResponseEntity<List<BlameResultDTO>> gitBlame(Principal principal,
                                                         @PathVariable("repositoryId") UUID repositoryId,
                                                         @PathVariable("branch") String branch, @RequestBody Map<String, String> requestBody) {
        User user = userService.getUser(principal.getName());

        List<BlameResultDTO> result = gitService.gitBlame(user, repositoryId, branch, requestBody.get("filePath"));

        return ResponseEntity.status(200).body(result);
    }

    
    @Operation(summary = "Get tracking status repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/trackingStatus")
    public Map<String, Object> trackingStatus(Principal principal,
                                              @PathVariable("repositoryId") UUID repositoryId,
                                              @PathVariable("branch") String branch) throws GitAPIException, IOException, InterruptedException {
        UUID userId = userService.getUser(principal.getName()).getId();
        Git git = gitService.getGitRepository(userId, repositoryId);

        return gitService.trackingStatus(git, branch);
    }

    @Operation(summary = "Move by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/move")
    ResponseEntity<Object> move(Principal principal,
                                @PathVariable("repositoryId") UUID repositoryId,
                                @PathVariable("branch") String branch,
                                @RequestBody Map<String, String> userInput) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        String sourcePath = userInput.get("source");
        String destinationPath = userInput.get("destination");

        if (userInput.get("sourceType").equals("folder") && userInput.get("destinationType").equals("file")) {
            throw new BadRequestException("Cannot move folder into a file");
        }

        File sourceFile = new File(System.getenv("GIT_CLONED_PATH") + "/" +
                userId + "/" + repositoryId + "/" + sourcePath);
        File destinationFile = new File(System.getenv("GIT_CLONED_PATH") + "/" +
                userId + "/" + repositoryId + "/" + destinationPath);

        Files.move(sourceFile.toPath(), destinationFile.toPath());

        git.rm().addFilepattern(userInput.get("source")).call();
        git.add().addFilepattern(userInput.get("destination")).call();

        CommitCommand commitCommand = git.commit().setMessage(String.format("Moved %s to %s", userInput.get("source"), userInput.get("destination")))
                .setCommitter(userService.getUser(principal.getName()).getName(), userService.getUser(principal.getName()).getEmail());
        commitCommand.call();

        PushCommand pushCommand = git.push();
        pushCommand.call();

        return ResponseEntity.ok().body("moved successfully");
    }

    @Operation(summary = "Move by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/checkout/{commitId}")
    ResponseEntity<Object> checkout(Principal principal,
                                    @PathVariable("repositoryId") UUID repositoryId,
                                    @PathVariable("branch") String branch,
                                    @PathVariable("commitId") String commitId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);
        GitService.checkout(git, branch, true);
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitId).call();
        git.push().setForce(true).call();

        return ResponseEntity.ok().body("reset successfully");

    }

    
    @Operation(summary = "Delete by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/rename")
    public ResponseEntity<Object> rename(Principal principal,
                                         @PathVariable("repositoryId") UUID repositoryId,
                                         @PathVariable("branch") String branch,
                                         @RequestBody Map<String, String> userInput) throws GitAPIException, IOException, InterruptedException {
        UUID userId = userService.getUser(principal.getName()).getId();


        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        List<String> filePath = Arrays.stream(userInput.get("filePath").split("/")).collect(Collectors.toList());
        String fileName = filePath.remove(filePath.size() - 1);
        String relativePath = String.join("/", filePath);
        if (!relativePath.isEmpty()) relativePath = relativePath + "/";

        File fileToRename = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId + "/" + relativePath + fileName);
        File renameTo = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId + "/" + relativePath + userInput.get("newName"));
        Files.move(fileToRename.toPath(), renameTo.toPath());

        git.rm().addFilepattern(relativePath + fileName).call();
        git.add().addFilepattern(relativePath + userInput.get("newName")).call();

        CommitCommand commitCommand = git.commit().setMessage(String.format("Renamed %s to %s", relativePath + fileName, relativePath + userInput.get("newName")))
                .setCommitter(userService.getUser(principal.getName()).getName(), userService.getUser(principal.getName()).getEmail());
        commitCommand.call();

        PushCommand pushCommand = git.push();
        pushCommand.call();

        return ResponseEntity.ok().body("");

    }

    
    @Operation(summary = "Delete by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/delete")
    public ResponseEntity<Object> delete(Principal principal,
                                         @PathVariable("repositoryId") UUID repositoryId,
                                         @PathVariable("branch") String branch,
                                         @RequestBody Map<String, String> userInput) throws GitAPIException, IOException, InterruptedException {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        git.add().addFilepattern(userInput.get("filePath")).call();
        File fileToDelete = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId + "/" + userInput.get("filePath"));

        if (fileToDelete.exists()) {
            git.rm().addFilepattern(userInput.get("filePath")).call();

            CommitCommand commitCommand = git.commit().setMessage("deleted " + userInput.get("filePath"))
                    .setCommitter(userService.getUser(principal.getName()).getName(), userService.getUser(principal.getName()).getEmail());
            commitCommand.call();

            PushCommand pushCommand = git.push();
            pushCommand.call();

            ObjectId lastCommitId = git.getRepository().resolve(Constants.HEAD);
            ObjectId branchId = git.getRepository().resolve(branch);

            Map<String, Object> saveResponse = new HashMap<>();
            saveResponse.put("message", "Pushed successfully");
            saveResponse.put("lastCommitId", lastCommitId);
            saveResponse.put("branchId", branchId);

            // TODO: if we don't add and commit 'from git bash' its causing error

            return new ResponseEntity<>(saveResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("file path to be deleted does not exist", HttpStatus.BAD_REQUEST);
        }

    }

    void recursiveCopy(File sourceFile, File destinationFile) throws IOException {
        if (sourceFile.isDirectory()) {
            if (!destinationFile.exists()) {
                destinationFile.mkdir();
            }

            for (String f : Objects.requireNonNull(sourceFile.list())) {
                recursiveCopy(new File(sourceFile, f), new File(destinationFile, f));
            }
        } else {
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
