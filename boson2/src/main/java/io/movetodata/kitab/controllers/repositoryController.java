package io.movetodata.kitab.controllers;

import io.movetodata.fractal.library.models.GitCommit;
import io.movetodata.fractal.library.services.GitService;
import io.movetodata.kitab.library.models.FolderModel;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.FolderRepository;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.passport.security.UserPrincipal;
import io.movetodata.sharedUtils.ActiveDisplay;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/repository")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class repositoryController {

    private final UserService userService;

    private final FolderRepository folderRepository;
    private final GitService gitServiceRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "It provides list of all repository.")
    @GetMapping("/all")
    public ResponseEntity<Object> getRepository(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> all = folderRepository.getByType("repository");
        List<FolderModel> activeRepositories = activeDisplay.statusDisplay(userId, all, "active"); // note authz is
                                                                                                   // checked in
                                                                                                   // activeDisplay

        return ResponseEntity.ok().body(activeRepositories);
    }

    @Operation(summary = "This can be used to create repository.")
    @PostMapping("/{templateType}/create")
    public ResponseEntity<?> newRepository(Principal principal,
            @Valid @RequestBody FolderModel newRepository,
            @PathVariable("templateType") String templateType) throws Exception {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(newRepository.parent)) {
            return new ResponseEntity<>("parent id " + newRepository.parent + " does not exist!", HttpStatus.NOT_FOUND);
        }

        if (!authzService.isOwner(userId, newRepository.getParent())) {
            return new ResponseEntity<>("Access Denied to " + newRepository.getParent(), HttpStatus.FORBIDDEN);
        }

        List<FolderModel> children = folderRepository.getByParent(newRepository.parent);

        children.removeIf(child -> child.getStatus().equals("inTrash"));

        if (children.stream().anyMatch(repository -> newRepository.name.equals(repository.name))) {
            return new ResponseEntity<>("Same name repository exists!", HttpStatus.BAD_REQUEST);

        }
        newRepository.type = "repository";
        newRepository.status = "active";

        FolderModel createdRepository = folderRepository.save(newRepository);

        GitCommit gitCommit = new GitCommit();
        gitCommit.setMessage("Initial Repository");
        gitCommit.setEmail(userService.getUser(principal.getName()).getEmail());
        gitCommit.setUsername(userService.getUser(principal.getName()).getUsername());

        gitServiceRepository.createRepository(userService.getUser(principal.getName()).getId(), gitCommit,
                createdRepository.id.toString(), templateType);

        return new ResponseEntity<>(createdRepository, HttpStatus.OK);
    }

    @Operation(summary = "It provides repository by Name")
    @GetMapping("/{name}/name")
    public ResponseEntity<Object> getRepositoryByName(Principal principal, @PathVariable("name") String name) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allRepository = folderRepository.getByType("repository");
        List<FolderModel> activeRepositories = activeDisplay.statusDisplay(userId, allRepository, "active"); // note
                                                                                                             // authz is
                                                                                                             // checked
                                                                                                             // in
                                                                                                             // activeDisplay

        for (FolderModel repository : activeRepositories) {
            if (repository.getName().equals(name)) {
                if (repository.getStatus().equals("active")) {
                    return new ResponseEntity<>(repository, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response.okResponse("The repository has been deleted."), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>("repository with name " + name + " does not exist!", HttpStatus.NOT_FOUND);

    }

    @Operation(summary = "It provides repository by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getRepositoryById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        } else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The repository has been deleted."), HttpStatus.OK);
        }
        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }
}
