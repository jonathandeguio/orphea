package io.orphea.kitab.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.connect.library.repository.LinkRepository;
import io.orphea.connect.library.services.UploadService;
import io.orphea.dataset.library.DTOs.CsvPreprocessingDTO;
import io.orphea.kitab.library.enums.ResourceStatus;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.enums.TransactionStatus;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.models.TransactionModel;
import io.orphea.kitab.library.repository.TransactionRepository;
import io.orphea.kitab.library.services.FileService;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.passport.exception.UnauthorizedException;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.service.AuthzService;
import io.orphea.passport.library.service.UserService;
import io.orphea.platform.library.services.PlatformConfigService;
import io.orphea.sharedutils.ActiveDisplay;
import io.orphea.sharedutils.Response.ErrorDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/folder")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class folderController {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final LinkRepository linkRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;
    private final ResourceService resourceService;
    private final FileService fileService;
    private final PlatformConfigService platformConfigService;
    private final UploadService uploadService;

    @Operation(summary = "Authz testing")
    @GetMapping("/authz/{Id}")
    public ResponseEntity<Object> getFolderByIdAuthz(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, Id)) {
            throw new UnauthorizedException();

        }

        return new ResponseEntity<>("Access Granted", HttpStatus.OK);
    }

    @Operation(summary = "It provides folder by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getFolderById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!resourceService.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);

        } else if (resourceService.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>("The Folder has been deleted.", HttpStatus.OK);
        }
        if (!authzService.isViewer(userId, Id)) {
            throw new UnauthorizedException();

        }

        return new ResponseEntity<>(resourceService.findById(Id).get(), HttpStatus.OK);
    }

    @Operation(summary = "This can be used to create folder.")
    @PostMapping("/create")
    public ResponseEntity<Object> newFolder(Principal principal, @RequestBody CreateFolderRequest newFolder) {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!resourceService.existsById(newFolder.getParent())) {
            return new ResponseEntity<>("parent id " + newFolder.parent + " does not exist!", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isEditor(userId, newFolder.getParent())) {
            throw new UnauthorizedException();

        }

        Optional<ResourceModel> parent = resourceService.findById(newFolder.getParent());

        if (parent.isPresent()) {
            List<ResourceModel> children = resourceService.getByParent(parent.get().getId());
            children.removeIf(child -> child.getStatus().equals(ResourceStatus.IN_TRASH));

            if (children.stream().anyMatch(folder -> newFolder.name.equals(folder.getName()))) {
                return new ResponseEntity<>("Same name folder exists!", HttpStatus.BAD_REQUEST);
            }
            ResourceModel newResource = ResourceModel.builder()
                    .name(newFolder.getName())
                    .project(parent.get().getProject())
                    .parent(parent.get().getId())
                    .type(newFolder.getType())
                    .subType(ResourceSubtype.NONE)
                    .status(ResourceStatus.ACTIVE)
                    .createdBy(user.getId())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .updatedBy(user.getId()).build();

            return new ResponseEntity<>(resourceService.save(newResource), HttpStatus.OK);

        }
        return new ResponseEntity<>(new ErrorDTO(404, "No Such Parent"), HttpStatus.OK);
    }

    @Operation(summary = "Get list of all children by parentId")
    @GetMapping("/children/{Id}/{status}")
    public ResponseEntity<Object> getChildren(Principal principal, @PathVariable("Id") UUID Id,
                                              @PathVariable("status") String status) {

        UUID userId = userService.getUser(principal.getName()).getId();


        ResourceModel parent = resourceService.findById(Id).get();
        // the slow load problem
        List<ResourceModel> allFolders = resourceService.getByParent(parent.getId());
        List<ResourceModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, ResourceStatus.valueOf(status.toUpperCase())); // note authz is
        // checked in
        // activeDisplay

        return ResponseEntity.ok().body(activeFolders);
    }

    @Operation(summary = "Move datasets/repos/folders")
    @GetMapping("/children/{Id}/moveto/{newParentId}")
    public ResponseEntity<Object> moveChildren(Principal principal, @PathVariable("Id") UUID Id,
                                               @PathVariable("newParentId") UUID newParent) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, Id)) {
            throw new UnauthorizedException();

        }

        if (!resourceService.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist!", HttpStatus.NOT_FOUND);
        }

        ResourceModel parentModel = resourceService.findById(newParent).get();

        ResourceModel resourceModel = resourceService.findById(Id).get();
        resourceModel.setParent(parentModel.getId());
        resourceService.save(resourceModel);

        return new ResponseEntity<>("Moved Successfully", HttpStatus.OK);
    }

    @Operation(summary = "Get list of all children by parentId but only folders")
    @GetMapping("/children/{Id}/folderOnly")
    public ResponseEntity<List<ResourceModel>> folderOnlyChildren(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();

        List<ResourceModel> allFolders = resourceService.getByParent(Id);
        List<ResourceModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, ResourceStatus.ACTIVE); // note authz is
        // checked in
        // activeDisplay

        List<ResourceModel> folderOnlyChildren = new ArrayList<>();

        for (ResourceModel resourceModel : activeFolders) {
            if (Objects.equals(resourceModel.getType(), "folder")) {
                folderOnlyChildren.add(resourceModel);
            }
        }

        return ResponseEntity.ok().body(folderOnlyChildren);
    }

    @Operation(summary = "Get list of all children by parentId but only dashboards")
    @GetMapping("/children/{Id}/dashboardsOnlyAndFolders")
    public ResponseEntity<List<ResourceModel>> dashboardsOnlyAndFolders(Principal principal,
                                                                        @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<ResourceModel> allFolders = resourceService.getByParent(Id);
        List<ResourceModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, ResourceStatus.ACTIVE); // note authz is
        // checked in
        // activeDisplay

        List<ResourceModel> activeDashboardsOnlyAndFolders = new ArrayList<>();

        for (ResourceModel resourceModel : activeFolders) {
            if (Objects.equals(resourceModel.getType(), "dashboard")) {
                activeDashboardsOnlyAndFolders.add(resourceModel);
            }

            if (Objects.equals(resourceModel.getType(), "folder")) {
                activeDashboardsOnlyAndFolders.add(resourceModel);
            }
        }

        return ResponseEntity.ok().body(activeDashboardsOnlyAndFolders);
    }

    @Operation(summary = "Get list of all children by parentId but only datasets ")
    @GetMapping("/children/{Id}/datasetsOnlyAndFolders")
    public ResponseEntity<List<ResourceModel>> datasetsOnlyChildren(Principal principal, @PathVariable("Id") UUID Id) {
        UUID userId = userService.getUser(principal.getName()).getId();
        List<ResourceModel> allFolders = resourceService.getByParent(Id);
        List<ResourceModel> activeFolders = activeDisplay.statusDisplay(userId, allFolders, ResourceStatus.ACTIVE);
        String defaultBranch = platformConfigService.getPlatformConfig().getDefaultBranch();

        List<ResourceModel> activeDatasetsOnlyAndFolders = new ArrayList<>();

        for (ResourceModel resourceModel : activeFolders) {
            if (Objects.equals(resourceModel.getType(), "dataset")) {

                TransactionModel transactionModel = transactionRepository
                        .findTransactionModelByDatasetIdAndBranch(resourceModel.getId(), defaultBranch);

                if (transactionModel.getStatus().equals(TransactionStatus.ACTIVE) && !linkRepository.existsByDatasetId(resourceModel.getId())) {
                    activeDatasetsOnlyAndFolders.add(resourceModel);
                }
            }

            if (Objects.equals(resourceModel.getType(), "folder")) {
                activeDatasetsOnlyAndFolders.add(resourceModel);
            }
        }

        return ResponseEntity.ok().body(activeDatasetsOnlyAndFolders);
    }

    @Operation(summary = "Upload files to a folder")
    @PostMapping("/uploadFile/{parentId}")
    public ResponseEntity<Object> uploadFile(Principal principal,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(name = "sheetName", required = false) String sheetName,
                                             @PathVariable("parentId") UUID parentId,
                                             @RequestParam(name = "fileName") String fileName,
                                             @RequestParam("csvPreprocessing") String csvPreprocessingDTO,
                                             @RequestParam(name = "description", required = false, defaultValue = "uploaded") String description) throws Exception {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!authzService.isEditor(userId, parentId)) {
            throw new UnauthorizedException();
        }

        if (!resourceService.existsById(parentId)) {
            return new ResponseEntity<>("parent id " + parentId + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, parentId)) {
            throw new UnauthorizedException();

        }
        ObjectMapper objectMapper = new ObjectMapper();
        CsvPreprocessingDTO csvPreprocessing = csvPreprocessingDTO != null && !csvPreprocessingDTO.isEmpty() ? objectMapper.readValue(
                csvPreprocessingDTO, CsvPreprocessingDTO.class) : null;

        List<String> sheetNames = (sheetName != null && !sheetName.isEmpty())
                ? List.of(sheetName.split(","))
                : List.of("");
        ResourceModel resourceModel = uploadService.uploadFileWrapper(parentId, file, fileName, description, sheetNames, csvPreprocessing, user.getId());
        return ResponseEntity.ok().body(resourceModel);
    }

    @Operation(summary = "Upload files to a folder")
    @GetMapping("/getFile/{fileId}")
    public ResponseEntity<Object> getFile(Principal principal,
                                          @PathVariable("fileId") UUID fileId
    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, fileId)) {
            throw new UnauthorizedException();

        }

        Map<String, Object> mp = fileService.getFileInputStream(fileId);

        return ResponseEntity.ok()
                .headers((HttpHeaders) mp.get("headers"))
                .body(mp.get("stream"));
    }

    @Getter
    @Setter
    private static class CreateFolderRequest {
        public String name;
        public UUID parent;
        public ResourceType type;
        public String description;
    }
}
