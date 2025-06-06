package io.bosler.kitab.controllers;

import io.bosler.ignite.library.repository.LinkRepository;
import io.bosler.kitab.library.models.FolderModel;
import io.bosler.kitab.library.models.ResourceViewsModel;
import io.bosler.kitab.library.models.TransactionModel;
import io.bosler.kitab.library.repository.FolderRepository;
import io.bosler.kitab.library.repository.ResourceViewsRepository;
import io.bosler.kitab.library.repository.TransactionRepository;
import io.bosler.passport.library.service.AuthzService;
import io.bosler.passport.library.service.UserService;
import io.bosler.passport.security.UserPrincipal;
import io.bosler.sharedUtils.ActiveDisplay;
import io.bosler.sharedUtils.Response.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/folder")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class folderController {

    private final UserService userService;
    private final FolderRepository folderRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final TransactionRepository transactionRepository;
    private final LinkRepository linkRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;

    private final OkResponse response = new OkResponse();

    @Operation(summary = "Authz testing")
    @GetMapping("/authz/{Id}")
    public ResponseEntity<Object> getFolderByIdAuthz(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>("Access Granted", HttpStatus.OK);
    }

    @Operation(summary = "It provides list of all folders.")
    @GetMapping("/all")
    public ResponseEntity<Object> getFolders(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByType("folder");
        List<FolderModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, "active"); // note authz is
                                                                                                     // checked in
                                                                                                     // activeDisplay

        return ResponseEntity.ok().body(activeFolders);
    }

    @Operation(summary = "It provides folder by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getFolderById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);

        } else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The Folder has been deleted."), HttpStatus.OK);
        }
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(folderRepository.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create folder.")
    @PostMapping("/create")
    public ResponseEntity<Object> newFolder(Principal principal, @Valid @RequestBody FolderModel newFolder) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(newFolder.parent)) {
            return new ResponseEntity<>("parent id " + newFolder.parent + " does not exist!", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, newFolder.getParent())) {
            return new ResponseEntity<>("Access Denied to " + newFolder.getParent(), HttpStatus.FORBIDDEN);
        }

        List<FolderModel> children = folderRepository.getByParent(newFolder.parent);
        children.removeIf(child -> child.getStatus().equals("inTrash"));

        if (children.stream().anyMatch(folder -> newFolder.name.equals(folder.name))) {
            return new ResponseEntity<>("Same name folder exists!", HttpStatus.BAD_REQUEST);
        }
        newFolder.type = "folder";
        newFolder.status = "active";
        newFolder.setUpdatedAt(new Date());
        newFolder.setCreatedBy(userId);
        newFolder.setUpdatedBy(userId);

        return new ResponseEntity<>(folderRepository.save(newFolder), HttpStatus.OK);
    }

    @Operation(summary = "Get list of all children by parentId")
    @GetMapping("/children/{Id}/{status}")
    public ResponseEntity<Object> getByParent(Principal principal, @PathVariable("Id") UUID Id,
            @PathVariable("status") String status) {

        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByParent(Id);
        List<FolderModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, status); // note authz is
                                                                                                     // checked in
                                                                                                     // activeDisplay

        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        return ResponseEntity.ok().body(activeFolders);
    }

    @Operation(summary = "Move datasets/repos/folders")
    @GetMapping("/children/{Id}/moveto/{newParentId}")
    public ResponseEntity<Object> moveChildren(Principal principal, @PathVariable("Id") UUID Id,
                                              @PathVariable("newParentId") UUID newParent) {

        UUID userId = userService.getUser(principal.getName()).id;
        
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        }

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.save(resourceViewsModel);

        FolderModel folderModel = folderRepository.findById(Id).get();
        folderModel.setParent(newParent);
        folderRepository.save(folderModel);
        
        return new ResponseEntity<>("Moved Successfully", HttpStatus.OK);
    }

    @Operation(summary = "Get list of all children by parentId but only folders")
    @GetMapping("/children/{Id}/folderOnly")
    public ResponseEntity<List<FolderModel>> folderOnlyChildren(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByParent(Id);
        List<FolderModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, "active"); // note authz is
                                                                                                     // checked in
                                                                                                     // activeDisplay

        List<FolderModel> folderOnlyChildren = new ArrayList<>();

        for (FolderModel folderModel : activeFolders) {
            if (Objects.equals(folderModel.getType(), "folder")) {
                folderOnlyChildren.add(folderModel);
            }
        }

        return ResponseEntity.ok().body(folderOnlyChildren);
    }

    @Operation(summary = "Get list of all children by parentId but only dashboards")
    @GetMapping("/children/{Id}/dashboardsOnlyAndFolders")
    public ResponseEntity<List<FolderModel>> dashboardsOnlyAndFolders(Principal principal,
            @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByParent(Id);
        List<FolderModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, "active"); // note authz is
                                                                                                     // checked in
                                                                                                     // activeDisplay

        List<FolderModel> activeDashboardsOnlyAndFolders = new ArrayList<>();

        for (FolderModel folderModel : activeFolders) {
            if (Objects.equals(folderModel.getType(), "dashboard")) {
                activeDashboardsOnlyAndFolders.add(folderModel);
            }

            if (Objects.equals(folderModel.getType(), "folder")) {
                activeDashboardsOnlyAndFolders.add(folderModel);
            }
        }

        return ResponseEntity.ok().body(activeDashboardsOnlyAndFolders);
    }

    @Operation(summary = "Get list of all children by parentId but only datasets ")
    @GetMapping("/children/{Id}/datasetsOnlyAndFolders")
    public ResponseEntity<List<FolderModel>> datasetsOnlyChildren(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByParent(Id);
        List<FolderModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, "active"); // note authz is
                                                                                                     // checked in
                                                                                                     // activeDisplay

        List<FolderModel> activeDatasetsOnlyAndFolders = new ArrayList<>();

        for (FolderModel folderModel : activeFolders) {
            if (Objects.equals(folderModel.getType(), "dataset")) {

                List<TransactionModel> transactions = transactionRepository
                        .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(folderModel.getId(), "master");
                List<TransactionModel> activeTransactions = activeDisplay.activeDisplayTransactionModel(userId,
                        transactions); // note authz is checked in activeDisplay

                if (activeTransactions.size() == 0 && !linkRepository.existsByDatasetId(folderModel.getId())) {
                    activeDatasetsOnlyAndFolders.add(folderModel);
                }
            }

            if (Objects.equals(folderModel.getType(), "folder")) {
                activeDatasetsOnlyAndFolders.add(folderModel);
            }
        }

        return ResponseEntity.ok().body(activeDatasetsOnlyAndFolders);
    }

    @Operation(summary = "It provides folder by Name")
    @GetMapping("/{name}/name")
    public ResponseEntity<Object> getFolderByName(Principal principal, @PathVariable("name") String name) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> allFolders = folderRepository.getByType("folder");
        for (FolderModel folder : allFolders) {
            if (folder.getName().equals(name)) {
                if (folder.getStatus().equals("active") && authzService.isViewer(userId, folder.getId())) {
                    return new ResponseEntity<>(folder, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response.okResponse("The Folder has been deleted."), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>("Folder named " + name + " not found.", HttpStatus.NOT_FOUND);
    }
}
