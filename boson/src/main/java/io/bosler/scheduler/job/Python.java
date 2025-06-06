package io.bosler.scheduler.job;

import io.bosler.build.BobEnums.BuildLanguage;
import io.bosler.build.BobEnums.BuildLaunchedBy;
import io.bosler.build.BobEnums.BuildTrigger;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.library.models.BuildSpecification;
import io.bosler.build.library.repository.BuildSpecificationsRepository;
import io.bosler.build.library.services.K8Service;
import io.bosler.build.library.services.PreviewService;
import io.bosler.dataset.library.Keys.DatasetMappingKey;
import io.bosler.dataset.library.repository.DatasetMappingRepository;
import io.bosler.kitab.library.models.RepositoryHardwareSpecsModel;
import io.bosler.passport.library.service.UserService;
import io.bosler.scheduler.enums.JobExecutionStatus;
import io.bosler.scheduler.library.models.SchedulerJobInfo;
import io.bosler.scheduler.library.models.SchedulerJobLogs;
import io.bosler.scheduler.library.repository.SchedulerRepository;
import io.bosler.scheduler.library.services.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.util.UUID;

import static io.bosler.passport.enums.PlatformUsers.PlatformInternal;

@RequiredArgsConstructor
public class Python implements Job {

    @Resource
    private SchedulerRepository schedulerRepository;

    @Resource
    private UserService userService;

    @Resource
    private BuildSpecificationsRepository buildSpecificationsRepository;

    @Resource
    private PreviewService previewService;

    @Resource
    private K8Service k8Service;

    @Resource
    private SchedulerService schedulerService;

    @Resource
    private DatasetMappingRepository datasetMappingRepository;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        UUID jobId = UUID.fromString(context.getJobDetail().getKey().getName());
        SchedulerJobLogs log = schedulerService.createSchedulerJobLog(jobId);
        schedulerService.handleSchedulerJobLog(JobExecutionStatus.RUNNING, log.getId(), "Initialized build schedule", jobId);
        try {
            // Getting last build spec for the dataset
            SchedulerJobInfo schedulerJobInfo = schedulerRepository.findById(UUID.fromString(context.getJobDetail().getKey().getName())).orElseThrow();
            DatasetMappingKey datasetMappingKey = new DatasetMappingKey(schedulerJobInfo.getResourceId(), schedulerJobInfo.getBranch());
            UUID transactionId = datasetMappingRepository.findById(datasetMappingKey).orElseThrow().getCurrentTransaction();

            BuildSpecification buildSpecification = buildSpecificationsRepository.findByTransactionId(transactionId);
            if (buildSpecification != null) {

                UUID userId = userService.getUser(PlatformInternal.toString()).getId();

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

                previewService.buildTransform(userId, repository, branch, scriptPath, branchId, commitId, cores, memory, numberOfExecutors, failureRetries, BuildTrigger.DATASET, BuildLaunchedBy.CRON, BuildType.DEFAULT, buildLanguage, null, null, fileName, lineNo);
            } else {
                throw new JobExecutionException("Build Spec not present");
            }
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.COMPLETED, log.getId(), "Build launched, schedule job completed", jobId);
        } catch (Exception e) {
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.FAILED, log.getId(), "Schedule failed " + e.getMessage(), jobId);
        }


    }

}
