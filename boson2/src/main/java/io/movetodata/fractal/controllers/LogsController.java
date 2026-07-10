package io.movetodata.fractal.controllers;

import io.movetodata.fractal.library.models.Folder;
import io.movetodata.fractal.library.models.GitLog;
import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.UserPrincipal;
import io.movetodata.sharedUtils.Response.NoSuchElementFoundException;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.*;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/fractal")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fractal", description = "Code Repository management service endpoints")
public class LogsController {

    private final UserService userService;
    private final AuthzService authzService;
    private final GitService gitServiceFractal;
    private final FolderRepository folderRepository;
    private final GitBranchController gitBranchController;
    private final OkResponse response = new OkResponse();

    private static List<DiffEntry> listDiff(Repository repository,
                                            Git git,
                                            String oldCommit,
                                            String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
        return diffs;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository,
                                                          String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //no inspection Duplicates

        CanonicalTreeParser treeParser;

        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();
            return treeParser;
        }
    }

    @Operation(summary = "Get logs by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/logs")
    public ResponseEntity<Object> listLog(Principal principal,
                                                @PathVariable("repositoryId") String repositoryId,
                                                @PathVariable("branch") String branch) throws Exception {

        List<GitLog> listLog = new ArrayList<>();
        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
        }

        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));

        if (!authzService.isOwner(userId, folderModel.getParent())) {
            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
        }

        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);

        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
            Git git = new Git(repository);
            git.checkout().setName(branch).call();
            Iterable<RevCommit> logs = git.log().call();

            for (RevCommit log : logs) {
                listLog.add(new GitLog(
                        log.toObjectId().getName(),
                        log.getCommitterIdent().getName(),
                        log.getCommitterIdent().getEmailAddress(),
                        log.toString(),
                        log.getFullMessage()
                ));
            }
            repository.close();
            return new ResponseEntity<>(listLog, HttpStatus.OK);
        } else {
            throw new NoSuchElementFoundException("Repository or Branch does not exist");
        }
    }

    @Operation(summary = "List of diff by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/diff")
    public ResponseEntity<Object> listDiff(Principal principal,
                                           @PathVariable("repositoryId") String repositoryId,
                                           @PathVariable("branch") String branch,
                                           @RequestParam(name = "oldCommit") String oldCommit,
                                           @RequestParam(name = "newCommit") String newCommit) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(UUID.fromString(repositoryId))) {
            return new ResponseEntity<>("parent id " + repositoryId + " does not exist.", HttpStatus.NOT_FOUND);
        }

        FolderModel folderModel = folderRepository.getById(UUID.fromString(repositoryId));

        if (!authzService.isOwner(userId, folderModel.getParent())) {
            return new ResponseEntity<>("Access Denied to " + folderModel.getParent(), HttpStatus.FORBIDDEN);
        }

        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);

        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {
            Git git = new Git(repository);
            git.checkout().setName(branch).call();
            List<DiffEntry> diffEntries = listDiff(repository, git, oldCommit, newCommit);
            StringBuilder sb = new StringBuilder();

            for (DiffEntry entry : diffEntries) {
                sb.append(entry.getChangeType().toString())
                        .append(" : ")
                        .append(
                                entry.getOldPath().equals(entry.getNewPath()) ? entry.getNewPath() : entry.getOldPath()
                                        + " -> " + entry.getNewPath()
                        );

                OutputStream output = new OutputStream() {
                    final StringBuilder builder = new StringBuilder();

                    @Override
                    public void write(int b) {
                        builder.append((char) b);
                    }

                    @Override
                    public String toString() {
                        return this.builder.toString();
                    }
                };

                try (DiffFormatter formatter = new DiffFormatter(output)) {
                    formatter.setRepository(repository);
                    formatter.format(entry);
                }
                sb.append("\n").append(output);
            }
            return new ResponseEntity<>(response.okResponse(sb.toString()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Repository with Id " + repositoryId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);

        }
    }

    @SneakyThrows
    @Operation(summary = "Get tree no.2 by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/tree")
    List<Object> treeView(Principal principal,
                           @PathVariable("repositoryId") String repositoryId,
                           @PathVariable("branch") String branch
    ) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);
        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {

//            new Git(repository).checkout().setName(branch).call();

            RevWalk rw = new RevWalk(repository);
            try (TreeWalk tw = new TreeWalk(repository)) {

                RevCommit commitToCheck = rw.parseCommit(repository.resolve(Constants.HEAD));
                tw.addTree(commitToCheck.getTree());
                tw.addTree(new DirCacheIterator(repository.readDirCache()));

                tw.addTree(new FileTreeIterator(repository));
                tw.setRecursive(false);

                List<Folder> folders = new ArrayList<>();

                while (tw.next()) {
                    Folder folder = new Folder();

                    if (tw.isSubtree()) {
                        folder.setType("folder");
                        tw.enterSubtree();
                    } else {
                        folder.setType("file");
                    }

                    folder.setPath(tw.getPathString());
                    folder.setDepth(tw.getDepth());
                    folder.setName(tw.getNameString());
                    folder.setMode(tw.getFileMode().toString());

                    folders.add(folder);
                }
                return iterativeFileTreeExplorer2(folders);
            }
        } else {
            throw new NoSuchElementFoundException("Repository or branch does not Exist");
        }
    }

    private List<Object> iterativeFileTreeExplorer2(List<Folder> completeDirectoryList) {
        List<Object> directoryForest = new ArrayList<>();

        Stack<String> paths = new Stack<>();
        Stack<List<Object>> toPopulate = new Stack<>();
        Stack<String> keys = new Stack<>();

        toPopulate.push(directoryForest);
        keys.push("0");

        for (Folder folder : completeDirectoryList) {

            while (!paths.isEmpty() && !folder.getPath().startsWith(paths.peek())) {
                paths.pop();
                toPopulate.pop();
                keys.pop();
            }

            List<Object> whereToPut = toPopulate.peek();
            folder.setLeaf(Objects.equals(folder.getType(), "file"));
            folder.setKey(keys.peek() + "-" + whereToPut.size());

            Map<String, Object> vertex = new TreeMap<>(((Comparator<String>) String::compareTo).reversed());
            vertex.put("key", folder.getKey());
            vertex.put("name", folder.getName());
            vertex.put("type", folder.isLeaf() ? "file" : "folder");
            vertex.put("path", folder.getPath());
            vertex.put("depth", folder.getDepth());
            vertex.put("isDirectory", !folder.isLeaf());

            String folderPath = folder.getPath();
            if (folderPath.contains("/")) {
                int index = folderPath.lastIndexOf("/");
                String parent = folderPath.substring(0, index);
                index = parent.lastIndexOf("/");
                if (index != -1) {
                    parent = parent.substring(index + 1);
                }
                vertex.put("parent", parent);
            } else {
                vertex.put("parent", null);
            }

            List<Object> children = new ArrayList<>();
            paths.push(folder.getPath());
            toPopulate.push(children);
            keys.push(folder.getKey());
            vertex.put("children", children);

            if (folder.getName().startsWith(".")) {
                vertex.put("isHidden", true);
            } else {
                vertex.put("isHidden", false);
            }

            whereToPut.add(vertex);
        }
        return directoryForest;
    }

    @SneakyThrows
    @Operation(summary = "View file content by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/viewFileContent")
    Map<String, String> viewFileContents(Principal principal,
                                         @PathVariable("repositoryId") String repositoryId,
                                         @PathVariable("branch") String branch,
                                         @RequestParam("filePath") String filePath) {

        UUID userId = userService.getUser(principal.getName()).id;
        Repository repository = gitServiceFractal.getRepository(userId + "/" + repositoryId, branch);

        if (repository.getObjectDatabase().exists() && gitBranchController.branchExists(principal, repositoryId, branch)) {

            new Git(repository).checkout().setName(branch).call();

//            ObjectId lastCommitId = repository.resolve(Constants.HEAD);

            // a RevWalk allows to walk over commits based on some filtering that is defined
//            try (RevWalk revWalk = new RevWalk(repository)) {
//                RevCommit commit = revWalk.parseCommit(lastCommitId);
//                // and using commits tree find the path
//                RevTree tree = commit.getTree();
//
//                // now try to find a specific file
//                try (TreeWalk treeWalk = new TreeWalk(repository)) {
//                    treeWalk.addTree(tree);
//                    treeWalk.setRecursive(true);
//                    treeWalk.setFilter(PathFilter.create(filePath));
//                    if (!treeWalk.next()) {
//                        throw new IllegalStateException("Did not find expected file " + filePath);
//                    }

            File file = new File(System.getenv("GIT_CLONED_PATH") + "/" +
                    userId + "/" + repositoryId + "/" + filePath);
            Scanner sc = new Scanner(file).useDelimiter("\\A");
            String content = "";
            if (sc.hasNext()) {
                content = sc.next();
            }

//                    ObjectId objectId = treeWalk.getObjectId(0);
//                    ObjectLoader loader = repository.open(objectId);

            String binaryAsAString = Base64.encodeBase64String(content.getBytes());
            Map<String, String> fileContent = new HashMap<>();
            fileContent.put("fileContents.b64", binaryAsAString);
//                    fileContent.put("size", String.valueOf(loader.getSize()));
//                    fileContent.put("type", String.valueOf(loader.getType()));
//                    fileContent.put("objectId", objectId.getName());
//                    fileContent.put("path", treeWalk.getPathString());
//                    fileContent.put("mode", treeWalk.getFileMode().toString());
//                    fileContent.put("name", treeWalk.getNameString());

            return fileContent;
        }
//            }
        else {
            throw new NoSuchElementFoundException("Repository or branch does not Exist");
        }
    }
}
