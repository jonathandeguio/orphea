//package io.bosler.fractal.fractal.controllers;
//
//import io.bosler.fractal.library.services.GitService;
//import io.bosler.kitab.library.models.BranchModel;
//import io.bosler.kitab.library.models.FolderModel;
//import io.bosler.kitab.library.repository.BranchRepository;
//import io.bosler.kitab.library.repository.FolderRepository;
//import io.bosler.passport.library.service.AuthzService;
//import io.bosler.passport.library.service.UserService;
//import io.bosler.passport.security.UserPrincipal;
//import io.bosler.sharedUtils.Response.NoSuchElementFoundException;
//import io.bosler.sharedUtils.Response.OkResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import org.eclipse.jgit.api.CommitCommand;
//import org.eclipse.jgit.api.DeleteBranchCommand;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.ListBranchCommand;
//import org.eclipse.jgit.lib.Ref;
//import org.eclipse.jgit.lib.Repository;
//import org.eclipse.jgit.lib.StoredConfig;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import java.security.Principal;
//import java.util.*;
//
//@CrossOrigin
//@EnableWebMvc
//@RestController
//@RequestMapping("/api/fractal")
//@RequiredArgsConstructor
//@SecurityRequirement(name = "bearerAuth")
//@Tag(name = "Fractal", description = "Code Repository management service endpoints")
//public class GitBranchController {
//
//    static String masterBranch = "master";
//    private final BranchRepository branchRepository;
//    private final AuthzService authzService;
//    private final UserService userService;
//    private final FolderRepository folderRepository;
//    private final GitService gitServiceFractal;
//    private final OkResponse response = new OkResponse();
//
//    @Operation(summary = "Create branch by repositoryId, baseBranch and newBranch.")
//    @GetMapping("/{repositoryId}/{baseBranch}/{newBranch}/createBranch")
//    public ResponseEntity<Object> createBranch(Principal principal,
//                                               @PathVariable("repositoryId") String repositoryId,
//                                               @PathVariable("baseBranch") String baseBranch,
//                                               @PathVariable("newBranch") String newBranch) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isOwner(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, baseBranch);
//
//        if (repository.getObjectDatabase().exists() && branchExists(principal, repositoryId, baseBranch)) {
//            Git git = new Git(repository);
//            if (!branchExists(principal, repositoryId, newBranch)) {
//                git.checkout().setName(baseBranch).call();
//                git.branchCreate()
//                        .setName(newBranch)
//                        .call()
//                        .getName();
//                git.checkout().setCreateBranch(true);
//                StoredConfig config = git.getRepository().getConfig();
//                config.setString("branch", newBranch, "remote", "origin");
//                config.setString("branch", newBranch, "merge", "refs/heads/" + newBranch);
//                config.save();
//
//
//                BranchModel branchModel = new BranchModel();
//                // Doubtful if random UUID matches with some existing UUID
//                branchModel.setId(String.valueOf(UUID.randomUUID()));
////                branchModel.setDatasetId(datasetId);
//                branchModel.setBranch(newBranch);
////                branchModel.setType("raw");e
//                branchModel.setCreatedBy(userId);
//                branchModel.setCreatedAt(new Date());
////                branchModel.setDataset(datasetModel);
//                branchModel.setRepositoryId(UUID.fromString(repositoryId));
//                branchRepository.save(branchModel);
//
//                return new ResponseEntity<>(response.okResponse("Branch Created Successfully"), HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>("Branch already exists", HttpStatus.BAD_REQUEST);
//            }
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + baseBranch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @Operation(summary = "Delete branch by repositoryId and branch.")
//    @DeleteMapping("/{repositoryId}/{branch}/deleteBranch")
//    public ResponseEntity<Object> deleteBranch(Principal principal,
//                                               @PathVariable("repositoryId") String repositoryId,
//                                               @PathVariable("branch") String branch) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isOwner(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, masterBranch);
//        Git git = new Git(repository);
//        if (repository.getObjectDatabase().exists() && branchExists(principal, repositoryId, branch)) {
//            git.branchDelete().setForce(true).setBranchNames(branch).call();
//
//            return new ResponseEntity<>(response.okResponse("Branch deleted Successfully"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//
//        }
//    }
//
//    @Operation(summary = "Delete all branches by repositoryId.")
//    @DeleteMapping("/{repositoryId}/deleteBranches")
//    ResponseEntity<Object> deleteBranches(Principal principal,
//                                                Git git,
//                                                Collection<String> deleteBranches,
//                                                @PathVariable("repositoryId") String repositoryId) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isOwner(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, masterBranch);
//
//        if (repository.getObjectDatabase().exists()) {
//            DeleteBranchCommand deleteBranchCommand = git.branchDelete()
//                    .setBranchNames(deleteBranches.toArray(new String[0]))
//                    .setForce(true);
//            List<String> resultList = deleteBranchCommand.call();
//            return new ResponseEntity<>(resultList, HttpStatus.OK);
//        } else {
//
//            throw new NoSuchElementFoundException("Repository not found");
//        }
//    }
//
//    @Operation(summary = "Rename branch by repositoryId, branch and newBranch.")
//    @GetMapping("/{repositoryId}/{branch}/{newBranch}/renameBranch")
//    public ResponseEntity<Object> renameBranch(Principal principal,
//                                               @PathVariable("repositoryId") String repositoryId,
//                                               @PathVariable("branch") String branch,
//                                               @PathVariable("newBranch") String newBranch) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isOwner(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//
//        if (repository.getObjectDatabase().exists() && branchExists(principal, repositoryId, branch)) {
//            if (branchExists(principal, repositoryId, newBranch)) {
//                return new ResponseEntity<>("Branch with name " + newBranch + " already exists", HttpStatus.BAD_REQUEST);
//            }
//
//            Git git = new Git(repository);
//            git.branchRename().setOldName(branch).setNewName(newBranch).call();
//
//            if (branch.equals(masterBranch)) {
//                System.out.println("master branch name changed successfully");
//                masterBranch = newBranch;
//            }
//
//            return new ResponseEntity<>(response.okResponse("Branch rename Successfully"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Pull the branch by repositoryId and branch.")
//    @GetMapping("/{repositoryId}/{branch}/pullBranch")
//    ResponseEntity<Object> pullBranch(Principal principal,
//                                      @PathVariable("repositoryId") String repositoryId,
//                                      @PathVariable("branch") String branch) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isOwner(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//        Git git = new Git(repository);
//
////        if(repository.getObjectDatabase().exists() && remoteBranchExists(repositoryId, branch)) {
//        if (repository.getObjectDatabase().exists()) {
//            if (branchExists(principal, repositoryId, branch)) {
//                git.checkout().setName(branch).call();
//
//                git.add().addFilepattern(".").call();
//
//                CommitCommand commitCommand = git.commit().setMessage(new Date().toString())
//                        .setCommitter(userService.getUser(principal.getName()).getName(), userService.getUser(principal.getName()).getEmail());
//                commitCommand.call();
//
//            } else {
//                git.checkout().setName(branch).setCreateBranch(true).setStartPoint("origin/" + branch).call();
//            }
//            git.pull().setRemote("origin").setRemoteBranchName(branch).call();
//
//            return new ResponseEntity<>(response.okResponse("Pull branch successful"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @Operation(summary = "Get local branch by repositoryId.")
//    @GetMapping("/{repositoryId}/localBranches")
//    public Map<String, Object> getLocalBranches(Principal principal,
//                                                @PathVariable("repositoryId") String repositoryId) throws Exception {
//
//        List<String> branches = new ArrayList<>();
//        UUID userId = userService.getUser(principal.getName()).getId();
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, masterBranch);
//
//        if (repository.getObjectDatabase().exists()) {
//            Git git = new Git(repository);
//            final List<Ref> branchRefs = git.branchList().call();
//            for (Ref ref : branchRefs) {
//                String name = ref.getName();
//                branches.add(name);
//            }
//
//            Map<String, Object> branchInfo = new HashMap<>();
//            branchInfo.put("localBranches", branches);
//            branchInfo.put("activeBranch", repository.getBranch());
//            return branchInfo;
//        } else {
//            throw new NoSuchElementFoundException("Repository does not exist");
//        }
//    }
//
//    @Operation(summary = "Get remote branches by repositoryId.")
//    @GetMapping("/{repositoryId}/remoteBranches")
//    public List<String> getRemoteBranches(Principal principal,@PathVariable("repositoryId") String repositoryId) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        List<String> branches = new ArrayList<>();
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, masterBranch);
//
//        if (repository.getObjectDatabase().exists()) {
//            Git git = new Git(repository);
//            final List<Ref> branchRefs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
//            for (Ref ref : branchRefs) {
//                String name = ref.getName();
//                branches.add(name);
//            }
//            return branches;
//        } else {
//            throw new NoSuchElementFoundException("Repository does not exist");
//        }
//    }
//
//    public boolean branchExists(Principal principal,
//                                String RepositoryId,
//                                String branch) throws Exception {
//
//        List<String> branches = (List<String>) getLocalBranches(principal, RepositoryId).get("localBranches");
//        for (String b : branches) {
//            if (b.substring(b.lastIndexOf('/') + 1).equals(branch)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    public boolean remoteBranchExists(String repositoryId,
////                                      String branch) throws Exception {
////
////        List<String> branches = getRemoteBranches(repositoryId);
////        for (String b : branches) {
////            if (b.substring(b.lastIndexOf('/') + 1).equals(branch)) {
////                return true;
////            }
////        }
////        return false;
////    }
//}
