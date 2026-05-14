package io.orphea.kitab.controllers;

import io.orphea.build.BobEnums.BuildTrigger;
import io.orphea.build.library.models.BuildLog;
import io.orphea.build.library.models.BuildSpecification;
import io.orphea.build.library.repository.BuildSpecificationsRepository;
import io.orphea.build.library.services.BuildLogService;
import io.orphea.build.library.services.KitabService;
import io.orphea.connect.library.repository.LinkRepository;
import io.orphea.dataset.library.Keys.DatasetMappingKey;
import io.orphea.dataset.library.models.DatasetMappingModel;
import io.orphea.dataset.library.repository.DatasetMappingRepository;
import io.orphea.dataset.library.services.DatasetMappingService;
import io.orphea.kitab.library.enums.ResourceStatus;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.models.BranchModel;
import io.orphea.kitab.library.models.DatasetDetailsDTO;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.models.TransactionModel;
import io.orphea.kitab.library.repository.DatasetRepository;
import io.orphea.kitab.library.repository.TransactionRepository;
import io.orphea.kitab.library.services.ResourceService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kitab/dataset")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kitab", description = "This is a catalog management service.")
public class kitabDatasetController {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final DatasetRepository datasetRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final LinkRepository linkRepository;
    private final AuthzService authzService;
    private final ActiveDisplay activeDisplay;
    private final ResourceService resourceService;
    private final KitabService kitabService;
    private final DatasetMappingRepository datasetMappingRepository;
    private final PlatformConfigService platformConfigService;
    private final DatasetMappingService datasetMappingService;
    private final BuildLogService buildLogService;

    @Operation(summary = "This can be used to create dataset.")
    @PostMapping("/create")
    public ResponseEntity<Object> newDataset(Principal principal, @RequestBody CreateDatasetRequest newDataset) {
        User user = userService.getUser(principal.getName());
        UUID userId = user.getId();

        if (!resourceService.existsById(newDataset.getParent())) {
            return new ResponseEntity<>("parent id " + newDataset.parent + " does not exist.", HttpStatus.NOT_FOUND);
        }

        if (platformConfigService.isResourceCreationLimitReached(ResourceType.DATASET)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorDTO(HttpStatus.TOO_MANY_REQUESTS.value(), "Maximum No. of Dashboard Limit Reached."));
        }

        /**
         * Throw access denied in case of user only have viewer permissions.
         */
        if (!authzService.isOwner(userId, newDataset.getParent()) &&
                !authzService.isEditor(userId, newDataset.getParent())) {
            return new ResponseEntity<>("Access Denied to " + newDataset.getParent(), HttpStatus.FORBIDDEN);
        }

        ResourceModel newResource = resourceService.createDataset(newDataset.getName(), newDataset.getDescription(), userId, newDataset.getParent(), newDataset.getSubType());
        return new ResponseEntity<>(newResource, HttpStatus.OK);
    }

    @Operation(summary = "It provides dataset by id")
    @GetMapping("/{datasetId}/{branch}")
    public ResponseEntity<Object> getDatasetById(@PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!resourceService.existsById(datasetId)) {
            return new ResponseEntity<>("Id " + datasetId + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }
        ResourceModel resourceModel = resourceService.findById(datasetId).orElse(null);

        if (resourceModel.getType() != ResourceType.DATASET) {
            return new ResponseEntity<>("Id " + datasetId + " does not exist.", HttpStatus.NOT_FOUND);
        }

        Optional<DatasetMappingModel> datasetMappingModel = datasetMappingService.getDatasetMapping(datasetId, branch);
        UUID currentTransactionId = null;
        UUID buildId = null;
        if (datasetMappingModel.isPresent()) {
            currentTransactionId = datasetMappingModel.get().getCurrentTransaction();
            buildId = datasetMappingModel.get().getCurrentBuildId();
        }

        Optional<BuildLog> buildLog = buildLogService.getBuildLog(buildId);
        BuildTrigger buildTrigger = buildLog.map(BuildLog::getTrigger).orElse(null);

        DatasetDetailsDTO datasetDetailsDTO = new DatasetDetailsDTO();
        datasetDetailsDTO.copyNonNullProperties(resourceModel);
        datasetDetailsDTO.setBuildId(buildId);
        datasetDetailsDTO.setTransactionId(currentTransactionId);
        datasetDetailsDTO.setBuildTrigger(buildTrigger);

        return new ResponseEntity<>(datasetDetailsDTO, HttpStatus.OK);
    }

    @Operation(summary = "It provides dataset by Id")
    @GetMapping("/{Id}/exist")
    public ResponseEntity<Object> datasetExistById(@PathVariable("Id") UUID Id, Principal principal) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!datasetRepository.existsById(Id)) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        //TODO Auth
//        if (!authzService.isViewer(userId, Id)) {
//            return new ResponseEntity<>("Access Denied to " + Id, HttpStatus.FORBIDDEN);
//        }
        if (resourceService.findById(Id).get().getStatus().equals("inTrash")) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        return new ResponseEntity<>(datasetRepository.existsById(Id), HttpStatus.OK);
    }

    @Operation(summary = "It provides dataset by Id")
    @PostMapping("/byIds")
    public ResponseEntity<Object> getDatasetByIds(Principal principal, @RequestBody List<UUID> Ids) {
        UUID userId = userService.getUser(principal.getName()).getId();

        Set<UUID> set = new HashSet<>(Ids);
        Ids.clear();
        Ids.addAll(set);

        List<ResourceModel> responseIds = new ArrayList<>();
        List<ResourceModel> activeDatasets = activeDisplay.statusDisplay(userId, responseIds, ResourceStatus.ACTIVE); // note authz is
        // checked in
        // activeDisplay

        for (UUID Id : Ids) {

            if (!resourceService.existsById(Id)) {
                return new ResponseEntity<>("Id " + Id + " does not exist.", HttpStatus.NOT_FOUND);

            } else if (resourceService.findById(Id).get().getStatus().equals("inTrash")) {
                continue;
//                return new ResponseEntity<>("The dataset has been deleted.", HttpStatus.OK);
            }

            responseIds.add(resourceService.findById(Id).get());
        }

        return new ResponseEntity<>(responseIds, HttpStatus.OK);
    }

    @Operation(summary = "It provides all branches belong to dataset")
    @GetMapping("/{datasetId}/branches")
    public ResponseEntity<Object> branches(Principal principal, @PathVariable("datasetId") UUID datasetId) {
        UUID userId = userService.getUser(principal.getName()).getId();
        Set<BranchModel> branchModels = datasetRepository.getById(datasetId).getBranches();
//        List<BranchModel> branchModels = branchRepository.findAllBranchModelByDatasetId(datasetId);
        // List<BranchModel> activeBranchModel =
        // activeDisplay.activeDisplayBranchModel(userId, branchModels); // note authz
        // is checked in activeDisplay

        return new ResponseEntity<>(branchModels, HttpStatus.OK);
    }

    @Operation(summary = "It provides all transactions by datasetId and branch")
    @GetMapping("/{datasetId}/{branch}/transactions")
    public ResponseEntity<Object> getTransactions(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch) {
        UUID userId = userService.getUser(principal.getName()).getId();

        TransactionModel transaction = transactionRepository.findTransactionModelByDatasetIdAndBranch(datasetId, branch);

        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @Operation(summary = "It provides gives boolean for Build Specification if exists or not.")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/buildSpecification")
    public ResponseEntity<Object> getBuildSpecification(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) {
        if (Objects.equals(datasetId, "undefined") || Objects.equals(branch, "undefined")) {
            return new ResponseEntity<>("Not valid datasetId or branch", HttpStatus.BAD_REQUEST);
        }

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
            transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction();
        }

        if (buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId)) {
            BuildSpecification value = buildSpecificationsRepository.findByTransactionId(transactionId);
            return new ResponseEntity<>(value, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "It provides buildSpecification")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/existsBuildSpecification")
    public ResponseEntity<Object> existsBuildSpecification(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) {
        if (Objects.equals(datasetId, "undefined") || Objects.equals(branch, "undefined")) {
            return new ResponseEntity<>("Not valid datasetId or branch", HttpStatus.BAD_REQUEST);
        }

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        boolean value = buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId);

        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @Operation(summary = "It provides repositoryId by datasetId")
    @GetMapping("/{datasetId}/{branch}/repository")
    public ResponseEntity<Object> repository(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) {
        UUID userId = userService.getUser(principal.getName()).getId();

        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        if (buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId)) {
            UUID repositoryId = buildSpecificationsRepository.findByTransactionId(transactionId).getRepository();

            Map<String, Object> map = Map.of("repositoryId", repositoryId, "branch", branch);

            return new ResponseEntity<>(map, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " or branch" + branch + " does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Provides path for dataset files")
    @GetMapping(path = "/physicalPath/{datasetId}/{branch}/{transactionId}")
    ResponseEntity<Object> buildPhysicalPath(Principal principal, @PathVariable(name = "datasetId") UUID datasetId, @PathVariable(name = "branch") String branch, @PathVariable(name = "transactionId") UUID transactionId) {
        try {
            if (!resourceService.existsById(datasetId)) {
                return new ResponseEntity<>("Id " + datasetId + " does not exist!", HttpStatus.NOT_FOUND);
            }

            UUID userId = userService.getUser(principal.getName()).getId();
            if (!authzService.isViewer(userId, datasetId)) {
                return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
            }

            if (transactionId.toString().compareTo("00000000-0000-0000-0000-000000000000") == 0) {
                transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(datasetId, branch)).getCurrentTransaction();
            }

            HashMap<String, String> response = new HashMap<>();
            String endpoint = kitabService.getPhysicalEndpoint();
            response.put("physicalPath", endpoint + "/" + datasetId + "/" + transactionId);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "It provides dataset type")
    @GetMapping("/{datasetId}/{branch}/{transactionId}/type")
    public ResponseEntity<Object> getDatasetType(Principal principal, @PathVariable("datasetId") UUID datasetId, @PathVariable("branch") String branch, @PathVariable("transactionId") UUID transactionId) {

        UUID userId = userService.getUser(principal.getName()).getId();

        if (!resourceService.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with " + datasetId + " does not exist.", HttpStatus.NOT_FOUND);
        }
        if (!authzService.isViewer(userId, datasetId)) {
            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
        }

        String datasetType;
        boolean buildSpecificationExists = buildSpecificationsRepository.existsBuildSpecificationByTransactionId(transactionId);

        if (buildSpecificationExists) {
            BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);
            datasetType = String.valueOf(buildSpecification.getLanguage());
        } else {
            if (linkRepository.existsByDatasetIdAndBranch(datasetId, branch)) {
                datasetType = "connect";
            } else {
                datasetType = "uploaded";
            }
        }

        assert datasetType != null;
        return new ResponseEntity<>(datasetType, HttpStatus.OK);
    }

//    @Operation(summary = "It provides dataset encoding")
//    @GetMapping("/{Id}/{branch}/encoding")
//    public ResponseEntity<Object> getDatasetEncoding(Principal principal, @PathVariable("Id") UUID datasetId, @PathVariable("branch") String branch) {
//
//        UUID userId = userService.getUser(principal.getName()).getId();
//
//        if (!resourceService.existsById(datasetId)) {
//            return new ResponseEntity<>("Id " + datasetId + " does not exist.", HttpStatus.NOT_FOUND);
//        }
//        if (!authzService.isViewer(userId, datasetId)) {
//            return new ResponseEntity<>("Access Denied to " + datasetId, HttpStatus.FORBIDDEN);
//        }
//
////        Optional<BranchModel> branchModelOptional = branchService.findBranchModelByDatasetIdAndBranch(datasetId, branch);
//
//
//        String encoding = "UTF-8";
////        if (branchModelOptional.isPresent()) {
////            encoding = branchModelOptional.get().getEncoding();
////        }
//
//        SchemaModel schemaModel = schemaRepository.findByDatasetIdAndBranchAndTransactionIdAndStatus(datasetId, branch, transactionId, "active");
//
//        HashMap<String, Object> response = new HashMap<>();
//
//        response.put("encoding", encoding);
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @Getter
    @Setter
    static class CreateDatasetRequest {
        private String name;
        private String description;
        private ResourceType type;
        private ResourceSubtype subType = ResourceSubtype.NONE;
        private UUID parent;
    }
}
