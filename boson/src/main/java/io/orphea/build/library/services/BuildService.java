package io.orphea.build.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.orphea.bezier.library.models.PipelineModel;
import io.orphea.bezier.library.repository.PipelineRepository;
import io.orphea.bezier.library.services.BezierService;
import io.orphea.build.BobEnums.*;
import io.orphea.build.library.dto.NotebookBuildInitiateRequest;
import io.orphea.build.library.dto.SourceDataset;
import io.orphea.build.library.models.BuildLog;
import io.orphea.build.library.models.BuildSpecification;
import io.orphea.build.library.models.SocketMessage;
import io.orphea.build.library.repository.BuildSpecificationsRepository;
import io.orphea.connect.library.models.Link;
import io.orphea.dataset.library.Keys.DatasetMappingKey;
import io.orphea.dataset.library.repository.DatasetMappingRepository;
import io.orphea.dataset.library.services.DatasetMappingService;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.enums.ResourceType;
import io.orphea.kitab.library.models.RepositoryHardwareSpecsModel;
import io.orphea.kitab.library.services.DatasetStatsService;
import io.orphea.kitab.library.services.DatasetWritingTransactionService;
import io.orphea.kitab.library.services.ResourceService;
import io.orphea.platform.library.repository.CacheRepository;
import io.orphea.scheduler.enums.ScheduleTriggerType;
import io.orphea.scheduler.library.models.SchedulerJobInfo;
import io.orphea.scheduler.library.models.TriggerModel;
import io.orphea.scheduler.library.repository.SchedulerRepository;
import io.orphea.scheduler.library.repository.TriggerRepository;
import io.orphea.sharedutils.Redis;
import io.orphea.sharedutils.RedisKeyGenerator;
import io.orphea.synchro.library.services.SynchroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildService {
    private final PipelineRepository pipelineRepository;
    private final SchedulerRepository schedulerRepository;
    private final BuildLogService buildLogService;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final TriggerRepository triggerRepository;
    private final DatasetMappingRepository datasetMappingRepository;
    private final DatasetMappingService datasetMappingService;
    private final K8Service k8Service;
    private final PreviewService previewService;
    private final DatasetWritingTransactionService datasetWritingTransactionService;
    private final CacheRepository cacheRepository;
    private final SynchroService synchroService;
    private final ResourceService resourceService;
    private final BezierService bezierService;
    private final DatasetStatsService datasetStatsService;
    private final FunnelUtils funnelUtils;
    @Autowired
    SimpMessagingTemplate template;

    @Transactional
    public void initiateBuildForNotebook(NotebookBuildInitiateRequest notebookBuildInitiateRequest) throws JsonProcessingException {
        buildLogService.initialBuildLogSetupWithSockets(notebookBuildInitiateRequest.getBuildId(), null, notebookBuildInitiateRequest.getUserId(), BuildTrigger.NOTEBOOK, BuildLaunchedBy.MANUAL, null, notebookBuildInitiateRequest.getBranch());
    }

    public boolean checkForScheduleValidation(UUID sourceDatasetId, String sourceBranch, UUID targetDatasetId, String targetBranch) {
        // Change here in case of advanced schedule, i.e only treat schedules which are of type source
        // Before building, we check by source conditions on all triggers of that particular schedule
        SchedulerJobInfo schedulerJobInfo = schedulerRepository.findByResourceIdAndBranchAndResourceType(targetDatasetId, targetBranch, ResourceType.DATASET);
        log.info(">>>>>>>>>> | " + schedulerJobInfo);
        List<TriggerModel> triggers = schedulerJobInfo.getTriggers();
        for (TriggerModel trigger : triggers) {
            log.info(">>>>>>>>>> | " + trigger.getTriggerValue() + "  " + sourceDatasetId);
            if (UUID.fromString(trigger.getTriggerValue()).equals(sourceDatasetId)) {
                log.info(">>>>>>>>>> | FOUND ONE");
                // If the current dataset is present in build by source triggers
                // Then make
                trigger.setSourceUpdatedByBuild(true);
                triggerRepository.save(trigger);
                break;
            }
        }

        // validate trigger expression
        boolean expression = true;
        for (TriggerModel trigger : triggers) {
            if (trigger.getOperator().equals("and")) {
                expression = expression && trigger.getSourceUpdatedByBuild();
                log.info(">>>>>>>>>> | " + expression);
            } else if (trigger.getOperator().equals("or")) {
                expression = expression || trigger.getSourceUpdatedByBuild();
            } else if (trigger.getOperator().equals("not")) {
                expression = expression && !trigger.getSourceUpdatedByBuild();
            }
        }

        if (expression) {
            // If expression comes true, it means all the condition satisfied and this particular schedule will be fired
            // Hence we need to revoke all these satisfied conditions to prepare for next schedule
            for (TriggerModel trigger : triggers) {
                trigger.setSourceUpdatedByBuild(false);
                triggerRepository.save(trigger);
            }
        }

        return expression;
    }

    public void buildBySource(UUID userId, UUID sourceDatasetId, String sourceBranch) throws Exception {

        log.info(">>>>>>>>>> BUILD BY SOURCE STARTED");
        List<PipelineModel.TargetDatasetAndTargetBranch> targetDatasets = pipelineRepository.findAllBySourceDatasetAndSourceBranch(sourceDatasetId, sourceBranch);

        if (!targetDatasets.isEmpty()) {
            log.info("---------- In target Datasets");
            for (PipelineModel.TargetDatasetAndTargetBranch targetDataset : targetDatasets) {
                if (schedulerRepository.existsByResourceIdAndBranchAndResourceType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), ResourceType.DATASET)) {
                    SchedulerJobInfo schedulerJobInfo = schedulerRepository.findByResourceIdAndBranchAndResourceType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), ResourceType.DATASET);
                    UUID transactionId = datasetMappingRepository.getReferenceById(new DatasetMappingKey(targetDataset.getTargetDataset(), targetDataset.getTargetBranch())).getCurrentTransaction();

                    if (!schedulerJobInfo.getTriggerType().equals(ScheduleTriggerType.SOURCE)) {
                        return;
                    }

                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);

                    if (buildSpecification.getId() != null) {
                        log.info("---------- Build Spec Exists for target");
                        if (checkForScheduleValidation(sourceDatasetId, sourceBranch, targetDataset.getTargetDataset(), targetDataset.getTargetBranch())) {
                            log.info("---------- Schedule Validated");
                            UUID repository = buildSpecification.getRepository();
                            String branch = buildSpecification.getBranch();
                            String scriptPath = buildSpecification.getScriptPath();
                            BuildLanguage buildLanguage = buildSpecification.getLanguage();
                            String branchId = buildSpecification.getBranchId();
                            String commitId = buildSpecification.getCommitId();
                            String fileName = buildSpecification.getFileName();
                            String lineNo = buildSpecification.getLineNo();
                            // Getting Hardware Specs
                            RepositoryHardwareSpecsModel repositoryHardwareSpecsModel = k8Service.getHardwareSpecs(repository, branch, scriptPath, userId);
                            int cores = repositoryHardwareSpecsModel.getCores();
                            String memory = repositoryHardwareSpecsModel.getMemory();
                            int numberOfExecutors = repositoryHardwareSpecsModel.getNumberOfExecutors();
                            int failureRetries = repositoryHardwareSpecsModel.getFailureRetries();

                            previewService.buildTransform(userId, repository, branch, scriptPath, branchId, commitId, cores, memory, numberOfExecutors, failureRetries, BuildTrigger.DATASET, BuildLaunchedBy.SOURCE, BuildType.DEFAULT, buildLanguage, null, null, fileName, lineNo);
                        }
                    }

                }


            }
        }

    }

    public void checkAndBuildBySource(UUID datasetId, String sourceBranch, UUID userId) throws Exception {

        List<PipelineModel.TargetDatasetAndTargetBranch> targetDatasets = pipelineRepository.findAllBySourceDatasetAndSourceBranch(datasetId, sourceBranch);

        if (!targetDatasets.isEmpty()) {

            for (PipelineModel.TargetDatasetAndTargetBranch targetDataset : targetDatasets) {

                boolean schedulerJobInfo = schedulerRepository.existsByResourceIdAndBranchAndResourceType(targetDataset.getTargetDataset(), targetDataset.getTargetBranch(), ResourceType.DATASET);

                if (schedulerJobInfo) { // Only build if it is bySource, if it is under timed schedule then ignore

                    BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(new UUID(0, 0));

                    if (buildSpecification != null && checkForScheduleValidation(datasetId, sourceBranch, targetDataset.getTargetDataset(), targetDataset.getTargetBranch())) {
                        UUID repository = buildSpecification.getRepository();
                        String branch = buildSpecification.getBranch();
                        String scriptPath = buildSpecification.getScriptPath();
                        String branchId = buildSpecification.getBranchId();
                        String commitId = buildSpecification.getCommitId();
                        String fileName = buildSpecification.getFileName();
                        String lineNo = buildSpecification.getLineNo();

                        BuildLanguage buildLanguage = buildSpecification.getLanguage();

                        // Getting Hardware Specs
                        RepositoryHardwareSpecsModel repositoryHardwareSpecsModel = k8Service.getHardwareSpecs(repository, branch, scriptPath, userId);
                        int cores = repositoryHardwareSpecsModel.getCores();
                        String memory = repositoryHardwareSpecsModel.getMemory();
                        int numberOfExecutors = repositoryHardwareSpecsModel.getNumberOfExecutors();
                        int failureRetries = repositoryHardwareSpecsModel.getFailureRetries();

                        previewService.buildTransform(userId, repository, branch, scriptPath, branchId, commitId, cores, memory, numberOfExecutors, failureRetries, BuildTrigger.DATASET, BuildLaunchedBy.SOURCE, BuildType.DEFAULT, buildLanguage, null, null, fileName, lineNo);
                    }

                }

            }
        }

    }

    public Map<String, Object> getPreviewResult(UUID previewId) throws JsonProcessingException {
        Map<String, Object> result = funnelUtils.getPreviewResult(previewId);
        Redis.deleteCache(RedisKeyGenerator.preview(previewId), cacheRepository);
        return result;
    }

    public void handleBuildAbort(UUID buildId, UUID datasetId, String branch, UUID userId) throws Exception {
        log.info(">>>>>>> BUILD ABORT STARTED");
        Optional<BuildLog> buildLogOptional = buildLogService.getBuildLog(buildId);
        if (buildLogOptional.isEmpty()) {
            log.info("No such build log.");
            return;
        }
        BuildLog buildLog = buildLogOptional.get();

        if (buildLog.getStatus() != BuildStatus.ACTIVE) {
            log.info("Build not active");
            return;
        }
        // Model Changes
        buildLogService.abortBuild(buildId);
        log.info(">>>>>>> LOG ABORTED");

        // Handling in dataset mapping
        UUID transactionId = null;
        if (buildSpecificationsRepository.existsBuildSpecificationByBuildId(buildLog.getId())) {
            List<BuildSpecification> buildSpecificationsList = buildSpecificationsRepository.findByBuildId(buildLog.getId());
            for (BuildSpecification buildSpecification : buildSpecificationsList) {
                transactionId = buildSpecification.getTransactionId();
                try {
                    datasetWritingTransactionService.abortTransaction(buildSpecification.getDatasetId(), branch, userId);
                } catch (Exception e) {
                    log.error(">>>>>>> Abort dataset writing transaction failed on " + buildLog.getId() + "/" + datasetId + "/" + branch);
                    log.error(e.getMessage());
                }

                datasetMappingService.updateTransactionStatus(transactionId, BuildStatus.ABORTED);
            }
        }
        try {
            datasetWritingTransactionService.abortTransaction(datasetId, branch, userId);
            log.info(">>>>>>> WRITING TRANSACTION ABORT SUCCESS");
        } catch (Exception e) {
            log.error(">>>>>>> Abort dataset writing transaction failed on " + buildLog.getId() + "/" + datasetId + "/" + branch);
        }

        log.info(">>>>>>> TRANSACTION UPDATED");

        // Socket Handling for dataset page
        // TODO : Shift all sockets related to build : dataset, repo, build page
        if (datasetId != null && branch != null) {
            SocketMessage socketMessage = new SocketMessage();
            socketMessage.setMessage("aborted");
            template.convertAndSend("/topic/build/" + datasetId + "/" + userId, socketMessage);
        }
    }

    // Here branch is target branch, source branch will always be master
    @Transactional
    public void postTransform(UUID target, List<SourceDataset> sources, UUID transactionId, String branch, UUID repositoryId, String scriptPath, UUID buildId, BuildTrigger buildTrigger, UUID userId) throws Exception {
        ResourceSubtype subType = buildTrigger.equals(BuildTrigger.UPLOAD) ? ResourceSubtype.RAWDATASET : ResourceSubtype.BUILDDATASET;
        // Check build schedule and launch builds
        buildBySource(userId, target, branch);
        datasetStatsService.statsAndCacheCleanup(target, branch);
        // Handling sync
        synchroService.postTransformSyncHandling(target, branch, userId, transactionId);
        // Finishing builds
        buildLogService.checkpoint(buildId, target, BuildStatus.SUCCESS, transactionId);
        // Updating resource
        resourceService.updateDatasetOnPostTransform(target, branch, subType, userId, buildId, buildTrigger, transactionId);
        // Post transform operations
        datasetMappingService.postTransformDatasetMappingOperations(target, branch, transactionId, buildId);
        // Resolve Bezier Links
        bezierService.resolveBezierLinks(sources, target, branch, repositoryId, scriptPath, buildId, userId);
    }

    @Transactional
    public void postDataIngestion(UUID userId, Link link, UUID newTransactionId) throws Exception {
        List<SourceDataset> sources = new ArrayList<>();
        sources.add(new SourceDataset(link.getId().toString(), link.getBranch()));
        postTransform(link.getDatasetId(), sources, newTransactionId, link.getBranch(), null, null, link.getBuildId(), BuildTrigger.CONNECT, userId);
    }
}
