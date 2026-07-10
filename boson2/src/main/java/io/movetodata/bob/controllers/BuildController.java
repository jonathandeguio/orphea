package io.movetodata.bob.controllers;

import io.movetodata.bob.library.models.*;
import io.movetodata.bob.library.repository.BuildLogRepository;
import io.movetodata.bob.library.repository.BuildSpecificationsRepository;
import io.movetodata.bob.library.repository.BuildStageLogRepository;
import io.movetodata.bob.library.services.BuildService;
import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.kitab.library.repository.DatasetStatsRepository;
import io.movetodata.passport.library.service.AuthzService;
import io.movetodata.passport.library.service.UserService;
import io.movetodata.sharedUtils.KubernetesUtils;
import io.movetodata.sharedUtils.Response.OkResponse;
import io.movetodata.synchro.library.models.PostgresSyncSpecification;
import io.movetodata.synchro.library.repository.PostgresSyncRepository;
import io.movetodata.synchro.library.services.SynchroService;
import io.movetodata.zoro.library.services.ZoroService;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static io.movetodata.sharedUtils.KubernetesUtils.deletePod;
import static io.movetodata.sharedUtils.Redis.deleteCache;
import static io.movetodata.sharedUtils.Redis.deleteCacheWithWildCard;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/api/bob")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bob", description = "Build service endpoints")
public class BuildController {

    private final UserService userService;
    private final AuthzService authzService;
    private final BuildService buildService;
    private final SynchroService synchroService;
    private final ZoroService zoroService;
    private final PostgresSyncRepository postgresSyncRepository;
    private final DatasetStatsRepository datasetStatsRepository;

    private final OkResponse response = new OkResponse();

    private final BuildLogRepository buildLogRepository;
    private final BuildStageLogRepository buildStageLogRepository;
    private final DatasetRepository datasetRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;

    @Autowired
    SimpMessagingTemplate template;

    @Operation(summary = "Get build status.")
    @GetMapping("/{buildId}/status")
    public ResponseEntity<Object> status(Principal principal,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse servletResponse,
                                         @PathVariable("buildId") UUID buildId
    ) throws Exception {

        return new ResponseEntity<>(buildLogRepository.findById(buildId), HttpStatus.OK);
    }

    @Operation(summary = "Get all builds status.")
    @GetMapping("/status/all")
    public ResponseEntity<Object> statusAll(Principal principal,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse servletResponse

    ) {

        List<BuildLog> buildLogList = buildLogRepository.findAllByOrderByStartedAtDesc();

        return new ResponseEntity<>(buildLogList, HttpStatus.OK);
    }

    @Operation(summary = "This endpoint can be used to abort build.")
    @GetMapping("/{id}/abort")
    public ResponseEntity<Object> abortBuild(Principal principal,
                                             @PathVariable("id") UUID id) throws ApiException, IOException {

        BuildLog buildLog = buildLogRepository.findById(id).get();

        buildLog.status = "aborted";

        buildLog.finishedAt = new Date();

        buildLogRepository.save(buildLog);


        String buildId = String.valueOf(id);

        ApiClient client = KubernetesUtils.kubernetesClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api coreApi = new CoreV1Api(client);

        List<V1Pod> podList =
                coreApi
                        .listNamespacedPod("movetodata", "false", null, null,
                                null, null, null, null, null,
                                null, null)
                        .getItems();

        V1Pod pod = null;
        for (V1Pod p : podList) {
            if (Objects.requireNonNull(p.getMetadata()).getName().contains(buildId.substring(buildId.lastIndexOf('-') + 1) + "-driver")) {
                pod = p;
                break;
            }
        }

        if (pod != null) {
            deletePod("movetodata", pod.getMetadata().getName());
        }

//        SocketMessage socketMessage = new SocketMessage();
//        socketMessage.setMessage("aborted");
//        template.convertAndSend("/topic/build/" + buil + "/" + branch, socketMessage);

        return new ResponseEntity<>("Build with id " + id + " aborted", HttpStatus.OK);
    }

    @Operation(summary = "Abort builds by datasetId and Branch.")
    @GetMapping("/abort/{datasetId}/{branch}")
    public ResponseEntity<Object> abortBuildByDatasetIdAndBranch(Principal principal,
                                                                 @PathVariable("datasetId") UUID datasetId,
                                                                 @PathVariable("branch") String branch

    ) throws ApiException, IOException {

        // TODO :doesn't really aborts it, just updates the log

        if(buildStageLogRepository.existsByDatasetIdAndBranchAndStatus(datasetId, branch, "active")) {
            BuildStageLog buildStageLog = buildStageLogRepository.findByDatasetIdAndBranchAndStatus(datasetId, branch, "active");

            BuildLog buildLog = buildLogRepository.findById(buildStageLog.getBuildLog().id).get();

            buildLog.status = "aborted";

            buildLogRepository.save(buildLog);

            buildStageLog.status = "aborted";

            buildStageLogRepository.save(buildStageLog);


            ApiClient client = KubernetesUtils.kubernetesClient();
            Configuration.setDefaultApiClient(client);

            CoreV1Api coreApi = new CoreV1Api(client);

            // TODO : get namespace from env
            List<V1Pod> podList =
                    coreApi
                            .listNamespacedPod("movetodata", "false", null, null,
                                    null, null, null, null, null,
                                    null, null)
                            .getItems();

            V1Pod pod = null;
            for (V1Pod p : podList) {
                if (Objects.requireNonNull(p.getMetadata()).getName().contains(String.valueOf(buildLog.getId()).substring(String.valueOf(buildLog.getId()).lastIndexOf('-') + 1) + "-driver")) {
                    pod = p;
                    break;
                }
            }

            if (pod != null) {
                deletePod("movetodata", pod.getMetadata().getName());
            }


            SocketMessage socketMessage = new SocketMessage();
            socketMessage.setMessage("aborted");
            template.convertAndSend("/topic/build/" + datasetId + "/" + branch, socketMessage);

            return new ResponseEntity<>("Build aborted with id " +buildLog.id, HttpStatus.OK);

        } else {
            return new ResponseEntity<>("Not active build with id " + datasetId + " and " + branch , HttpStatus.OK);
        }

    }

    @Operation(summary = "Get all builds by datasetId and Branch.")
    @GetMapping("/status/{datasetId}/{branch}")
    public ResponseEntity<Object> buildsByDatasetIdAndBranch(Principal principal,
                                                             @PathVariable("datasetId") UUID datasetId,
                                                             @PathVariable("branch") String branch

    ) {

        List<BuildStageLog> buildStageLogs = buildStageLogRepository.findAllByDatasetIdAndBranchOrderByStartedAtDesc(datasetId, branch);

        return new ResponseEntity<>(buildStageLogs, HttpStatus.OK);
    }

    @Operation(summary = "Get active builds by datasetId and Branch.")
    @GetMapping("/status/{datasetId}/{branch}/active")
    public ResponseEntity<Object> activeBuildsByDatasetIdAndBranch(Principal principal,
                                                                   @PathVariable("datasetId") UUID datasetId,
                                                                   @PathVariable("branch") String branch

    ) {

        return new ResponseEntity<>(buildStageLogRepository.existsByDatasetIdAndBranchAndStatus(datasetId, branch, "active"), HttpStatus.OK);
    }

    @Operation(summary = "Get build logs.")
    @GetMapping("/{buildId}/log")
    public ResponseEntity<Object> log(Principal principal,
                                      @PathVariable("buildId") String buildId) throws Exception {

        ApiClient client = KubernetesUtils.kubernetesClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api coreApi = new CoreV1Api(client);

        PodLogs logs = new PodLogs();

        List<V1Pod> podList =
                coreApi
                        .listNamespacedPod("movetodata", "false", null, null,
                                null, null, null, null, null,
                                null, null)
                        .getItems();

        /*
//        kubectl -n movetodata logs movetodata-spark-buildId-driver
        for (V1Pod pod : podList) {
//            System.out.println("printing it");
//            System.out.println(pod.getMetadata().getName());

            if (Objects.requireNonNull(pod.getMetadata()).getName().contains(buildId.substring(buildId.lastIndexOf('-') + 1) + "-driver")) {

                InputStream is = logs.streamNamespacedPodLog(pod);
                Scanner sc = new Scanner(is).useDelimiter("\\A");
                String content = "";
                if (sc.hasNext()) {
                    content = sc.next();
                }
                return new ResponseEntity<>(content, HttpStatus.OK);
            }

         */

        V1Pod pod = null;
        for (V1Pod p : podList) {
            if (Objects.requireNonNull(p.getMetadata()).getName().contains(buildId.substring(buildId.lastIndexOf('-') + 1) + "-driver")) {
                pod = p;
                break;
            }
        }

        if (pod != null) {


//            InputStream is = logs.streamNamespacedPodLog(pod);
//            Scanner sc = new Scanner(is).useDelimiter("\\A");
//            String content = "";
//            if (sc.hasNext()) {
//                content = sc.next();
//            }
//
//            System.out.println();

//            V1PodLogOptions logOptions = new V1PodLogOptions();
//            logOptions.setFollow(true); // set to true to follow the logs as they are written


            String log = coreApi.readNamespacedPodLog(pod.getMetadata().getName(), "movetodata", null, true, null,
                    null, null, null, null, null, null);


            HashMap<String, HashMap<String, String>> logOutput = new HashMap<>();

            HashMap<String, String> stepStarting = new HashMap<>();

            stepStarting.put("status", "active");
            stepStarting.put("startedAt", "time");
            stepStarting.put("finishedAt", "time");
            stepStarting.put("log", log);

            logOutput.put("Started", stepStarting);

            HashMap<String, String> stepRunning = new HashMap<>();

            stepRunning.put("status", "active");
            stepRunning.put("startedAt", "time");
            stepRunning.put("finishedAt", "time");
            stepRunning.put("log", log);
            logOutput.put("Started", stepRunning);

            HashMap<String, String> stepFinished = new HashMap<>();

            stepFinished.put("status", "active");
            stepFinished.put("startedAt", "time");
            stepFinished.put("finishedAt", "time");
            stepFinished.put("log", log);

            logOutput.put("Finished", stepFinished);

            return new ResponseEntity<>(logOutput, HttpStatus.OK);
        }


        return new ResponseEntity<>("Build Id " + buildId + " is not valid", HttpStatus.BAD_REQUEST);

    }


    @Operation(summary = "Build by a trigger such as dataset or repositoryId and branch.")
    @PostMapping("/build/{triggerType}")
    public ResponseEntity<Object> buildByTrigger(Principal principal,
                                                 @Valid @PathVariable("triggerType") String triggerType,
                                                 @Valid @RequestBody BuildProperties buildProperties) throws Exception {

        UUID userId = userService.getUser(principal.getName()).id;

        BuildSpecification buildSpecification = null;
        String trigger = null;
        String scriptPath = null;
        UUID repository = null;
        String branch = null;
        String branchId = null;
        String commitId = null;
        int cores = 0;
        String memory = null;
        int numberOfExecutors = 0;
        int failureRetries = 0;

        buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(buildProperties.getDatasetId(), buildProperties.getBranch());

        switch (triggerType) {
            case "Dataset":
                trigger = "dataset";
                break;
            case "Repository":
            case "Build":
                trigger = triggerType.toLowerCase();
                repository = buildProperties.getRepositoryId();
                branch = buildProperties.getBranch();
                break;
            default:
                return new ResponseEntity<>("Not valid trigger type", HttpStatus.BAD_REQUEST);
        }

        if (buildSpecification != null) {
            repository = buildSpecification.getRepository();
            branch = buildSpecification.getBranch();
            scriptPath = buildSpecification.getScriptPath();
            branchId = buildSpecification.getBranchId();
            commitId = buildSpecification.getCommitId();
            cores = buildSpecification.getCores();
            memory = buildSpecification.getMemory();
            numberOfExecutors = buildSpecification.getNumberOfExecutors();
            failureRetries = buildSpecification.getFailureRetries();
        } else {
            scriptPath = buildProperties.getScriptPath();
            branchId = buildProperties.getBranchId();
            commitId = buildProperties.getCommitId();
            cores = buildProperties.getCores();
            memory = buildProperties.getMemory();
            numberOfExecutors = buildProperties.getNumberOfExecutors();
            failureRetries = buildProperties.getFailureRetries();
        }

        if (scriptPath.endsWith(".py")) {
            return buildService.buildPythonTransform(userId, repository, branch, scriptPath, branchId, commitId, cores, memory, numberOfExecutors, failureRetries, trigger);
        } else if (scriptPath.endsWith(".sql")) {
            return buildService.buildSqlTransform(userId, repository, branch, scriptPath, branchId, commitId, cores, memory, numberOfExecutors, failureRetries, trigger);
        }

        return new ResponseEntity<>("Unsupported script file extension", HttpStatus.BAD_REQUEST);
    }


    @Operation(summary = "This endpoint is used to get build specification with other specified properties")
    @PostMapping("/getBuildSpecificationWithAnother")
    public ResponseEntity<Object> GetSpecificationWithAnother(Principal principal, @Valid @RequestBody CheckAnotherSpecification searchValues) {
        try {
            if (!datasetRepository.existsById(searchValues.getDatasetId())) { // check if the dataset exists in catalog
                return new ResponseEntity<>("No dataset found in catalog for " + searchValues.getDatasetId(), HttpStatus.NOT_FOUND);
            }

            UUID userId = userService.getUser(principal.getName()).id;

            if (!authzService.isEditor(userId, searchValues.getDatasetId())) {
                return new ResponseEntity<>("Access Denied to " + searchValues.getDatasetId(), HttpStatus.FORBIDDEN);
            }

            boolean existBuildSpecification = buildSpecificationsRepository.existsBuildSpecificationByDatasetIdAndBranch(searchValues.getDatasetId(), searchValues.getBranch());

            // if no build spec then we are ok
            if (!existBuildSpecification) {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }

            List<BuildSpecification> allByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage = buildSpecificationsRepository.findAllByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage(searchValues.getDatasetId(), searchValues.getBranch(), searchValues.getRepository(), searchValues.getScriptPath(), searchValues.getLanguage());

            // if found build specification but with some other repository/branch and script then return True because above we already checked if exists
            if (allByDatasetIdAndBranchAndRepositoryAndScriptPathAndLanguage.isEmpty()) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "This endpoint is for update the status of a build")
    @PostMapping("/update/{buildId}")
    public ResponseEntity<Object> updateBuildStatus(Principal principal, @PathVariable("buildId") UUID buildId, @Valid @RequestBody UpdateStatus updateValues) {
        try {
            UUID userId = userService.getUser(principal.getName()).id;

            if (!buildLogRepository.existsById(buildId)) {
                return new ResponseEntity<>("No build found in catalog for " + buildId, HttpStatus.NOT_FOUND);
            }

//        if (!authzService.isEditor(userId, updateValues.getDatasetId())) {
//            return new ResponseEntity<>("Access Denied to " + updateValues.getDatasetId(), HttpStatus.FORBIDDEN);
//        }

            SocketMessage socketMessage = new SocketMessage();
            socketMessage.setMessage(updateValues.getStageStatus());
            template.convertAndSend("/topic/build/" + updateValues.getDatasetId() + "/" + updateValues.getBranch(), socketMessage);

            BuildLog buildLog = buildLogRepository.getById(buildId);
            buildLog.setStatus(updateValues.getBuildStatus());

            if (Objects.equals(updateValues.getBuildStatus(), "success")) {
                buildLog.setFinishedAt(new Date());
                buildLogRepository.save(buildLog);
            } else {
                buildLog.setRepository(updateValues.getRepositoryId());
                buildLog.setBranch(updateValues.getBranch());
                buildLog.setScriptPath(updateValues.getScriptPath());
                buildLog.setSparkApplicationId(updateValues.getSparkApplicationId());

                if (!buildStageLogRepository.existsByDatasetIdAndBranchAndBuildLogId(updateValues.getDatasetId(), updateValues.getBranch(), buildId)) {
                    BuildStageLog buildStageLog = new BuildStageLog();
                    buildStageLog.setDatasetId(updateValues.getDatasetId());
                    buildStageLog.setRepository(updateValues.getRepositoryId());
                    buildStageLog.setBranch(updateValues.getBranch());
                    buildStageLog.setBranchId(updateValues.getBranchId());
                    buildStageLog.setCommitId(updateValues.getCommitId());
                    buildStageLog.setScriptPath(updateValues.getScriptPath());
                    buildStageLog.setStatus(updateValues.getStageStatus());
                    buildStageLog.setStartedAt(new Date());
                    buildStageLog.setStartedBy(userId);
                    buildStageLog.setBuildLog(buildLog);
                    buildStageLogRepository.save(buildStageLog);
                } else {
                    BuildStageLog buildStageLog = buildStageLogRepository.findByDatasetIdAndBranchAndBuildLogId(updateValues.getDatasetId(), updateValues.getBranch(), buildId);
                    buildStageLog.setStatus(updateValues.getStageStatus());
                    buildStageLog.setFinishedAt(new Date());
                    buildStageLogRepository.save(buildStageLog);
                }
            }

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "resolve build specifications")
    @PostMapping("/resolveBuildSpecifications")
    ResponseEntity<Object> resolveBuildSpecifications(Principal principal, @RequestBody BuildSpecification
            buildSpecification1) {
        try {
            // TODO AUTH
            UUID userId = userService.getUser(principal.getName()).id;

            // Delete old build specifications

            List<BuildSpecification> buildSpecifications = buildSpecificationsRepository.findAllByRepositoryAndBranchAndScriptPathAndBuildIdNot(
                    buildSpecification1.getRepository(),
                    buildSpecification1.getBranch(),
                    buildSpecification1.getScriptPath(),
                    buildSpecification1.getBuildId());

            buildSpecificationsRepository.deleteAll(buildSpecifications);
            // Delete


            List<BuildSpecification> checkExisting = buildSpecificationsRepository.findAllByDatasetIdAndBranch(
                    buildSpecification1.getDatasetId(),
                    buildSpecification1.getBranch());

            if (checkExisting.isEmpty()) {
                buildSpecification1.setCreatedBy(userId);
                buildSpecification1.setUpdatedBy(userId);
                buildSpecificationsRepository.save(buildSpecification1);
            } else {
                for (BuildSpecification buildSpecification : checkExisting) {
                    buildSpecification.setUpdatedAt(new Date());
                    buildSpecification.setUpdatedBy(userId);
                    buildSpecification.setLanguage(buildSpecification1.getLanguage());
                    buildSpecification.setScriptPath(buildSpecification1.getScriptPath());
                    buildSpecification.setBranchId(buildSpecification1.getBranchId());
                    buildSpecification.setCommitId(buildSpecification1.getCommitId());
                    buildSpecification.setCores(buildSpecification1.getCores());
                    buildSpecification.setMemory(buildSpecification1.getMemory());
                    buildSpecification.setBuildId(buildSpecification1.getBuildId());
                    buildSpecificationsRepository.saveAndFlush(buildSpecification);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong.", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Check Schedules and build by source")
    @PostMapping("/buildBySource")
    ResponseEntity<Object> buildBySource(Principal principal, @RequestBody BuildSpecification
            buildSpecificationRequest) throws Exception {

        // TODO AUTH
        UUID userId = userService.getUser(principal.getName()).id;


        if (!datasetRepository.existsById(buildSpecificationRequest.getDatasetId())) {
            return new ResponseEntity<>("Dataset with Id " + buildSpecificationRequest.getDatasetId() + " does not exist", HttpStatus.NOT_FOUND);
        }


        buildService.buildBySource(userId, buildSpecificationRequest);

        return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);

    }

    @Operation(summary = "Check Schedules and build by source")
    @GetMapping("/buildBySource/{datasetId}/{branch}")
    ResponseEntity<Object> buildBySourceGet(Principal principal,
                                            @PathVariable("datasetId") UUID datasetId,
                                            @PathVariable("branch") String branch) throws Exception {

        // TODO AUTH
        UUID userId = userService.getUser(principal.getName()).id;


        if (!datasetRepository.existsById(datasetId)) {
            return new ResponseEntity<>("Dataset with Id " + datasetId + " does not exist", HttpStatus.NOT_FOUND);
        }


        buildService.checkAndBuildBySource(datasetId, branch, userId);

        return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);

    }


    @Operation(summary = "Post processing after transform")
    @PostMapping("/postTransform")
    public ResponseEntity<Object> postTransform(Principal principal,
                                                @RequestBody BuildSpecification buildSpecificationRequest) throws Exception {

        // TODO AUTH
        UUID userId = userService.getUser(principal.getName()).id;


        // Check build schedule and launch builds
        buildService.buildBySource(userId, buildSpecificationRequest);


        // Disabling auto stats calculations
        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch())) {

            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

            datasetStatsRepository.delete(datasetStatsModel);
        }

        // Remove Redis cache
        deleteCache("dataset" + buildSpecificationRequest.getDatasetId() + buildSpecificationRequest.getBranch());
        deleteCache("columns" + buildSpecificationRequest.getDatasetId() + buildSpecificationRequest.getBranch());
        deleteCacheWithWildCard("sparkResults" + buildSpecificationRequest.getDatasetId() + buildSpecificationRequest.getBranch() + "*");
        deleteCacheWithWildCard("chartData" + buildSpecificationRequest.getDatasetId() + buildSpecificationRequest.getBranch() + "*");


        // Stats calculations
//        SocketMessage textMessage = new SocketMessage();
//        textMessage.setMessage("active");
//        template.convertAndSend("/topic/statsCalculation/" + buildSpecificationRequest.getDatasetId() + "/" + buildSpecificationRequest.getBranch(), textMessage);
//        zoroService.statsCalculation(userId, buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());


        // perform Sync

        SocketMessage textMessage = new SocketMessage();


        if (postgresSyncRepository.existsByDatasetIdAndBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch())) {

            textMessage.setMessage("active");
            template.convertAndSend("/topic/postgresSync/" + buildSpecificationRequest.getDatasetId() + "/" + buildSpecificationRequest.getBranch(), textMessage);

            PostgresSyncSpecification postgresSyncSpecification = postgresSyncRepository.findByDatasetIdAndBranch(buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

            postgresSyncSpecification.setStartedAt(new Date());
            postgresSyncSpecification.setSyncStatus("active");

            postgresSyncRepository.save(postgresSyncSpecification);

            synchroService.performSync(userId, buildSpecificationRequest.getDatasetId(), buildSpecificationRequest.getBranch());

        }

        return new ResponseEntity<>("Launched the builds by source", HttpStatus.OK);
    }
}
