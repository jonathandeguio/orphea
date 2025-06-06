package io.bosler.build.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.bosler.build.BobEnums.*;
import io.bosler.build.library.Specifications.BuildLogSpecification;
import io.bosler.build.library.models.*;
import io.bosler.build.library.repository.BuildLogMessagesRepository;
import io.bosler.build.library.repository.BuildLogRepository;
import io.bosler.build.library.repository.BuildSpecificationsRepository;
import io.bosler.connect.library.enums.SourceTypeEnum;
import io.bosler.connect.library.models.DatabaseSourceConfig;
import io.bosler.connect.library.models.Link;
import io.bosler.connect.library.repository.LinkRepository;
import io.bosler.connect.library.services.SourceService;
import io.bosler.dataset.library.enums.DataHealthTypeEnum;
import io.bosler.dataset.library.services.DataHealth.DataHealthService;
import io.bosler.dataset.library.services.DatasetMappingService;
import io.bosler.kitab.library.enums.ResourceType;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.services.ResourceService;
import io.bosler.sharedutils.models.PageSettings;
import io.bosler.synchro.library.models.SyncSpecification;
import io.bosler.synchro.library.repository.SyncRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildLogService {

    private final DatasetMappingService datasetMappingService;
    private final BuildLogRepository buildLogRepository;
    private final BuildLogMessagesRepository buildLogMessagesRepository;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final K8Service k8Service;
    @Autowired
    private final SimpMessagingTemplate template;
    private final LinkRepository linkRepository;
    private final DataHealthService dataHealthService;
    private final SyncRepository syncRepository;
    private final ResourceService resourceService;
    private final SourceService sourceService;

    public Link findLinkById(UUID id) {
        return linkRepository.findById(id).orElseThrow(() -> new NoSuchElementException(MessageFormat.format("link by id {0} does not exist.", id)));
    }

    public Optional<SyncSpecification> getSync(UUID syncId) {
        return syncRepository.findById(syncId);
    }

    public Optional<BuildLog> getBuildLog(UUID buildId) {
        if (buildId == null) {
            return Optional.empty();
        }
        return buildLogRepository.findById(buildId);
    }

    public SourceTypeEnum processBuildLogSourceTypeField(UUID builder) {
        if (builder == null) {
            return null;
        }
        try {
            ResourceModel resource = resourceService.getResourceModel(builder);
            UUID sourceId = null;
            if (resource.getType().equals(ResourceType.LINK)) {
                Link link = findLinkById(resource.getId());
                sourceId = link.getSourceId();
                DatabaseSourceConfig config = sourceService.getSourceDatabaseSourceConfig(sourceId);
                return config.getDbmsType();
            } else if (resource.getType().equals(ResourceType.SOURCE)) {
                sourceId = resource.getId();
                DatabaseSourceConfig config = sourceService.getSourceDatabaseSourceConfig(sourceId);
                return config.getDbmsType();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

        return null;
    }

    public BuildLog initialBuildLogSetupWithSockets(UUID buildId,
                                                    UUID builder,
                                                    UUID userId,
                                                    BuildTrigger buildTrigger,
                                                    BuildLaunchedBy launchedBy,
                                                    String scriptPath,
                                                    String branch) throws JsonProcessingException {
        // -------- Build Log Maintainence -------
        BuildLog buildLog = new BuildLog();
        buildLog.setId(buildId);
        buildLog.setBuilder(builder);
        buildLog.setSourceType(processBuildLogSourceTypeField(builder));
        buildLog.setLaunchedBy(launchedBy);
        buildLog.setStatus(BuildStatus.ACTIVE);
        buildLog.setStage(BuildStage.STARTING);
        buildLog.setTrigger(buildTrigger);
        buildLog.setStartedAt(new Date());
        buildLog.setStartedBy(userId);
        buildLog.setBranch(branch);
        buildLog.setScriptPath(scriptPath);

        // Setting Details for starting stage
        buildLog.setStartingStageStatus(true);
        buildLog.setStartingStartedAt(new Date());
        buildLog.setStartingFinishedAt(new Date());

        BuildLogMessages message = createFunnelLog(buildLog, "Processing Started", FunnelStatus.INFO, BuildStage.STARTING, null);
        List<BuildLogMessages> currentStartingBuildLogMessages = buildLog.getStartingLogMessages();
        currentStartingBuildLogMessages.add(message);
        buildLog.setStartingLogMessages(currentStartingBuildLogMessages);

        buildLogRepository.save(buildLog);

        // Socket Handling for build log
        template.convertAndSend("/topic/build/" + buildId + "/" + userId, new SocketMessage("logUpdated"));


        // with all the updates to buildLog updating the table in real-time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(buildLog);

        // Socket handling for general build table, where all logs are present
        template.convertAndSend("/topic/build/log", new SocketMessage("newBuildLog", respData));
        template.convertAndSend("/topic/build/log/" + buildLog.getStartedBy(), new SocketMessage("newBuildLog", respData));

        return buildLog;
    }

    public BuildLogMessages createFunnelLog(BuildLog buildLog, String message, FunnelStatus funnelStatus, BuildStage buildStage, String debug) {
        BuildLogMessages messages = new BuildLogMessages();
        messages.setMessage(message);
        messages.setStatus(funnelStatus);
        messages.setStage(buildStage);
        messages.setStartedAt(new Date());

        if (Objects.equals(buildStage, BuildStage.STARTING)) {
            messages.setBuildLogStarting(buildLog);
        } else if (Objects.equals(buildStage, BuildStage.PREPARING)) {
            messages.setBuildLogPreparing(buildLog);
        } else if (Objects.equals(buildStage, BuildStage.RUNNING)) {
            messages.setBuildLogRunning(buildLog);
        } else if (Objects.equals(buildStage, BuildStage.FINISHED)) {
            messages.setBuildLogFinished(buildLog);
        }

        messages.setDebug(debug);
//        buildLogMessagesRepository.save(messages);
        return messages;
    }

    public void updateBuildLog(String message, FunnelStatus status, BuildStage buildStage, UUID buildId) throws Exception {
        updateBuildLog(message, status, buildStage, buildId, null);
    }

    public void updateBuildLog(String message, FunnelStatus status, BuildStage buildStage, UUID buildId, String debug) throws Exception {
        // Finishing Build
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setStatus(status);
        buildLogRequest.setStage(buildStage);
        buildLogRequest.setMessage(message);
        buildLogRequest.setDebug(debug);

        updateFunnelLog(buildLogRequest, buildId);
    }

    public void finishBuild(UUID buildId) throws Exception {
        // Finishing Build
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setStatus(FunnelStatus.INFO);
        buildLogRequest.setStage(BuildStage.FINISHED);
        buildLogRequest.setMessage("Processing Finished");

        updateFunnelLog(buildLogRequest, buildId);
    }

    public void checkpoint(UUID buildId, UUID datasetId, BuildStatus status, UUID transactionId) throws Exception {
        // Finishing Build
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setStatus(FunnelStatus.INFO);
        buildLogRequest.setStage(BuildStage.CHECKPOINT);
        buildLogRequest.setCheckpointDataset(datasetId);
        buildLogRequest.setCheckpointTransactionId(transactionId);
        buildLogRequest.setCheckpointStatus(status);
        buildLogRequest.setMessage("Processing dataset");

        updateFunnelLog(buildLogRequest, buildId, transactionId);
    }

    public void abortBuild(UUID buildId) throws Exception {
        // Finishing Build
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setStatus(FunnelStatus.ABORTED);
        buildLogRequest.setStage(BuildStage.FINISHED);
        buildLogRequest.setMessage("Processing Aborted");

        updateFunnelLog(buildLogRequest, buildId);
    }

    // Method overloading to accept transactionId as optional parameter
    public void updateFunnelLog(BuildMessageRequest request, UUID buildId) throws Exception {
        updateFunnelLog(request, buildId, null);
    }

    public void updateFunnelLog(BuildMessageRequest request, UUID buildId, UUID transactionId) throws Exception {
        log.info(">>>>>>> BUILD LOG REQUEST >>>>>>>>");
        log.info(request.getMessage());
        log.info(String.valueOf(request.getStatus()));
        log.info(String.valueOf(request.getStage()));
        log.info(String.valueOf(buildId.toString()));
        log.info("Spark Application ID : " + request.getSparkApplicationId());
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        // TODO : build | recheck sockets
        Optional<BuildLog> optionalBuildLog = buildLogRepository.findById(buildId);
        if (optionalBuildLog.isEmpty()) {
            return;
        }
        BuildLog buildLog = optionalBuildLog.get();
        String debug = null;
        if (!Objects.equals(request.getDebug(), "None")) {
            debug = request.getDebug();
        }
        if (Objects.equals(request.getStage(), BuildStage.FINISHED)) {
            List<BuildLogMessages> currentFinishedLogMessages = buildLog.getFinishedLogMessages();
            BuildLogMessages message = createFunnelLog(buildLog, request.getMessage(), request.getStatus(), request.getStage(), debug);
            currentFinishedLogMessages.add(message);
            buildLog.setFinishedLogMessages(currentFinishedLogMessages);

            if (buildLog.getFinishedStartedAt() == null) {
                buildLog.setFinishedStartedAt(new Date());
            }
            if (!buildLog.isFinishedStageStatus()) {
                buildLog.setFinishedStageStatus(true);
            }
            buildLog.setFinishedFinishedAt(new Date());
            buildLogRepository.save(buildLog);
        } else if (Objects.equals(request.getStage(), BuildStage.RUNNING) || Objects.equals(request.getStage(), BuildStage.CHECKPOINT)) {
            List<BuildLogMessages> currentRunningLogMessages = buildLog.getRunningLogMessages();
            BuildLogMessages message = createFunnelLog(buildLog, request.getMessage(), request.getStatus(), request.getStage(), debug);
            currentRunningLogMessages.add(message);
            buildLog.setRunningLogMessages(currentRunningLogMessages);

            if (buildLog.getRunningStartedAt() == null) {
                buildLog.setRunningStartedAt(new Date());
            }
            if (!buildLog.isRunningStageStatus()) {
                buildLog.setRunningStageStatus(true);
            }
            buildLog.setRunningFinishedAt(new Date());
            // Setting next stage starting time
            buildLog.setFinishedStartedAt(new Date());

            buildLogRepository.save(buildLog);

        } else if (Objects.equals(request.getStage(), BuildStage.PREPARING)) {
            List<BuildLogMessages> currentPreparingLogMessages = buildLog.getPreparingLogMessages();
            BuildLogMessages message = createFunnelLog(buildLog, request.getMessage(), request.getStatus(), request.getStage(), debug);
            currentPreparingLogMessages.add(message);
            buildLog.setPreparingLogMessages(currentPreparingLogMessages);

            if (buildLog.getPreparingStartedAt() == null) {
                buildLog.setPreparingStartedAt(new Date());
            }
            if (!buildLog.isPreparingStageStatus()) {
                buildLog.setPreparingStageStatus(true);
            }
            buildLog.setPreparingFinishedAt(new Date());
            // Setting next stage starting time
            buildLog.setRunningStartedAt(new Date());
        } else if (Objects.equals(request.getStage(), BuildStage.STARTING)) {
            List<BuildLogMessages> currentStartingLogMessages = buildLog.getStartingLogMessages();
            BuildLogMessages message = createFunnelLog(buildLog, request.getMessage(), request.getStatus(), request.getStage(), debug);
            currentStartingLogMessages.add(message);
            buildLog.setStartingLogMessages(currentStartingLogMessages);

            if (buildLog.getStartingStartedAt() == null) {
                buildLog.setStartingStartedAt(new Date());
            }
            if (!buildLog.isStartingStageStatus()) {
                buildLog.setStartingStageStatus(true);
            }
            buildLog.setStartingFinishedAt(new Date());
            // Setting next stage starting time
            buildLog.setPreparingStartedAt(new Date());
        }

        // TODO : Build status mapping | recheck
        // Status Mapping
        if (Objects.equals(request.getStatus(), FunnelStatus.ERROR) || Objects.equals(request.getStatus(), FunnelStatus.FAILED)) {
            buildLog.setStatus(BuildStatus.FAILED);
        } else if (Objects.equals(request.getStatus(), FunnelStatus.ABORTED)) {
            buildLog.setStatus(BuildStatus.ABORTED);
        } else if (!Objects.equals(buildLog.getStatus(), BuildStatus.ERROR) && !Objects.equals(buildLog.getStatus(), BuildStatus.FAILED) && Objects.equals(buildLog.getStatus(), BuildStatus.ABORTED) && Objects.equals(request.getStatus(), FunnelStatus.INFO)) {
            // If the status of Build turned to failed, error or aborted, then we dont change it, we just add the logs
            buildLog.setStatus(BuildStatus.ACTIVE);
        }

        // On Finish
        if (Objects.equals(request.getStage(), BuildStage.CHECKPOINT)) {
            BuildSpecification buildSpecification = null;
            if (buildSpecificationsRepository.existsBuildSpecificationByBuildIdAndDatasetId(buildLog.getId(), request.getCheckpointDataset())) {
                buildSpecification = buildSpecificationsRepository.findByBuildIdAndDatasetId(buildLog.getId(), request.getCheckpointDataset());
            }

            if (transactionId == null && buildSpecification != null) {
                transactionId = buildSpecification.getTransactionId();
            }

            performTransactionRelatedOperations(request, buildLog, transactionId);
        } else if (Objects.equals(request.getStage(), BuildStage.FINISHED)) {
            if (transactionId != null) {
                datasetMappingService.forceClosureOfTransaction(transactionId, buildLog.getStartedBy());
            }
            // If build status active on finish state as well, then it's a successful build
            if (Objects.equals(buildLog.getStatus(), BuildStatus.ACTIVE)) {
                buildLog.setStatus(BuildStatus.SUCCESS);
            }
            if (Objects.equals(request.getStage(), BuildStage.FINISHED)) {
                buildLog.setStage(BuildStage.FINISHED);
            }

            buildLog.setFinishedAt(new Date());

            String osName = System.getProperty("os.name").toLowerCase();
            log.info(">>>>> OS NAME :" + osName);

            // Check if the OS name contains "windows" then don't copy logs
            if (!osName.contains("windows")) {
                log.info(">>>>>> Deleting pod");
                k8Service.copyDriverLogAndDeleteFinishedPod(buildId, !Objects.equals(buildLog.getStage(), BuildStatus.ABORTED));
            }

            // Data Health Job Status

            if (Objects.equals(buildLog.getTrigger(), BuildTrigger.CONNECT)) {
                // Data Health Job Status
                UUID linkId = buildLog.getBuilder();
                if (linkId != null) {
                    Link link = findLinkById(linkId);
                    dataHealthService.performHealthCheckSpecificType(link.getDatasetId(), link.getBranch(), buildLog, DataHealthTypeEnum.BUILDSTATUS);
                    dataHealthService.performHealthCheckSpecificType(link.getDatasetId(), link.getBranch(), buildLog, DataHealthTypeEnum.JOBSTATUS);
                } else {
//                    log.error("link not present");
                }
            } else if (Objects.equals(buildLog.getTrigger(), BuildTrigger.SYNCHRO)) {
                UUID syncSpecId = buildLog.getBuilder();
                if (syncSpecId != null) {
                    Optional<SyncSpecification> syncSpec = getSync(syncSpecId);
                    if (syncSpec.isPresent()) {
                        SyncSpecification spec = syncSpec.get();
                        dataHealthService.performHealthCheckSpecificType(spec.getDatasetId(), spec.getBranch(), buildLog, DataHealthTypeEnum.SYNCSTATUS);
                    } else {
                        log.error("syncSpec not present");
                    }
                } else {
                    log.error("syncSpec not present");
                }
            }

        }

        // Remaining fields
        if (!Objects.equals(buildLog.getStage(), BuildStage.FINISHED)) {
            // Don't change the build stage, in case it got set to FINISHED
            // It can happen on abort build case, a funnel request might take time to come to boson
            if (Objects.equals(request.getStage(), BuildStage.CHECKPOINT)) {
                buildLog.setStage(BuildStage.RUNNING);
            } else {
                buildLog.setStage(request.getStage());
            }
        }

        if(request.getSparkApplicationId() != null) {
            buildLog.setSparkApplicationId(request.getSparkApplicationId());
        }
        buildLogRepository.save(buildLog);

        // with all the updates to buildLog updating the table in real-time
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String respData = objectMapper.writeValueAsString(buildLog);
        // For all, on table view
        template.convertAndSend("/topic/build/log", new SocketMessage("newBuildLog", respData));
        // For dataset page, users view
        template.convertAndSend("/topic/build/log/" + buildLog.getStartedBy(), new SocketMessage("newBuildLog", respData));
        template.convertAndSend("/topic/build/" + buildId + "/" + buildLog.getStartedBy(), new SocketMessage("logUpdated", respData));
    }

    public void performTransactionRelatedOperations(BuildMessageRequest request, BuildLog buildLog, UUID transactionId) throws Exception {
        if (transactionId != null) {
            if (Objects.equals(request.getCheckpointStatus(), BuildStatus.SUCCESS)) {
                datasetMappingService.successTransaction(transactionId);
            } else if (Objects.equals(request.getCheckpointStatus(), BuildStatus.FAILED)) {
                datasetMappingService.failedTransaction(transactionId);
            } else if (Objects.equals(request.getCheckpointStatus(), BuildStatus.ABORTED)) {
                datasetMappingService.abortTransaction(transactionId);
            }

            // Data Health Job Status
            dataHealthService.performHealthCheckSpecificType(request.getCheckpointDataset(), buildLog.getBranch(), buildLog, DataHealthTypeEnum.JOBSTATUS);
        }
    }

    public Page<BuildLog> getBuildLogPage(@NonNull PageSettings pageSetting, BuildFilters filterCriteria, UUID userId) {
        Sort buildLogSort = pageSetting.buildSort();
        Pageable buildLogPage = PageRequest.of(pageSetting.getPage(), pageSetting.getElementPerPage(), buildLogSort);

        // Pre-processing
        if (filterCriteria.isShowMyBuildsOnly()) {
            List<UUID> myId = new ArrayList<>();
            myId.add(userId);
            filterCriteria.setStartedBy(myId);
        }

        Specification<BuildLog> spec = Specification.where(null);

        // Add conditions based on the filter criteria
        if (filterCriteria.getSearchText() != null && !filterCriteria.getSearchText().isEmpty()) {
            spec = spec.and(BuildLogSpecification.partialSearchOnId(filterCriteria.getSearchText()));
        }

        if (filterCriteria.getTrigger() != null && !filterCriteria.getTrigger().isEmpty()) {
            spec = spec.and(BuildLogSpecification.hasTrigger(filterCriteria.getTrigger()));
        }

        if (filterCriteria.getSourceType() != null && !filterCriteria.getSourceType().isEmpty()) {
            spec = spec.and(BuildLogSpecification.hasSourceType(filterCriteria.getSourceType()));
        }

        if (filterCriteria.getStatus() != null && !filterCriteria.getStatus().isEmpty()) {
            spec = spec.and(BuildLogSpecification.hasStatus(filterCriteria.getStatus()));
        }

        if (filterCriteria.getRangeFrom() != null) {
            spec = spec.and(BuildLogSpecification.isInRangeFrom(filterCriteria.getRangeFrom()));
        }

        if (filterCriteria.getRangeTo() != null) {
            spec = spec.and(BuildLogSpecification.isInRangeTo(filterCriteria.getRangeTo()));
        }

        if (filterCriteria.getFinishRangeFrom() != null) {
            spec = spec.and(BuildLogSpecification.isInFinishRangeFrom(filterCriteria.getFinishRangeFrom()));
        }

        if (filterCriteria.getFinishRangeTo() != null) {
            spec = spec.and(BuildLogSpecification.isInFinishRangeTo(filterCriteria.getFinishRangeTo()));
        }

        if (filterCriteria.getStartedBy() != null && !filterCriteria.getStartedBy().isEmpty()) {
            spec = spec.and(BuildLogSpecification.startedBy(filterCriteria.getStartedBy()));
        }

        if (filterCriteria.getBranch() != null) {
            spec = spec.and(BuildLogSpecification.hasBranch(filterCriteria.getBranch()));
        }

        return buildLogRepository.findAll(spec, buildLogPage);
    }

    @Transactional
    public void createBuildLogEntry(String message, BuildStage stage, FunnelStatus status, UUID buildId, UUID transactionId) throws Exception {
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setMessage(message);
        buildLogRequest.setStage(stage);
        buildLogRequest.setStatus(status);

        updateFunnelLog(buildLogRequest, buildId, transactionId);
    }

    @Transactional
    public void createBuildLogEntryWithDebug(String message, String debug, BuildStage stage, FunnelStatus status, UUID buildId, UUID transactionId) throws Exception {
        BuildMessageRequest buildLogRequest = new BuildMessageRequest();
        buildLogRequest.setDebug(debug);
        buildLogRequest.setMessage(message);
        buildLogRequest.setStage(stage);
        buildLogRequest.setStatus(status);

        updateFunnelLog(buildLogRequest, buildId, transactionId);
    }

    public List<BuildTrigger> getListOfAllBuildTriggers() {
        return List.of(BuildTrigger.UPLOAD,
                BuildTrigger.SQL,
                BuildTrigger.CONNECT,
                BuildTrigger.DATASET,
                BuildTrigger.PYTHON,
                BuildTrigger.SYNCHRO,
                BuildTrigger.COLUMNSTATS,
                BuildTrigger.SCHEDULE);
    }

    public List<BuildStatus> getListOfAllBuildStatuses() {
        return List.of(BuildStatus.ACTIVE,
                BuildStatus.ERROR,
                BuildStatus.SUCCESS,
                BuildStatus.FAILED,
                BuildStatus.ABORTED);
    }
}
