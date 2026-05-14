package io.orphea.scheduler.job;

import io.orphea.build.BobEnums.BuildLaunchedBy;
import io.orphea.build.BobEnums.BuildTrigger;
import io.orphea.build.library.services.BuildLogService;
import io.orphea.connect.library.services.UploadService;
import io.orphea.passport.enums.PlatformUsers;
import io.orphea.passport.library.models.User;
import io.orphea.passport.library.service.UserService;
import io.orphea.scheduler.enums.JobExecutionStatus;
import io.orphea.scheduler.library.models.SchedulerJobInfo;
import io.orphea.scheduler.library.models.SchedulerJobLogs;
import io.orphea.scheduler.library.repository.SchedulerRepository;
import io.orphea.scheduler.library.services.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class DirectLoadDataset implements Job {

    @Autowired
    SimpMessagingTemplate template;
    @Resource
    @Autowired
    private SchedulerRepository schedulerRepository;
    @Resource
    private UploadService uploadService;
    @Resource
    private UserService userService;

    @Resource
    private BuildLogService buildLogService;

    @Resource
    private SchedulerService schedulerService;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        UUID jobId = UUID.fromString(context.getJobDetail().getKey().getName());
        SchedulerJobLogs schedulerJobLog = schedulerService.createSchedulerJobLog(jobId);
        try {
            SchedulerJobInfo schedulerJobInfo = schedulerRepository.findById(
                    jobId).orElseThrow();

            schedulerService.handleSchedulerJobLog(JobExecutionStatus.RUNNING, schedulerJobLog.getId(), "Initialized direct load dataset", jobId);

            UUID buildId = UUID.randomUUID();
            User user = userService.getUser(PlatformUsers.PlatformInternal.toString());
            UUID userId = user.getId();
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.RUNNING, schedulerJobLog.getId(), "Build with ID " + buildId + " initialized.", jobId);

            buildLogService.initialBuildLogSetupWithSockets(buildId, schedulerJobInfo.getBuilder(), userId, BuildTrigger.CONNECT, BuildLaunchedBy.MANUAL, null, schedulerJobInfo.getBranch());
            uploadService.DirectLoad(schedulerJobInfo.getBuilder(), userId, buildId, BuildTrigger.CONNECT, BuildLaunchedBy.MANUAL);

            schedulerService.handleSchedulerJobLog(JobExecutionStatus.COMPLETED, schedulerJobLog.getId(), "Build launched, schedule job completed.", jobId);
        } catch (Exception e) {
            log.error("Error in scheduler: {}", e.getMessage());
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.FAILED, schedulerJobLog.getId(), "Schedule failed " + e.getMessage(), jobId);
        }


    }

}
