package io.movetodata.fractal.controllers;

import io.movetodata.fractal.library.models.*;
import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.Auth;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.AuthUser;
import io.movetodata.platform.library.models.GitConfigModel;
import io.movetodata.platform.library.repository.GitConfigRepository;
import io.movetodata.sharedutils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final GitService gitService;
    private final GitConfigRepository gitConfigRepository;

    private static List<DiffEntry> listDiff(Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        Repository repository = git.getRepository();
        final List<DiffEntry> diffs = git.diff().setOldTree(prepareTreeParser(repository, oldCommit)).setNewTree(prepareTreeParser(repository, newCommit)).call();

        return diffs;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
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
    public ResponseEntity<Object> listLog(Principal principal, @PathVariable("repositoryId") UUID repositoryId, @PathVariable("branch") String branch) throws Exception {
        List<GitLog> listLog = new ArrayList<>();
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isEditor(userId, repositoryId)) {
            throw new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        Iterable<RevCommit> logs = git.log().call();

        for (RevCommit log : logs) {
            listLog.add(new GitLog(log.toObjectId().getName(), log.getCommitterIdent().getName(), log.getCommitterIdent().getEmailAddress(), log.toString(), log.getFullMessage(), log.getCommitTime()));
        }
        return new ResponseEntity<>(listLog, HttpStatus.OK);
    }


    @Operation(summary = "Get commit details by repositoryId and branch.")
    @GetMapping("/{repositoryId}/commit/{commitId}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getCommitDetails(@AuthenticationPrincipal AuthUser user, @PathVariable("repositoryId") @Param("id") UUID repositoryId, @PathVariable("commitId") String commitId) throws Exception {
        List<GitDiffDTO> dtos = gitService.getCommitDetails(user.getId(), repositoryId, commitId);

        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Get commit details by repositoryId and branch.")
    @GetMapping("/{repositoryId}/file/{commitId}")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getFileCommitChanges(@AuthenticationPrincipal AuthUser user, @PathVariable("repositoryId") @Param("id") UUID repositoryId, @PathVariable("commitId") String commitId, @RequestParam("filePath") String filePath) throws Exception {
        FileDiffDTO dto = gitService.getCommitDetails(user.getId(), repositoryId, commitId, filePath);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Get File History")
    @GetMapping("/{repositoryId}/fileHistory")
    @PreAuthorize(Auth.VIEWER)
    public ResponseEntity<Object> getFileHistory(@AuthenticationPrincipal AuthUser user, @PathVariable("repositoryId") @Param("id") UUID repositoryId, @RequestParam("filePath") String filePath) throws Exception {
        List<GitLog> dto = gitService.getFileHistory(user.getId(), repositoryId, filePath);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "List of diff by repositoryId and branch.")
    @PostMapping("/{repositoryId}/{branch}/diff")
    public ResponseEntity<Object> listDiff(Principal principal, @PathVariable("repositoryId") UUID repositoryId, @PathVariable("branch") String branch, @RequestParam(name = "oldCommit") String oldCommit, @RequestParam(name = "newCommit") String newCommit) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isOwner(userId, repositoryId)) {
            throw  new UnauthorizedException();
        }

        Git git = gitService.getGitRepository(userId, repositoryId);

        List<DiffEntry> diffEntries = listDiff(git, oldCommit, newCommit);
        StringBuilder sb = new StringBuilder();

        for (DiffEntry entry : diffEntries) {
            sb.append(entry.getChangeType().toString()).append(" : ").append(entry.getOldPath().equals(entry.getNewPath()) ? entry.getNewPath() : entry.getOldPath() + " -> " + entry.getNewPath());

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
                formatter.setRepository(git.getRepository());
                formatter.format(entry);
            }
            sb.append("\n").append(output);
        }
        return ResponseEntity.ok().body(sb.toString());
    }

    @Operation(summary = "Get tree no.2 by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/tree")
    Set<RepositoryTreeNode> treeView(Principal principal, @PathVariable("repositoryId") UUID repositoryId, @PathVariable("branch") String branch) throws GitAPIException, IOException, InterruptedException {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, repositoryId)) {
            throw new UnauthorizedException("Access Denied to " + repositoryId);
        }

        Git git = gitService.getGitRepository(userId, repositoryId);
        if(!branch.equals(git.getRepository().getBranch())) {
            GitService.checkout(git, branch, false);
        }
        Repository repository = git.getRepository();
        RevWalk rw = new RevWalk(repository);
        try (TreeWalk tw = new TreeWalk(repository)) {
            RevCommit commitToCheck = rw.parseCommit(repository.resolve(Constants.HEAD));
            tw.addTree(commitToCheck.getTree());
            tw.addTree(new DirCacheIterator(repository.readDirCache()));
            tw.addTree(new FileTreeIterator(repository));
            tw.setRecursive(false);

            List<Folder> folders = new ArrayList<>();
            List<String> ignoredFiles = Stream.of(".ipynb_checkpoints", "__pycache__").collect(Collectors.toList());

            while (tw.next()) {
                if (ignoredFiles.stream().anyMatch(ignoredFile -> tw.getPathString().contains(ignoredFile)))
                    continue;

                Folder folder = new Folder();
                // Check if the current node is a file or a folder
                if (tw.isSubtree()) {
                    folder.setType("FOLDER");
                    folder.setLeaf(false);
                    tw.enterSubtree();
                } else {
                    folder.setType("FILE");
                    folder.setLeaf(true);
                }

                folder.setPath(tw.getPathString());
                folder.setDepth(tw.getDepth());
                folder.setName(tw.getNameString());
                folder.setMode(tw.getFileMode().toString());

                folders.add(folder);
            }
            return iterativeFileTreeExplorer2(folders);
        }

    }

    private Set<RepositoryTreeNode> iterativeFileTreeExplorer2(List<Folder> completeDirectoryList) {
        Set<RepositoryTreeNode> directoryForest = new HashSet<>();

        Stack<String> paths = new Stack<>();
        Stack<Set<RepositoryTreeNode>> toPopulate = new Stack<>();
        Stack<String> keys = new Stack<>();

        toPopulate.push(directoryForest);
        keys.push("0");

        for (Folder folder : completeDirectoryList) {
            while (!paths.isEmpty() && !folder.getPath().startsWith(paths.peek())) {
                paths.pop();
                toPopulate.pop();
                keys.pop();
            }


            Set<RepositoryTreeNode> whereToPut = toPopulate.peek();
            folder.setKey(keys.peek() + "-" + whereToPut.size());

            RepositoryTreeNode vertex = new RepositoryTreeNode();
            vertex.setId(folder.getPath());
            vertex.setName(folder.getName());
            vertex.setType(folder.isLeaf() ? ResourceType.FILE : ResourceType.FOLDER);
            vertex.setSubType(folder.isLeaf() ? ResourceSubtype.valueOf(Utils.getFileResourceSubType(folder.getName())) : ResourceSubtype.FOLDER);
            vertex.setPath(folder.getPath());

            vertex.setParent(keys.peek());

            Set<RepositoryTreeNode> children = new HashSet<>();
            paths.push(folder.getPath());
            toPopulate.push(children);
            keys.push(folder.getKey());
            vertex.setChildren(folder.isLeaf() ? new HashSet<>() : children);

            whereToPut.add(vertex);
        }
        return directoryForest;
    }

    @Operation(summary = "View file content by repositoryId and branch.")
    @GetMapping("/{repositoryId}/{branch}/viewFileContent/{workingTree}")
    Map<String, String> viewFileContents(Principal principal, @PathVariable("repositoryId") UUID repositoryId, @PathVariable("branch") String branch, @PathVariable("workingTree") String workingTree, @RequestParam("filePath") String filePath) throws GitAPIException, IOException, InterruptedException {

        UUID userId = userService.getUser(principal.getName()).getId();

        Map<String, String> fileContent = new HashMap<>(1);

        String base64File = gitService.getFileContent(userId, repositoryId, branch, filePath, workingTree);
        fileContent.put("fileContents.b64", base64File);

        return fileContent;
    }
}