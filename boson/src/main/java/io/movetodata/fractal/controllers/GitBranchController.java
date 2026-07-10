package io.movetodata.fractal.controllers;

import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.models.BranchModel;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.repository.BranchRepository;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/fractal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fractal", description = "Code Repository management service endpoints")
public class GitBranchController {
    private final BranchRepository branchRepository;
    private final AuthzService authzService;
    private final UserService userService;
    private final ResourceService resourceService;
    private final GitService gitService;

    @Operation(summary = "Create branch by repositoryId, baseBranch and newBranch.")
    @GetMapping("/{repositoryId}/{baseBranch}/{newBranch}/createBranch")
    public ResponseEntity<Object> createBranch(Principal principal,
                                               @PathVariable("repositoryId") UUID repositoryId,
                                               @PathVariable("baseBranch") String baseBranch,
                                               @PathVariable("newBranch") String newBranch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);
        GitService.checkout(git, baseBranch, false);

        if (!gitService.hasBranch(git, newBranch)) {
            git.branchCreate()
                    .setName(newBranch)
                    .call()
                    .getName();
            git.checkout().setCreateBranch(true);
            StoredConfig config = git.getRepository().getConfig();
            config.setString("branch", newBranch, "remote", "origin");
            config.setString("branch", newBranch, "merge", "refs/heads/" + newBranch);
            config.save();

            GitService.checkout(git, newBranch, false);

            BranchModel branchModel = new BranchModel();
            // Doubtful if random UUID matches with some existing UUID
            branchModel.setId(String.valueOf(UUID.randomUUID()));
//                branchModel.setDatasetId(datasetId);
            branchModel.setBranch(newBranch);
//                branchModel.setType(ResourceSubtype.RAW);e
            branchModel.setCreatedBy(userId);
            branchModel.setCreatedAt(new Date());
//                branchModel.setDataset(datasetModel);
            branchModel.setRepositoryId(repositoryId);
            branchRepository.save(branchModel);

            return ResponseEntity.ok().body("Branch Created Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorDTO.builder().status(HttpStatus.BAD_REQUEST.value()).error("Bad Request").description("Branch Already Exists").build());
        }
    }

    @Operation(summary = "Delete branch by repositoryId and branch.")
    @DeleteMapping("/{repositoryId}/{branch}/deleteBranch")
    public ResponseEntity<Object> deleteBranch(Principal principal,
                                               @PathVariable("repositoryId") UUID repositoryId,
                                               @PathVariable("branch") String branch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }
        if(branch.equals("master")) throw new UnauthorizedException("Master Branch can not be deleted");

        Git git = gitService.getGitRepository(userId, repositoryId);
        git.branchDelete().setForce(true).setBranchNames(branch).call();

        return ResponseEntity.ok().body("Branch deleted Successfully");
    }

    @Operation(summary = "Delete all branches by repositoryId.")
    @DeleteMapping("/{repositoryId}/deleteBranches")
    ResponseEntity<Object> deleteBranches(Principal principal,
                                          Collection<String> deleteBranches,
                                          @PathVariable("repositoryId") UUID repositoryId) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        DeleteBranchCommand deleteBranchCommand = git.branchDelete()
                .setBranchNames(deleteBranches.stream().filter(branch -> !branch.equals("master")).collect(Collectors.toList()).toArray(new String[0]))
                .setForce(true);
        List<String> resultList = deleteBranchCommand.call();
        return new ResponseEntity<>(resultList, HttpStatus.OK);

    }

    @Operation(summary = "Rename branch by repositoryId, branch and newBranch.")
    @GetMapping("/{repositoryId}/{branch}/{newBranch}/renameBranch")
    public ResponseEntity<Object> renameBranch(Principal principal,
                                               @PathVariable("repositoryId") UUID repositoryId,
                                               @PathVariable("branch") String branch,
                                               @PathVariable("newBranch") String newBranch) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        if (gitService.hasBranch(git, newBranch)) {
            return new ResponseEntity<>("Branch with name " + newBranch + " already exists", HttpStatus.BAD_REQUEST);
        }

        git.branchRename().setOldName(branch).setNewName(newBranch).call();

        return ResponseEntity.ok().body("Branch rename Successfully");

    }

    @Operation(summary = "Pull the branch by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/pullBranch")
    @PreAuthorize(Auth.VIEWER)
    ResponseEntity<Object> pullBranch(Principal principal,
                                      @PathVariable("repositoryId") @Param("id") UUID repositoryId,
                                      @PathVariable("branch") String branch) throws GitAPIException, IOException, InterruptedException {
        UUID userId = userService.getUser(principal.getName()).getId();

        Git git = gitService.getGitRepository(userId, repositoryId);

        gitService.pull(git, userId, branch);

        return ResponseEntity.ok().body("Pull branch successful");
    }


    @Operation(summary = "Get local branch by repositoryId.")
    @GetMapping("/{repositoryId}/branches")
    public Map<String, Object> getBranches(Principal principal,
                                                @PathVariable("repositoryId") UUID repositoryId) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        Git git = gitService.getGitRepository(userId, repositoryId);

        Map<String, Object> branchInfo = new HashMap<>();
        branchInfo.put("branches", GitService.getAllBranches(git));
        branchInfo.put("activeBranch", GitService.getActiveBranch(git));
        branchInfo.put("lastCommitId", GitService.getLastCommitId(git));
        return branchInfo;
    }


}