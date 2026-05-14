package io.orphea.fractal.fractal.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/fractal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fractal", description = "Code Repository management service endpoints")
public class FractalController {

//    private final UserService userService;
//    private final AuthzService authzService;
//    private final BuildService buildService;
//    private final GitService gitServiceFractal;
//    private final FolderRepository folderRepository;
//    private final GitBranchController gitBranchController;
//    private final ResourceViewsRepository resourceViewsRepository;
//    private final OkResponse response = new OkResponse();
    String syncStatus = "in sync";

//    @Autowired
//    SimpMessagingTemplate template;

    private static List<Integer> getCounts(Repository repository, String branch) throws IOException {
        BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, branch);
        List<Integer> counts = new ArrayList<>();
        if (trackingStatus != null) {
            counts.add(trackingStatus.getAheadCount());
            counts.add(trackingStatus.getBehindCount());
        } else {
            System.out.println("Returned null, likely no remote tracking of branch " + branch);
            counts.add(0);
            counts.add(0);
        }
        return counts;
    }


    @Operation(summary = "Get open repositoryId and branch.")
    @GetMapping(value = "/{repositoryId}/{branch}/open")
    ResponseEntity<Object> open(@PathVariable("repositoryId") String repositoryId,
                                @PathVariable("branch") String branch) {


//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isViewer(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();
//
//        resourceViewsModel.setResourceId(UUID.fromString(repositoryId));
//        resourceViewsModel.setAction("view");
//        resourceViewsModel.setViewedBy(userId);
//
//        resourceViewsRepository.save(resourceViewsModel);
//
//        File file = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId);
//
//        if (!file.exists()) {
//            Git.cloneRepository()
//                    .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
//                    .setDirectory(new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId))
//                    .call();
//        }
        return new ResponseEntity<>("Repository with repoId " + repositoryId + " opened successfully", HttpStatus.OK);

    }


    @Operation(summary = "Add File to hadoop")
    @GetMapping(value = "/addGitFiles/{repoId}/{fileType}")
    ResponseEntity<Object> addGitFiles(Principal principal,
                                       @PathVariable("repoId") String repoId,
                                       @PathVariable("fileType") String fileType
                                       ) throws IOException {

        // Create a Configuration object
        Configuration conf = new Configuration();

        // Set the URI of the HDFS NameNode
        conf.set("fs.defaultFS", "hdfs://localhost:9000");
        try {
            FileSystem fs = FileSystem.get(conf);
            Path sourceDirectory = new Path("/home/fa065107/Desktop/test_git/" + repoId +  "/.git/objects/pack/pack-5b9c9ec011bdb310ad05767bd0e9329a8f8172b4.pack");
            Path targetDirectory = new Path("hdfs://localhost:9000/" + repoId + "/pack/");

            // Copy files from the local folder to HDFS
//            fs.copyFromLocalFile(true, sourceDirectory, targetDirectory);
            fs.copyFromLocalFile(sourceDirectory, targetDirectory);
//            copyDirectory(fs, sourceDirectory,targetDirectory);


            System.out.println("Folder copied to HDFS successfully.");
        } catch (IOException e) {
            System.err.println("Error copying files to Hadoop directory: " + e.getMessage());
        }

        return new ResponseEntity<>("Add file success", HttpStatus.OK);
    }

    private static void copyDirectory(FileSystem fs, Path localPath, Path hdfsPath) throws IOException {
        System.out.println(localPath.toString());
        File[] files = new File(localPath.toString()).listFiles();
        System.out.println(files.length);
        if (files != null) {
            for (File file : files) {

                if (file.isDirectory()) {
                    System.out.println("DIRECT" + file.toString());
                    Path newHdfsPath = new Path(hdfsPath, file.getName());
                    fs.mkdirs(newHdfsPath);
                    copyDirectory(fs, new Path(file.getAbsolutePath()), newHdfsPath);
                } else {
                    System.out.println("FILE : " + file.toString());
                    fs.copyFromLocalFile(new Path(file.getAbsolutePath()), hdfsPath);
                }
            }
        }
    }


    @Operation(summary = "List root of HDFS.")
    @GetMapping(value = "/createRepo")
    ResponseEntity<Object> createRepo() throws IOException {
        UUID RepositoryId = UUID.randomUUID();
        File repositoryDirectory = new File("/home/fa065107/Desktop/test_git/" + RepositoryId);

        if (repositoryDirectory.exists()) {
            return new ResponseEntity<>("Repository already exists", HttpStatus.BAD_REQUEST);
        }

        Repository remoteRepository = new FileRepositoryBuilder()
                .setGitDir(new File("/home/fa065107/Desktop/test_git/" + RepositoryId + File.separator + ".git"))
                .readEnvironment()
                .findGitDir()
                .build();

        remoteRepository.create();

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000");

        try {
            FileSystem fs = FileSystem.get(conf);
            Path folderPath = new Path("hdfs://localhost:9000/" + RepositoryId + "/pack");
            Path folderPath2 = new Path("hdfs://localhost:9000/" + RepositoryId + "/refs");

            // Create the folder
            boolean success = fs.mkdirs(folderPath);
            boolean success2 = fs.mkdirs(folderPath2);

            if (success && success2) {
                System.out.println("Folder created successfully.");
            } else {
                System.err.println("Failed to create folder.");
            }
        } catch (IOException e) {
            System.err.println("Error creating folder: " + e.getMessage());
        }

//        FileWriter fileWriter = new FileWriter(System.getenv("JULIA_BASE_PATH") + "/"
//                + RepositoryId + "/.git/config", true);
//        fileWriter.append("[http]\n" + "    receivepack = true");
//        fileWriter.close();


        return new ResponseEntity<>("Repository created successfully", HttpStatus.OK);
    }


    @Operation(summary = "List root of HDFS.")
    @GetMapping(value = "/listHDFS")
    ResponseEntity<Object> listHDFS() {

        // Create a Configuration object
        Configuration conf = new Configuration();

        // Set the URI of the HDFS NameNode
        conf.set("fs.defaultFS", "hdfs://localhost:9000");

        try {
            // Create a FileSystem object
            FileSystem fs = FileSystem.get(conf);

            // Specify the directory path in HDFS
            Path directoryPath = new Path("/");

            // List files inside the directory
            FileStatus[] fileStatuses = fs.listStatus(directoryPath);

            List<String> listFiles = listFilesRecursively(fs, directoryPath);

            // Close the FileSystem object
            fs.close();

            return new ResponseEntity<>(listFiles, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("files here", HttpStatus.OK);
    }


    private static List<String> listFilesRecursively(FileSystem fs, Path directoryPath) throws IOException {
        List<String> fileList = new ArrayList<>();
        FileStatus[] fileStatuses = fs.listStatus(directoryPath);
        for (FileStatus fileStatus : fileStatuses) {
            if (fileStatus.isDirectory()) {
                fileList.addAll(listFilesRecursively(fs, fileStatus.getPath()));
            } else {
                fileList.add(fileStatus.getPath().toString());
            }
        }
        return fileList;
    }

//    @SneakyThrows
//    @Operation(summary = "Get fresh clone by repositoryId and branch.")
//    @GetMapping(value = "/{repositoryId}/{branch}/cloneAfresh")
//    ResponseEntity<Object> cloneAfresh(Principal principal,
//                                       @PathVariable("repositoryId") String repositoryId,
//                                       @PathVariable("branch") String branch) {
//
////        Repository remoteRepository = gitServiceFractal.getRepository("/git/remote/" + repositoryId, "master");
//        UUID userId = userService.getUser(principal.getName()).getId();
//
////        if (remoteRepository.getObjectDatabase().exists()) {
//
////            TODO : unable to delete repository's/.git/objects folder which is causing error
//
//        File file = new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId);
//        try {
//            if (file.exists()) {
//                FileDeleteStrategy.FORCE.delete(file);
//            }
//            Git.cloneRepository()
//                    .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
//                    .setDirectory(new File(System.getenv("GIT_CLONED_PATH") + "/" + userId + "/" + repositoryId))
//                    .call().getRepository().close();
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage() + " ********************");
//        }
//
//        return new ResponseEntity<>(response.okResponse("Repository with repoId " +
//                repositoryId + " cloned successfully"), HttpStatus.OK);
////        } else {
////            return new ResponseEntity<>("Repository with Id " + repositoryId + " does not exist", HttpStatus.NOT_FOUND);
////        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Checkout by repositoryId and branch.")
//    @GetMapping("/{repositoryId}/{branch}/checkout")
//    ResponseEntity<Object> checkout(Principal principal,
//                                    @PathVariable("repositoryId") String repositoryId,
//                                    @PathVariable("branch") String branch) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isViewer(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
//            Ref checkoutCommand = new Git(repository).checkout().setName(branch).call();
//
//            return new ResponseEntity<>(response.okResponse("Checkout successful"), HttpStatus.OK);
//
//        } else {
//            return new ResponseEntity<>("Not able to checkout.", HttpStatus.NOT_FOUND);
//
//        }
//
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Auto save by repositoryId and branch.")
//    @PostMapping("/{repositoryId}/{branch}/autoSave")
//    ResponseEntity<Object> autoSave(Principal principal,
//                                    @PathVariable("repositoryId") String repositoryId,
//                                    @PathVariable("branch") String branch,
//                                    @RequestBody ArrayList<Map<String, Object>> userInput) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isEditor(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
////            new Git(repository).checkout().setName(branch).call();
//
//            for (Map<String, Object> contents : userInput) {
//
//                String fileContent = (String) contents.get("fileContent");
//                byte[] valueDecoded = Base64.decodeBase64(fileContent.getBytes());
//                String folderPath = (String) contents.get("filePath");
//                int index = folderPath.lastIndexOf('/');
//                File file;
//                if (index != -1) {
//                    folderPath = folderPath.substring(0, index);
//                    file = new File(repository.getDirectory().getParent(), folderPath);
//                    file.mkdirs();
//                }
//                file = new File(repository.getDirectory().getParent(), (String) contents.get("filePath"));
//                FileWriter fileWriter = new FileWriter(file.getPath());
//                fileWriter.write(new String(valueDecoded));
//                fileWriter.close();
//            }
//            return new ResponseEntity<>(response.okResponse("Save successful"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Auto save by repositoryId and branch.")
//    @PostMapping("/{repositoryId}/{branch}/makeCopy")
//    ResponseEntity<Object> makeCopy(Principal principal,
//                                    @PathVariable("repositoryId") String repositoryId,
//                                    @PathVariable("branch") String branch,
//                                    @RequestBody ArrayList<Map<String, Object>> userInput) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isEditor(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
//            new Git(repository).checkout().setName(branch).call();
//
//            for (Map<String, Object> contents : userInput) {
//
//                File SourceFile;
//                File TargetFile;
//
//                String[] fileParts = contents.get("filePath").toString().split("\\.(?=[^\\.]+$)");
//
//                SourceFile = new File(repository.getDirectory().getParent(), contents.get("filePath").toString());
//                TargetFile = new File(repository.getDirectory().getParent(), fileParts[0] + "-copy." + fileParts[1]);
//
//                Files.copy(SourceFile.toPath(), TargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//
//            }
//            return new ResponseEntity<>(response.okResponse("File duplicated with copy"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Save by repositoryId and branch.")
//    @GetMapping("/{repositoryId}/{branch}/save")
//    ResponseEntity<Object> save(Principal principal,
//                                @PathVariable("repositoryId") String repositoryId,
//                                @PathVariable("branch") String branch) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isEditor(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        folderModel.setUpdatedAt(new Date());
//        folderModel.setUpdatedBy(userId);
//
//        folderRepository.save(folderModel);
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//        Git git = new Git(repository);
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
////            if (gitBranchController.remoteBranchExists(repositoryId, branch)) {
//            Map<String, Object> syncStatus = trackingStatus(principal, repositoryId, branch);
//            if (!(boolean) syncStatus.get("inSync")) {
//                return new ResponseEntity<>("You are out of sync, please pull first", HttpStatus.BAD_REQUEST);
//            }
////            }
//            CheckoutCommand checkoutCommand = git.checkout().setName(branch);
//            checkoutCommand.call();
//
//            git.add().addFilepattern(".").call();
//
//            CommitCommand commitCommand = git.commit().setMessage(new Date().toString())
//                    .setCommitter(userService.getUser(principal.getName()).getName(), userService.getUser(principal.getName()).getEmail());
//            commitCommand.call();
//
//            PushCommand pushCommand = git.push();
//            pushCommand.call();
//
//
//            ObjectId lastCommitId = repository.resolve(Constants.HEAD);
//
//            ObjectId branchId = repository.resolve(branch);
//
//            Map<String, Object> saveResponse = new HashMap<>();
//            saveResponse.put("message", "Pushed successfully");
//            saveResponse.put("lastCommitId", lastCommitId);
//            saveResponse.put("branchId", branchId);
//
//            return new ResponseEntity<>(saveResponse, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Merge by repositoryId, mergeBranch and mergeInto.")
//    @GetMapping("/{repositoryId}/{mergeBranch}/{mergeInto}/merge")
//    public ResponseEntity<Object> merge(Principal principal,
//                                        @PathVariable("repositoryId") String repositoryId,
//                                        @PathVariable("mergeBranch") String mergeBranch,
//                                        @PathVariable("mergeInto") String mergeBranchInto) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
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
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, mergeBranch);
//        Git git = new Git(repository);
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, mergeBranch)
//                && gitBranchController.branchExists(principal, repositoryId, mergeBranchInto)) {
//
//            CheckoutCommand checkoutCommand = git.checkout().setName(mergeBranchInto);
//            checkoutCommand.call();
//
//            Ref ref = git.getRepository().findRef("refs/heads/" + mergeBranch);
//
//            MergeCommand mergeCommand = git.merge().include(ref);
//            mergeCommand.call();
//
//            return new ResponseEntity<>(response.okResponse("Merge successful"), HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + mergeBranchInto + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Get tracking status repositoryId and branch.")
//    @GetMapping("/{repositoryId}/{branch}/trackingStatus")
//    public Map<String, Object> trackingStatus(Principal principal,
//                                              @PathVariable("repositoryId") String repositoryId,
//                                              @PathVariable("branch") String branch) {
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        // Sending to socket to tell that someone opened repo
////        SocketMessage textMessage = new SocketMessage();
////        textMessage.setMessage(userId.toString());
////
////        template.convertAndSend("/topic/repository/" + repositoryId + "/" + branch, textMessage);
//
//        String repoName = userId + "/" + repositoryId;
//        Repository repository = gitServiceFractal.getRepository(repoName, branch);
//        Git git = new Git(repository);
//
//        git.fetch().setCheckFetchedObjects(true).call();
//        List<Ref> branchList = git.branchList().call();
//        Ref branchRef = null;
//
//        for (Ref ref : branchList) {
//            if (ref.getName().equals("refs/heads/" + branch)) {
//                branchRef = ref;
//                break;
//            }
//        }
//
//        if (branchRef == null) {
//            throw new NoSuchElementFoundException("Branch not found");
//        }
//
//        List<Integer> counts = getCounts(repository, branchRef.getName());
//        Map<String, Object> trackRecord = new HashMap<>();
//        trackRecord.put("ahead", counts.get(0));
//        trackRecord.put("behind", counts.get(1));
//        trackRecord.put("inSync", counts.get(1) == 0);
//
//        Status statusCommand = git.status().call();
//        trackRecord.put("gitStatus", statusCommand);
//
//        return trackRecord;
//    }
//
//    @Operation(summary = "Move by repositoryId and branch.")
//    @PostMapping("/{repositoryId}/{branch}/move")
//    ResponseEntity<Object> move(Principal principal,
//                                @PathVariable("repositoryId") String repositoryId,
//                                @PathVariable("branch") String branch,
//                                @RequestBody Map<String, String> userInput) throws Exception {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
//            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//
//        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));
//
//        if (!authzService.isEditor(userId, folderModel.getParent())) {
//            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
//        }
//
//        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
//
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
//            new Git(repository).checkout().setName(branch).call();
//            String sourcePath = userInput.get("source");
//            String destinationPath = userInput.get("destination");
//
//            if (userInput.get("sourceType").equals("folder") && userInput.get("destinationType").equals("file")) {
//                throw new BadRequestException("Cannot move folder into a file");
//            }
//
//            File sourceFile = new File(System.getenv("GIT_CLONED_PATH") + "/" +
//                    userId + "/" + repositoryId + "/" + sourcePath);
//            File destinationFile = new File(System.getenv("GIT_CLONED_PATH") + "/" +
//                    userId + "/" + repositoryId + "/" + destinationPath);
//
//            Files.move(sourceFile.toPath(), destinationFile.toPath());
//
//            return new ResponseEntity<>(response.okResponse("moved successfully"), HttpStatus.OK);
//
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//
//        }
//    }
//
//    @SneakyThrows
//    @Operation(summary = "Delete by repositoryId and branch.")
//    @DeleteMapping("/{repositoryId}/{branch}/delete")
//    public ResponseEntity<Object> delete(Principal principal,
//                                         @PathVariable("repositoryId") String repositoryId,
//                                         @PathVariable("branch") String branch,
//                                         @RequestBody Map<String, String> userInput) {
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
//        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
//
//            new Git(repository).checkout().setName(branch).call();
//            File fileToDelete = new File(System.getenv("GIT_CLONED_PATH") + "/" +
//                    userId + "/" + repositoryId + "/" + userInput.get("filePath"));
//            if (fileToDelete.exists()) {
//                recursiveDeleteNonEmptyDirectories(fileToDelete);
//
//                // TODO: if we don't add and commit 'from git bash' its causing error
//
//                return new ResponseEntity<>(response.okResponse("deleted successfully"), HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>("file path to be deleted does not exist", HttpStatus.BAD_REQUEST);
//            }
//        } else {
//            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
//        }
//    }
//
//    private void recursiveDeleteNonEmptyDirectories(File file) throws IOException {
//        File[] contents = file.listFiles();
//        if (contents != null) {
//            for (File f : contents) {
//                recursiveDeleteNonEmptyDirectories(f);
//            }
//        }
//        FileUtils.delete(file);
//    }
//
//    @Operation(summary = "This api can be used to start jupyter instance.")
//    @GetMapping("/startJupyter")
//    ResponseEntity<Object> startJupyter(Principal principal, HttpServletRequest httpServletRequest) throws Exception {
//
//        return new ResponseEntity<>("Jupyter started", HttpStatus.OK);
//
//        /*
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        String token = AuthUtils.getJwtFromRequest(httpServletRequest);
//
//        ApiClient client = buildService.kubernetesClient();
//        Configuration.setDefaultApiClient(client);
//
//        CoreV1Api coreApi = new CoreV1Api(client);
//
//        String namespace = "orphea";
//
//        // Check first if it is already running
//        String podName = "callisto-" + userId;
//
//        // List all the Pods in the namespace
//        V1PodList podList = coreApi.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
//
//        // Check if the Pod is present in the list
//        for (V1Pod pod : podList.getItems()) {
//            if (pod.getMetadata().getName().equals(podName)) {
//                return new ResponseEntity<>("Jupyter already running.", HttpStatus.OK);
//            }
//        }
//
//
//
//        // create a SimpleDateFormat object with desired date format
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//        sdf.setTimeZone(TimeZone.getDefault());
//        String tokenTTL = sdf.format(AuthUtils.getTTLFromJwt(token));
//
//        V1ObjectMeta metadata = new V1ObjectMeta();
//        metadata.setName(podName);
//        metadata.setNamespace(namespace);
//
//        Map<String, String> labels = new HashMap<>();
//        labels.put("name", podName);
//        metadata.setLabels(labels);
//
//        V1Container container = new V1Container();
//        container.setImage(System.getenv("CALLISTO_IMAGE"));
//        container.setImagePullPolicy("Always");
//        container.setName(podName);
//
//
//        List<V1EnvVar> envVars = new ArrayList<>();
//        envVars.add(new V1EnvVar().name("ORPHEA_USERID").value(userId.toString()));
//        envVars.add(new V1EnvVar().name("BASE_URL").value(System.getenv("BASE_URL")));
//        envVars.add(new V1EnvVar().name("ORPHEA_API").value("http://boson:8080"));
//        envVars.add(new V1EnvVar().name("DB_USERNAME").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("boson-db-username"))));
//        envVars.add(new V1EnvVar().name("DB_PASSWORD").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("boson-db-password"))));
//        envVars.add(new V1EnvVar().name("DB_HOST").value(System.getenv("DB_HOST")));
//        envVars.add(new V1EnvVar().name("DB_PORT").value(System.getenv("DB_PORT")));
//        envVars.add(new V1EnvVar().name("HOST").value(System.getenv("HOST")));
//        envVars.add(new V1EnvVar().name("JULIA_HOST").value(System.getenv("JULIA_HOST")));
//        envVars.add(new V1EnvVar().name("JULIA_API_PORT").value(System.getenv("JULIA_API_PORT")));
//        envVars.add(new V1EnvVar().name("JULIA_PORT").value(System.getenv("JULIA_PORT")));
//        envVars.add(new V1EnvVar().name("GIT_CLONED_PATH").value(System.getenv("GIT_CLONED_PATH")));
//        envVars.add(new V1EnvVar().name("BACKING_FS").value(System.getenv("BACKING_FS")));
//        envVars.add(new V1EnvVar().name("HDFS_ENDPOINT").value(System.getenv("HDFS_ENDPOINT")));
//        envVars.add(new V1EnvVar().name("GS_BUCKET").value(System.getenv("GS_BUCKET")));
//        envVars.add(new V1EnvVar().name("MINIO_ENDPOINT").value(System.getenv("MINIO_ENDPOINT")));
//
//        envVars.add(new V1EnvVar().name("NOTEBOOK_DIR").value(System.getenv("ORPHEA_MOUNT_PATH") + "/git/cloned"));
//        envVars.add(new V1EnvVar().name("NOTEBOOK_TOKEN").value(token));
//        envVars.add(new V1EnvVar().name("NOTEBOOK_TOKEN_TTL").value(tokenTTL));
//
//        envVars.add(new V1EnvVar().name("TOKEN_SECRET").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("token-secret"))));
//        envVars.add(new V1EnvVar().name("FRACTAL_TEMPLATES_TOKEN").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("fractal-templates-token"))));
//        envVars.add(new V1EnvVar().name("MINIO_ACCESS_KEY").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("minio-username"))));
//        envVars.add(new V1EnvVar().name("MINIO_SECRET_KEY").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("minio-password"))));
//        envVars.add(new V1EnvVar().name("GOOGLE_CLOUD_CREDENTIALS").valueFrom(new V1EnvVarSource().secretKeyRef(new V1SecretKeySelector().name("orphea-secret").key("google-cloud-credentials"))));
//        container.setEnv(envVars);
//
//        List<V1ContainerPort> ports = new ArrayList<>();
//        ports.add(new V1ContainerPort().containerPort(8686).protocol("TCP"));
//        container.setPorts(ports);
//
//        List<V1VolumeMount> volumeMounts = new ArrayList<>();
//        volumeMounts.add(new V1VolumeMount().mountPath("/orphea").name("orphea-nfs-volume"));
//        container.setVolumeMounts(volumeMounts);
//
//        container.setWorkingDir(System.getenv("ORPHEA_MOUNT_PATH"));
//
//        List<V1Container> containers = new ArrayList<>();
//        containers.add(container);
//
//        List<V1LocalObjectReference> imagePullSecrets = new ArrayList<>();
//        imagePullSecrets.add(new V1LocalObjectReference().name("regcred"));
//
//        V1PersistentVolumeClaimVolumeSource pvcVolumeSource = new V1PersistentVolumeClaimVolumeSource();
//        pvcVolumeSource.setClaimName("orphea-pvc");
//
//        V1Volume orpheaNfsVolume = new V1Volume();
//        orpheaNfsVolume.setName("orphea-nfs-volume");
//        orpheaNfsVolume.setPersistentVolumeClaim(pvcVolumeSource);
//
//        List<V1Volume> volumes = new ArrayList<>();
//        volumes.add(orpheaNfsVolume);
//
//        V1PodSpec podSpec = new V1PodSpec();
//        podSpec.setContainers(containers);
//        podSpec.setImagePullSecrets(imagePullSecrets);
//        podSpec.setVolumes(volumes);
//
//        V1Pod podAuto = new V1Pod();
//        podAuto.setMetadata(metadata);
//        podAuto.setSpec(podSpec);
//
//        V1Pod createdPod = coreApi.createNamespacedPod(namespace, podAuto, null, null, null, null);
//
//
//        V1ObjectMeta metadataService = new V1ObjectMeta();
//        metadataService.setName(podName);
//        metadataService.setNamespace(namespace);
//
//        Map<String, String> labelsService = new HashMap<>();
//        labelsService.put("name", podName);
//        metadataService.setLabels(labelsService);
//
//        // below needs to be automatic for onPremise
//        Map<String, String> annotations = new HashMap<>();
//        annotations.put("beta.cloud.google.com/backend-config", "{\"default\": \"orphea-backend-config\"}");
//        metadata.setAnnotations(annotations);
//
//        V1ServicePort servicePort = new V1ServicePort();
//        servicePort.setName(podName);
//        servicePort.setPort(8686);
//        servicePort.setTargetPort(new IntOrString(8686));
//
//        V1ServiceSpec serviceSpec = new V1ServiceSpec();
//        serviceSpec.setType("NodePort");
//        serviceSpec.setPorts(java.util.Collections.singletonList(servicePort));
//
//        Map<String, String> selector = new HashMap<>();
//        selector.put("name", podName);
//        serviceSpec.setSelector(selector);
//
//        V1Service service = new V1Service();
//        service.setMetadata(metadata);
//        service.setSpec(serviceSpec);
//
//        V1Service createdService = coreApi.createNamespacedService(namespace, service, null, null, null,null);
//
//        // Ingress
//        NetworkingV1Api networkingV1Api = new NetworkingV1Api(client);
//
//        String ingressName = "orphea-ingress";
//        String servicePath = "/api/jupyter/" + userId + "/*";
//
//        // Get the Ingress object
//        V1Ingress ingress = networkingV1Api.readNamespacedIngress(ingressName, namespace, null);
//
//        // Create a new Ingress rule
//        V1HTTPIngressPath httpIngressPath = new V1HTTPIngressPath();
//        V1IngressBackend ingressBackend = new V1IngressBackend();
//
//        V1IngressServiceBackend v1IngressServiceBackend = new V1IngressServiceBackend();
//        v1IngressServiceBackend.setName(podName);
//
//        V1ServiceBackendPort v1ServiceBackendPort = new V1ServiceBackendPort();
//        v1ServiceBackendPort.setNumber(8686);
//        v1IngressServiceBackend.setPort(v1ServiceBackendPort);
//
//        ingressBackend.setService(v1IngressServiceBackend);
//
//        httpIngressPath.setBackend(ingressBackend);
//        httpIngressPath.setPath(servicePath);
//        httpIngressPath.setPathType("ImplementationSpecific");
//
//        V1HTTPIngressRuleValue httpIngressRuleValue = new V1HTTPIngressRuleValue();
//        httpIngressRuleValue.setPaths(java.util.Collections.singletonList(httpIngressPath));
//
//        V1IngressRule ingressRule = new V1IngressRule();
//        ingressRule.setHost(System.getenv("dev.orphea.io"));  // TODO : Change this to auto
//        ingressRule.setHttp(httpIngressRuleValue);
//
//        // Add the new Ingress rule to the Ingress
//        List<V1IngressRule> rules = ingress.getSpec().getRules();
//        if (rules == null) {
//            rules = new ArrayList<>();
//        }
//        rules.add(ingressRule);
//        ingress.getSpec().setRules(rules);
//
//        // Update the Ingress object
//        try {
//            V1Ingress result = networkingV1Api.replaceNamespacedIngress(ingressName, namespace, ingress, null, null, null, null);
//            System.out.println(result);
//        } catch (ApiException e) {
//            System.err.println("Exception when calling NetworkingV1Api#createNamespacedIngress");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
//
//
//
//        return new ResponseEntity<>("Jupyter started", HttpStatus.OK);
//
//         */
//    }
//
//
//    @Operation(summary = "This api can be used to stop jupyter instance.")
//    @GetMapping("/stopJupyter")
//    ResponseEntity<Object> stopJupyter(Principal principal) throws Exception {
//
//        return new ResponseEntity<>("Jupyter disabled", HttpStatus.OK);
//
//        /*
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        ApiClient client = buildService.kubernetesClient();
//        Configuration.setDefaultApiClient(client);
//
//        CoreV1Api coreApi = new CoreV1Api(client);
//
//        NetworkingV1Api networkingV1Api = new NetworkingV1Api(client);
//
//        String namespace = "orphea";
//        String ingressName = "orphea-ingress";
//        String podName = "callisto-" + userId;
//
//
//        // Delete the Service
//
//        // Get a list of Services in the namespace
//        V1ServiceList serviceList = coreApi.listNamespacedService(namespace, null, null, null, null, null, null, null, null, null, null);
//
//        // Iterate through the list to check if the Service is present
//        for (V1Service service : serviceList.getItems()) {
//            if (service.getMetadata().getName().equals(podName)) {
//                // The Service is present then delete
//                coreApi.deleteNamespacedService(podName, namespace, null, null, null, null, null, null);
//                break;
//            }
//        }
//
//        // Delete the Pod
//
//        // List all the Pods in the namespace
//        V1PodList podList = coreApi.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
//
//        // Check if the Pod is present in the list
//        for (V1Pod pod : podList.getItems()) {
//            if (pod.getMetadata().getName().equals(podName)) {
//                coreApi.deleteNamespacedPod(podName, namespace, null, null, null, null, null, null);
//                break;
//            }
//        }
//
//        // Get the Ingress object
//        V1Ingress ingress = networkingV1Api.readNamespacedIngress(ingressName, namespace, null);
//
//        // Remove the Ingress rule that maps to the service
//        List<V1IngressRule> rules = ingress.getSpec().getRules();
//        if (rules != null) {
//            for (V1IngressRule rule : rules) {
//
//                System.out.println(rule.getHttp());
//
//                List<V1HTTPIngressPath> paths = rule.getHttp().getPaths();
//
//                System.out.println(paths);
//
//                if (paths != null) {
//                    for (V1HTTPIngressPath path : paths) {
//                        System.out.println(path.getBackend());
//                        System.out.println(path.getBackend().getService().getName());
//                        System.out.println(path.getBackend().getService().getPort());
//                        System.out.println(path.getPath());
//                        System.out.println(path.getPathType());
//                        if (path.getBackend().getService().getName().equals(podName)) {
//                            paths.remove(path);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        // Update the Ingress object
//        try {
//            V1Ingress result = networkingV1Api.replaceNamespacedIngress(ingressName, namespace, ingress, null, null, null, null);
//            System.out.println(result);
//        } catch (ApiException e) {
//            System.err.println("Exception when calling NetworkingV1Api#createNamespacedIngress");
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
//
//        return new ResponseEntity<>("Jupyter stopped", HttpStatus.OK);
//
//         */
//    }
}
