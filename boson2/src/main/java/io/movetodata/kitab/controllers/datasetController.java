package io.movetodata.kitab.controllers;

import io.movetodata.bob.library.models.BuildSpecification;
import io.movetodata.bob.library.repository.BuildSpecificationsRepository;
import io.movetodata.docket.library.models.Tags;
import io.movetodata.docket.library.repository.TagRepository;
import io.movetodata.ignite.library.repository.LinkRepository;
import io.movetodata.kitab.library.models.*;
import io.movetodata.kitab.library.repository.*;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
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
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/dataset")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class datasetController {

    private final UserService userService;
    private final FolderRepository folderRepository;
    private final TransactionRepository transactionRepository;
    private final DatasetRepository datasetRepository;
    private final BranchRepository branchRepository;
    private final ResourceViewsRepository resourceViewsRepository;
    private final TagRepository tagRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final LinkRepository linkRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;
    private final OkResponse response = new OkResponse();

    @Operation(summary = "It provides list of all dataset.")
    @GetMapping("/all")
    // public ResponseEntity<List<FolderModel>> getDataset(Principal principal) {
    public ResponseEntity<Object> getDataset(Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        List<FolderModel> all = folderRepository.getByType("dataset");
        List<FolderModel> activeDatasets = activeDisplay.statusDisplay(userId, all, "active"); // note authz is checked
        // in activeDisplay

        return ResponseEntity.ok().body(activeDatasets);
    }

    @Operation(summary = "It provides dataset by Name")
    @GetMapping("/{name}/name")
    public ResponseEntity<Object> DatasetByName(Principal principal, @PathVariable("name") String name) {
        UUID userId = userService.getUser(principal.getName()).id;

        // if
        // (!authzService.canRead(UUID.fromString("64de0cf7-4a79-4bcc-a2f7-0f5c04124b53"),
        // UUID.fromString("64de0cf7-4a79-4bcc-a2f7-0f5c04124b53"), "dataset/read")){
        // return new ResponseEntity<>("Access to the resource " + userId + " not
        // allowed", HttpStatus.FORBIDDEN);
        // }

        List<FolderModel> allDatasets = folderRepository.getByType("dataset");
        List<FolderModel> activeDatasets = activeDisplay.statusDisplay(userId, allDatasets, "active"); // note authz is
        // checked in
        // activeDisplay

        for (FolderModel dataset : activeDatasets) {
            if (dataset.getName().equals(name)) {
                if (dataset.getStatus().equals("active") && authzService.isViewer(userId, dataset.getId())) {
                    return new ResponseEntity<>(dataset, HttpStatus.OK);
                } else {

                    return new ResponseEntity<>(response.okResponse("dataset was searched successfully by name"),
                            HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>("Dataset named " + name + " not found.", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "This can be used to create dataset.")
    @PostMapping("/create")
    public ResponseEntity<Object> newDataset(Principal principal, @Valid @RequestBody FolderModel newDataset, @RequestParam(name = "source", required = false) String source) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(newDataset.parent)) {
            return new ResponseEntity<>("parent id " + newDataset.parent + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isOwner(userId, newDataset.getParent())) {
            return new ResponseEntity<>("Access Denied to " + newDataset.getParent(), HttpStatus.FORBIDDEN);
        }

        List<FolderModel> children = folderRepository.getByParent(newDataset.parent);
        children.removeIf(child -> child.getStatus().equals("inTrash"));

        if (children.stream().anyMatch(dataset -> newDataset.name.equals(dataset.name))) {
            return new ResponseEntity<>("Dataset with name " + newDataset.name + " already exists!",
                    HttpStatus.BAD_REQUEST);
        }
        newDataset.type = "dataset";
        newDataset.status = "active";
        newDataset.setCreatedBy(userId);
        newDataset.setUpdatedBy(userId);
        newDataset.setCreatedAt(new Date());

        FolderModel folderDataset = folderRepository.save(newDataset);

        DatasetModel dataset = new DatasetModel();
        dataset.setId(folderDataset.id);

        if (source == null) {
            dataset.setType(null);
        } else if (source.equals("pipeline")) {
            dataset.setType("parquet");
        } else {
            dataset.setType(null);
        }

        datasetRepository.saveAndFlush(dataset);

        // Create branch - not need, it is done in the upload
//        BranchModel branchModel = new BranchModel();
//
//        branchModel.setDatasetId(dataset.getId());
//        branchModel.setBranch("master");
//        branchModel.setCreatedBy(userId);
//        branchModel.setCreatedAt(new Date());
//
//        branchRepository.saveAndFlush(branchModel);

        return new ResponseEntity<>(folderDataset, HttpStatus.OK);

    }

    @Operation(summary = "It provides dataset by Id")
    @GetMapping("/{Id}")
    public ResponseEntity<Object> getDatasetById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }
        FolderModel dataset = folderRepository.findById(Id).orElse(null);

        ResourceViewsModel resourceViewsModel = new ResourceViewsModel();

        resourceViewsModel.setResourceId(Id);
        resourceViewsModel.setAction("view");
        resourceViewsModel.setViewedBy(userId);

        resourceViewsRepository.saveAndFlush(resourceViewsModel);

        if (dataset == null || dataset.getType().equals("dataset")) {
            return new ResponseEntity<>("Not a dataset", HttpStatus.BAD_REQUEST);
        } else if (dataset.getStatus().equals("inTrash")) {
            return new ResponseEntity<>(response.okResponse("The dataset has been deleted."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dataset, HttpStatus.OK);
        }
    }

    @Operation(summary = "It provides dataset by Id")
    @GetMapping("/{Id}/exist")
    public ResponseEntity<Object> datasetExistById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!datasetRepository.existsById(Id)) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        //TODO Auth
//        if (!authzService.isViewer(userId, Id)) {
//            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
//        }
        if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        return new ResponseEntity<>(datasetRepository.existsById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It provides dataset by Id")
    @PostMapping("/byIds")
    public ResponseEntity<Object> getDatasetByIds(Principal principal, @RequestBody List<UUID> Ids) {
        UUID userId = userService.getUser(principal.getName()).id;

        Set<UUID> set = new HashSet<>(Ids);
        Ids.clear();
        Ids.addAll(set);

        List<FolderModel> responseIds = new ArrayList<>();
        List<FolderModel> activeDatasets = activeDisplay.statusDisplay(userId, responseIds, "active"); // note authz is
        // checked in
        // activeDisplay

        for (UUID Id : Ids) {

            if (!folderRepository.existsById(Id)) {
                return new ResponseEntity<>("Id " + Id + " does not exist.", HttpStatus.NOT_FOUND);

            }
            else if (folderRepository.findById(Id).get().getStatus().equals("inTrash")) {
                continue;
//                return new ResponseEntity<>(response.okResponse("The dataset has been deleted."), HttpStatus.OK);
            }

            responseIds.add(folderRepository.findById(Id).get());
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);
    }

    @Operation(summary = "It provides all transactions")
    @GetMapping("/allTransactions")
    public ResponseEntity<Object> getAllTransactions(Principal principal) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<TransactionModel> transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        List<TransactionModel> activeTransactions = activeDisplay.activeDisplayTransactionModel(userId, transactions); // note
        // authz
        // is
        // checked
        // in
        // activeDisplay

        return new ResponseEntity<>(activeTransactions, HttpStatus.OK);
    }

    @Operation(summary = "It provides all branches belong to dataset")
    @GetMapping("/{datasetId}/branches")
    public ResponseEntity<Object> branches(Principal principal, @PathVariable("datasetId") UUID datasetId) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<BranchModel> branchModels = branchRepository.findAllBranchModelByDatasetId(datasetId);
        // List<BranchModel> activeBranchModel =
        // activeDisplay.activeDisplayBranchModel(userId, branchModels); // note authz
        // is checked in activeDisplay

        return new ResponseEntity<>(branchModels, HttpStatus.OK);
    }

    @Operation(summary = "It provides all transactions by datasetId and branch")
    @GetMapping("/{datasetId}/{branch}/transactions")
    public ResponseEntity<Object> getTransactions(Principal principal, @PathVariable("datasetId") UUID datasetId,
                                                  @PathVariable("branch") String branch) {
        UUID userId = userService.getUser(principal.getName()).id;

        List<TransactionModel> transactions = transactionRepository
                .findAllByDatasetIdAndBranchOrderByCreatedAtDesc(datasetId, branch);
        // List<TransactionModel> activeTransactions =
        // activeDisplay.activeDisplayTransactionModel(userId, transactions); // note
        // authz is checked in activeDisplay

        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Operation(summary = "It provides all transactions by datasetId and branch")
    @GetMapping("/{datasetId}/{branch}/{status}/existsTransactions")
    public ResponseEntity<Object> existsTransactions(Principal principal, @PathVariable("datasetId") UUID datasetId,
                                                     @PathVariable("branch") String branch,
                                                     @PathVariable("status") String status) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        boolean value = transactionRepository
                .existsTransactionModelByDatasetIdAndBranchAndStatusOrderByCreatedAtDesc(datasetId, branch, status);

        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(summary = "It provides gives boolean for Build Specification if exists or not.")
    @GetMapping("/{datasetId}/{branch}/buildSpecification")
    public ResponseEntity<Object> getBuildSpecification(Principal principal,
                                                        @PathVariable("datasetId") UUID datasetId,
                                                        @PathVariable("branch") String branch) {
        if (Objects.equals(datasetId, "undefined") || Objects.equals(branch, "undefined")) {
            return new ResponseEntity<>("Not valid datasetId or branch", HttpStatus.BAD_REQUEST);
        }

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        BuildSpecification value = buildSpecificationsRepository.findByDatasetIdAndBranch(datasetId, branch);

        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(summary = "It provides buildSpecification")
    @GetMapping("/{datasetId}/{branch}/existsBuildSpecification")
    public ResponseEntity<Object> existsBuildSpecification(Principal principal,
                                                           @PathVariable("datasetId") UUID datasetId,
                                                           @PathVariable("branch") String branch) {
        if (Objects.equals(datasetId, "undefined") || Objects.equals(branch, "undefined")) {
            return new ResponseEntity<>("Not valid datasetId or branch", HttpStatus.BAD_REQUEST);
        }

        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        boolean value = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(datasetId, branch);

        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(summary = "It provides gives boolean for Build Specification if exists or not by Repository, branch and script path")
    @PostMapping("/getBuildSpecificationByScript")
    public ResponseEntity<Object> getBuildSpecificationByScript(
            @RequestBody GetByRepositoryProperties getByRepositoryProperties) {

        return new ResponseEntity<>(
                buildSpecificationsRepository.findByRepositoryAndBranchAndScriptPath(
                        getByRepositoryProperties.getRepositoryId(),
                        getByRepositoryProperties.getBranch(), getByRepositoryProperties.getScriptPath()),
                HttpStatus.OK);
    }

    @Operation(summary = "It provides buildSpecification by Repository, branch and script path")
    @PostMapping("/existsBuildSpecificationByScript")
    public ResponseEntity<Object> existsBuildSpecificationByScript(
            @RequestBody GetByRepositoryProperties getByRepositoryProperties) {

        boolean value = buildSpecificationsRepository.existsBuildSpecificationByRepositoryAndBranchAndScriptPath(
                getByRepositoryProperties.getRepositoryId(),
                getByRepositoryProperties.getBranch(), getByRepositoryProperties.getScriptPath());

        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(summary = "It provides repositoryId by datasetId")
    @GetMapping("/{datasetId}/{branch}/repository")
    public ResponseEntity<Object> repository(Principal principal, @PathVariable("datasetId") UUID datasetId,
                                             @PathVariable("branch") String branch) {
        UUID userId = userService.getUser(principal.getName()).id;

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(datasetId, branch)) {
            UUID repositoryId = buildSpecificationsRepository
                    .findByDatasetIdAndBranch(datasetId, branch).getRepository();

            Map<String, Object> map = Map.of(
                    "repositoryId", repositoryId,
                    "branch", branch);

            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " or branch" + branch + " does not exist",
                    HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Provides path for dataset files")
    @GetMapping(path = "/physicalPath/{datasetId}")
    ResponseEntity<Object> buildPhysicalPath(Principal principal, @PathVariable(name = "datasetId") UUID datasetId) {
        try {
            if (!folderRepository.existsById(datasetId)) {
                return new ResponseEntity<>("Id " + datasetId + " does not exist!", HttpStatus.NOT_FOUND);
            }

            UUID userId = userService.getUser(principal.getName()).id;
            if (!authzService.isViewer(userId, datasetId)) {
                return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
            }
            String endpoint;
            if (System.getenv("BACKING_FS").equals("s3")) {
                endpoint = "s3a://movetodata/datasets";
            } else if (System.getenv("BACKING_FS").equals("gs")) {
                endpoint="gs://" + System.getenv("GS_BUCKET") + "/movetodata/datasets";
            } else if (System.getenv("BACKING_FS").equals("hdfs")) {
                endpoint = System.getenv("HDFS_ENDPOINT") + "/movetodata/datasets";
            } else {
                throw new Exception("No Backing FS defined");
            }
            return new ResponseEntity<>(endpoint + "/" + datasetId, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "It provides dataset type")
    @GetMapping("/{Id}/{branch}/type")
    public ResponseEntity<Object> getDatasetType(Principal principal,
                                                 @PathVariable("Id") UUID Id,
                                                 @PathVariable("branch") String branch) {

        UUID userId = userService.getUser(principal.getName()).id;

        if (!folderRepository.existsById(Id)) {
            return new ResponseEntity<>("Id " + Id + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, Id)) {
            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
        }

        String datasetType;
        boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(Id, branch);

        if (buildSpecificationExists) {
            BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(Id, branch);
            datasetType = buildSpecification.getLanguage();
        } else {
            if(linkRepository.existsByDatasetIdAndBranch(Id, branch)) {
                datasetType = "ignite";
            } else {
                datasetType = "uploaded";
            }
        }

        assert datasetType != null;
        return new ResponseEntity<>(datasetType, HttpStatus.OK);
    }
}
