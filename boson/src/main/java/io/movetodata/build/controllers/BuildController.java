package io.movetodata.build.controllers;

import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.movetodata.build.BobEnums.BuildLanguage;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.library.dto.NotebookBuildInitiateRequest;
import io.movetodata.build.library.dto.PreviewSpecsRequest;
import io.movetodata.build.library.models.*;
import io.movetodata.build.library.repository.BuildLogRepository;
import io.movetodata.build.library.repository.BuildSpecificationsRepository;
import io.movetodata.build.library.requests.AbortBuildRequest;
import io.movetodata.build.library.requests.BuildLogRequest;
import io.movetodata.build.library.services.*;
import io.movetodata.kitab.library.models.RepositoryHardwareSpecsModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.requests.HardwareSpecsRequest;
import io.movetodata.passport.exception.UnauthorizedException;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedutils.CommonService;
import io.movetodata.sharedutils.DTO.PageToPageDTOMapper;
import io.movetodata.sharedutils.models.PageSettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.Principal;
import java.util.*;

import static io.movetodata.sharedutils.BackFsFileUtils.getResourcePath;

@Slf4j
@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Build", description = "Build service endpoints")
public class BuildController {

    private final UserService userService;
    private final AuthzService authzService;
    private final BuildService buildService;

    private final BuildLogRepository buildLogRepository;
    private final DatasetRepository datasetRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final PageToPageDTOMapper pageToPageDTOMapper;
    private final PreviewService previewService;
    private final BuildLogService buildLogService;
    private final K8Service k8Service;

    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private BuildSpecService buildSpecService;

    @Operation(summary = "Get all builds status.")
    @PostMapping("/status/all")
    public ResponseEntity<Object> statusAll(Principal principal, PageSettings pageSettings, @RequestBody BuildFilters buildFilters) {
        UUID userId = userService.getUser(principal.getName()).getId();
        log.info(
                "Request for build log page received with data : " + pageSettings);
        return ResponseEntity.ok().body(pageToPageDTOMapper.pageToPageDTO(
                buildLogService.getBuildLogPage(pageSettings, buildFilters, userId)));
    }

    @Operation(summary = "Abort builds and on dataset page it also sends back sockets")
    @PostMapping("/abort")
    public ResponseEntity<Object> abortBuild(Principal principal,
                                             @RequestBody AbortBuildRequest abortBuildRequest

    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        buildService.handleBuildAbort(abortBuildRequest.getBuildId(), abortBuildRequest.getDatasetId(), abortBuildRequest.getBranch(), userId);

        return new ResponseEntity<>("Build aborted", HttpStatus.OK);
    }

    @Operation(summary = "Get all builds by datasetId and Branch.")
    @GetMapping("/status/{datasetId}/{branch}")
    public ResponseEntity<Object> buildsByDatasetIdAndBranch(Principal principal,
                                                             @PathVariable("datasetId") UUID datasetId,
                                                             @PathVariable("branch") String branch

    ) {
        // TODO : Build
//        List<BuildStageLog> buildStageLogs = buildStageLogRepository.findAllByDatasetIdAndBranchOrderByStartedAtDesc(datasetId, branch);

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @Operation(summary = "Get active builds by datasetId and Branch.")
    @GetMapping("/status/{datasetId}/{branch}/active")
    public ResponseEntity<Object> activeBuildsByDatasetIdAndBranch(Principal principal,
                                                                   @PathVariable("datasetId") UUID datasetId,
                                                                   @PathVariable("branch") String branch

    ) {
        // TODO : Build
//        if (Objects.equals(buildStageLogRepository.findFirstByDatasetIdAndBranchAndStatusOrderByStartedAtDesc(datasetId, branch, "active").getStageStatus(), "active")) {
//            return new ResponseEntity<>(buildStageLogRepository.existsByDatasetIdAndBranchAndStatus(datasetId, branch, "active"), HttpStatus.OK);
//        } else {
        return new ResponseEntity<>(false, HttpStatus.OK);
//        }
    }

    @Operation(summary = "This endpoint is for frontend to get logs after it gets socket message")
    @GetMapping("/{buildId}/log")
    public ResponseEntity<Object> logGet(Principal principal,
                                         @PathVariable("buildId") UUID buildId) {

        BuildLog buildLog = buildLogRepository.findById(buildId).orElseThrow();

        return new ResponseEntity<>(buildLog, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint is for funnel and shyne to use for sending only messages while build running.")
    @PostMapping("/{buildId}/log")
    public ResponseEntity<String> logPost(Principal principal,
                                          @PathVariable UUID buildId,
                                          @RequestBody BuildMessageRequest request) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        buildLogService.updateFunnelLog(request, buildId);

        return ResponseEntity.ok("Log updated, socket sent");
    }

    @Operation(summary = "Update build log for other properties than messages.")
    @PostMapping("/updateBuildLog")
    public ResponseEntity<Object> updateBuildLogOtherThanMessages(Principal principal,
                                                                  @RequestBody BuildLogRequest buildLogRequest) throws Exception {

        BuildLog buildLog = buildLogRepository.getReferenceById(buildLogRequest.getBuildId());
        buildLog.setSparkApplicationId(buildLogRequest.getSparkApplicationId());
        buildLog.setBranch(buildLogRequest.getBranch());
        buildLog.setScriptPath(buildLogRequest.getScriptPath());
        buildLogRepository.save(buildLog);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get detailed build logs.")
    @GetMapping("/{buildId}/detailedLog")
    public ResponseEntity<Object> log(Principal principal, HttpServletResponse response,
                                      @PathVariable("buildId") String buildId) throws Exception {

        BuildLog buildLog = buildLogRepository.getReferenceById(UUID.fromString(buildId));

        if (Objects.equals(buildLog.getTrigger(), BuildTrigger.CONNECT)) {
            return new ResponseEntity<>("No detailed logs for connect", HttpStatus.OK);
        }

        InputStream inputStream = null;
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Object> responseEntity = null;

        String backingFS = System.getenv("BACKING_FS");

        switch (backingFS) {
            case "s3":
                responseEntity = ResponseEntity.ok()
                        .headers(headers)
                        .body("Not working yet.");
                break;
            case "gs":
                Credentials credentials = CommonService.getGoogleCredentials();
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                BlobId blobId = BlobId.of(System.getenv("GS_BUCKET"), "bosler/logs/" + buildId + "/" + "transform" + "/spark-driver.log");
                Blob blob = storage.get(blobId);
                inputStream = new ByteArrayInputStream(blob.getContent());
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "spark-driver.log");
                responseEntity = ResponseEntity.ok()
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
                break;
            case "hdfs":
                Configuration conf = new Configuration();
                conf.set("fs.defaultFS", System.getenv("HDFS_ENDPOINT"));
                FileSystem fs = FileSystem.get(conf);
                FSDataInputStream fileOpen = fs.open(new Path("/bosler/logs/" + buildId + "/" + "transform" + "/spark-driver.log"));
                inputStream = fileOpen;
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "spark-driver.log");
                responseEntity = ResponseEntity
                        .ok()
                        .headers(headers)
                        .body(new InputStreamResource(inputStream));
                break;
            case "localfs":

                String filePath = getResourcePath("logs", UUID.fromString(buildId), "transform" + "/spark-driver.log");
                File file = new File(filePath);

                if (!file.exists()) {
                    return new ResponseEntity<>("No detailed logs found", HttpStatus.NO_CONTENT);
                }

                FileSystemResource resource = new FileSystemResource(filePath);
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=spark-driver.log");
                StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
                response.flushBuffer();

                break;
            default:
                throw new Exception("Unknown backing FS ");
        }

        return new ResponseEntity<>(responseEntity, HttpStatus.OK);
    }

    @Operation(summary = "Initiate build for notebook dataset")
    @PostMapping("/buildNotebook")
    public ResponseEntity<Object> buildNotebook(Principal principal, @Valid @RequestBody NotebookBuildInitiateRequest notebookBuildInitiateRequest) throws JsonProcessingException {
        UUID userId = userService.getUser(principal.getName()).getId();
        buildService.initiateBuildForNotebook(notebookBuildInitiateRequest);

        return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
    }

    @Operation(summary = "Build by a trigger such as dataset or repositoryId and branch.")
    @PostMapping("/build/{triggerType}")
    public ResponseEntity<Object> buildByTrigger(Principal principal,
                                                 @Valid @PathVariable("triggerType") BuildTrigger triggerType,
                                                 @Valid @RequestBody BuildProperties buildProperties) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();

        BuildType buildType = buildProperties.getBuildType();
        BuildTrigger trigger = triggerType;
        String scriptPath = null;
        UUID repository = null;
        String fileName = null;
        String lineNo = null;
        String branch = buildProperties.getBranch();
        String branchId = null;
        String commitId = null;
        BuildLanguage buildLanguage = null;
        String rowLimit = buildProperties.getRowLimit();
        int cores = 1;
        String memory = "512m";
        int numberOfExecutors = 1;
        int failureRetries = 0;


        switch (triggerType) {
            case DATASET:
                break;
            case PYTHON:
            case SQL:
                scriptPath = buildProperties.getScriptPath();
                repository = buildProperties.getRepositoryId();
                commitId = buildProperties.getCommitId();
                buildLanguage = buildProperties.getBuildLanguage();
                break;
            default:
                return new ResponseEntity<>("Not valid trigger type", HttpStatus.BAD_REQUEST);
        }

        // Transaction Id comes, when build is fired from dataset page
        if (buildProperties.getTransactionId() != null && buildSpecificationsRepository.existsBuildSpecificationByTransactionId(buildProperties.getTransactionId())) {
            System.out.println("BUILD SPEC FOUND");
            // Only 1 build spec will be found of a transaction id i.e when built from dataset page
            BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(buildProperties.getTransactionId());
            repository = buildSpecification.getRepository();
            branch = buildSpecification.getBranch();
            scriptPath = buildSpecification.getScriptPath();
            branchId = buildSpecification.getBranchId();
            commitId = buildSpecification.getCommitId();
            buildLanguage = buildSpecification.getLanguage();
            fileName = buildSpecification.getFileName();
            lineNo = buildSpecification.getLineNo();
            log.info("FILE NAME : " + fileName);
            log.info("LINE NO : " + lineNo);
        } else if (buildProperties.getBuildId() != null && buildSpecificationsRepository.existsBuildSpecificationByBuildId(buildProperties.getBuildId())) {
            log.info("BUILD SPEC NOT FOUND WITH TRANS");
            // Multiple build spec will be found for a build id as multiple dataset can be build from a single repo
            // i.e. when built from repo / build table
            List<BuildSpecification> buildSpecification = buildSpecificationsRepository.findByBuildId(buildProperties.getBuildId());

            repository = buildSpecification.get(0).getRepository();
            branch = buildSpecification.get(0).getBranch();
            scriptPath = buildSpecification.get(0).getScriptPath();
            branchId = buildSpecification.get(0).getBranchId();
            commitId = buildSpecification.get(0).getCommitId();
            buildLanguage = buildSpecification.get(0).getLanguage();
            fileName = null;
            lineNo = null;
        }

        if (repository == null) {
            throw new Exception("REPO IS NOT COMING");
        }

        // Hardware Specs
        RepositoryHardwareSpecsModel repositoryHardwareSpecsModel = k8Service.getHardwareSpecs(repository, branch, scriptPath, userId);

        cores = repositoryHardwareSpecsModel.getCores();
        memory = repositoryHardwareSpecsModel.getMemory();
        numberOfExecutors = repositoryHardwareSpecsModel.getNumberOfExecutors();
        failureRetries = repositoryHardwareSpecsModel.getFailureRetries();

        return previewService.buildTransform(userId,
                repository,
                branch,
                scriptPath,
                branchId,
                commitId,
                cores,
                memory,
                numberOfExecutors,
                failureRetries,
                trigger,
                BuildLaunchedBy.MANUAL,
                buildType,
                buildLanguage,
                buildProperties.getCode(),
                rowLimit,
                fileName,
                lineNo);

    }

    @Operation(summary = "This endpoint is used to get build specification with other specified properties")
    @PostMapping("/getBuildSpecificationWithAnother")
    public ResponseEntity<Object> GetSpecificationWithAnother(Principal principal, @RequestBody CheckAnotherSpecification searchValues) {
        try {
            Log.info(">>>> Dataset : " + searchValues.getDatasetId());
            Log.info(">>>> Branch : " + searchValues.getBranch());
            Log.info(">>>> Repo : " + searchValues.getRepository());
            Log.info(">>>> Lang : " + searchValues.getLanguage());
            Log.info(">>>> Transac : " + searchValues.getTransactionId());
            Log.info(">>>> Path : " + searchValues.getScriptPath());
            Log.info(">>>> Lang : " + searchValues.getLanguage());

            if (!datasetRepository.existsById(searchValues.getDatasetId())) { // check if the dataset exists in catalog
                Log.info(" >>>>>>>>> NO DATASET FOUND");
                return new ResponseEntity<>("No dataset found in catalog for " + searchValues.getDatasetId(), HttpStatus.NOT_FOUND);
            }

            UUID userId = userService.getUser(principal.getName()).getId();

            if (!authzService.isEditor(userId, searchValues.getDatasetId())) {
                throw new UnauthorizedException();
            }

            HashMap<String, Object> response = new HashMap<>();

            if (buildSpecService.existsBuildSpecWithAnother(searchValues.getDatasetId(), searchValues.getBranch(), searchValues.getRepository(), searchValues.getScriptPath(), searchValues.getLanguage())) {
                Log.info(" >>>>>>>>> BILD SPEC FOUND WITH ANOTHER FOUND");
                BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(searchValues.getTransactionId());

                HashMap<String, String> buildSpecParams = new HashMap<>();

                buildSpecParams.put("scriptPath", buildSpecification.getScriptPath());
                buildSpecParams.put("branch", buildSpecification.getBranch());
                buildSpecParams.put("repository", buildSpecification.getRepository().toString());
                buildSpecParams.put("language", buildSpecification.getLanguage().toString());

                response.put("status", true);
                response.put("message", "Build Specification found with another dataset");
                response.put("debug", buildSpecParams);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Log.info(" >>>>>>>>> FINAL NOT FOUND");
                response.put("status", false);
                response.put("message", "No Build Specification found");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }


        } catch (Exception e) {
            Log.error(" >>>>>>>>> ERROR " + e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Check Schedules and build by source")
    @PostMapping("/buildBySource")
    ResponseEntity<Object> buildBySource(Principal principal, @RequestBody BuildSpecification
            buildSpecificationRequest) throws Exception {

        // TODO AUTH
        UUID userId = userService.getUser(principal.getName()).getId();


        if (!datasetRepository.existsById(buildSpecificationRequest.getDatasetId())) {
            return new ResponseEntity<>("Dataset with Id " + buildSpecificationRequest.getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);
        }

        buildService.buildBySource(userId, buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

        return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);

    }

    @Operation(summary = "Check Schedules and build by source")
    @GetMapping("/buildBySource/{datasetId}/{branch}")
    ResponseEntity<Object> buildBySourceGet(Principal principal,
                                            @PathVariable("datasetId") UUID datasetId,
                                            @PathVariable("branch") String branch) throws Exception {

        // TODO AUTH
        UUID userId = userService.getUser(principal.getName()).getId();


        if (!datasetRepository.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " does not exist", HttpStatus.NOT_FOUND);
        }


        buildService.checkAndBuildBySource(datasetId, branch, userId);

        return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);

    }

    @Operation(summary = "validate")
    @GetMapping("/validateSchedule/{sd}/{sb}/{td}/{tb}")
    public ResponseEntity<Object> validate(Principal principal, @RequestParam UUID sd, @RequestParam String sb, @RequestParam UUID td, @RequestParam String tb) throws Exception {
        return new ResponseEntity<>(buildService.checkForScheduleValidation(sd, sb, td, tb), HttpStatus.OK);
    }

    @Operation(summary = "Get Hardware Specs")
    @PostMapping("/hardwareSpecs")
    public ResponseEntity<Object> getHardwareSpecs(Principal principal,
                                                   @RequestBody HardwareSpecsRequest hardwareSpecsRequest) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, hardwareSpecsRequest.getRepository())) {
            throw new UnauthorizedException();
        }

        RepositoryHardwareSpecsModel hardwareSpecsModel = k8Service.getHardwareSpecs(hardwareSpecsRequest.getRepository(), hardwareSpecsRequest.getBranch(), hardwareSpecsRequest.getScriptPath(), userId);

        return new ResponseEntity<>(hardwareSpecsModel, HttpStatus.OK);
    }

    @Operation(summary = "Update Hardware Specs")
    @PutMapping("/hardwareSpecs")
    public ResponseEntity<Object> updateHardwareSpecs(Principal principal,
                                                      @RequestBody RepositoryHardwareSpecsModel repositoryHardwareSpecsModel) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, repositoryHardwareSpecsModel.getRepository())) {
            throw new UnauthorizedException();
        }

        k8Service.updateHardwareSpecsModel(repositoryHardwareSpecsModel, userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get Preview Specs")
    @PostMapping("/previewSpecs")
    public ResponseEntity<Object> getPreviewSpecs(Principal principal,
                                                  @RequestBody PreviewSpecsRequest previewSpecsRequest) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, previewSpecsRequest.getRepositoryId())) {
            throw new UnauthorizedException();
        }

        PreviewSpecsModel previewSpecsModel = k8Service.getPreviewSpecs(previewSpecsRequest.getRepositoryId(), previewSpecsRequest.getBranch());

        return new ResponseEntity<>(previewSpecsModel, HttpStatus.OK);
    }

    @Operation(summary = "Put Preview Specs")
    @PutMapping("/previewSpecs")
    public ResponseEntity<Object> putPreviewSpecs(Principal principal,
                                                  @RequestBody PreviewSpecsRequest previewSpecsRequest) throws Exception {

        UUID userId = userService.getUser(principal.getName()).getId();
        if (!authzService.isViewer(userId, previewSpecsRequest.getRepositoryId())) {
            throw new UnauthorizedException();
        }

        k8Service.putPreviewSpecs(previewSpecsRequest.getRepositoryId(), previewSpecsRequest.getBranch(), previewSpecsRequest);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Provides Build Spec by Build Id")
    @PostMapping("/getBuildSpecificationByBuildId/{buildId}")
    public ResponseEntity<Object> getBuildSpecificationByBuildId(Principal principal, @PathVariable("buildId") UUID buildId) {
        List<BuildSpecification> buildSpecification = buildSpecificationsRepository.findByBuildId(buildId);

        if (buildSpecification.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(buildSpecification, HttpStatus.OK);
    }

    @Operation(summary = "Get preview build results")
    @GetMapping("/previewResult/{previewId}")
    public ResponseEntity<Object> getPreviewResult(Principal principal,
                                                   @PathVariable("previewId") UUID previewId
    ) throws Exception {
        UUID userId = userService.getUser(principal.getName()).getId();
        Map<String, Object> result = buildService.getPreviewResult(previewId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


}
