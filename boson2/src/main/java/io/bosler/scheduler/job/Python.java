package io.bosler.scheduler.job;

import io.bosler.bob.library.models.BuildProperties;
import io.bosler.bob.library.models.BuildSpecification;
import io.bosler.bob.library.repository.BuildSpecificationsRepository;
import io.bosler.bob.library.services.BuildService;
import io.bosler.passport.library.service.UserService;
import io.bosler.scheduler.library.models.SchedulerJobInfo;
import io.bosler.scheduler.library.repository.SchedulerRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.util.UUID;

@RequiredArgsConstructor
public class Python implements Job {

    @Resource
    private SchedulerRepository schedulerRepository;

    @Resource
    private UserService userService;

    @Resource
    private BuildSpecificationsRepository buildSpecificationsRepository;

    @Resource
    private BuildService buildService;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {


        SchedulerJobInfo schedulerJobInfo = schedulerRepository.findByJobName(context.getJobDetail().getKey().getName());

        System.out.println(schedulerJobInfo.getDatasetId());
        System.out.println(schedulerJobInfo.getBranch());

        BuildProperties buildProperties = new BuildProperties();

        BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(schedulerJobInfo.getDatasetId(), schedulerJobInfo.getBranch());
        if (buildSpecification.getRepository() != null) {

            UUID userId = userService.getUser("bosler-internal").id;

            UUID repository = buildSpecification.getRepository();
            String branch = buildSpecification.getBranch();
            String scriptPath = buildSpecification.getScriptPath();
            String branchId = buildSpecification.getBranchId();
            String commitId = buildSpecification.getCommitId();
            int cores = buildSpecification.getCores();
            String memory = buildSpecification.getMemory();
            int numberOfExecutors = buildSpecification.getNumberOfExecutors();
            int failureRetries = buildSpecification.getFailureRetries();

            buildService.buildPythonTransform(userId,
                    repository,
                    branch,
                    scriptPath,
                    branchId,
                    commitId,
                    cores,
                    memory,
                    numberOfExecutors,
                    failureRetries,
                    "scheduleByCron");
        }

        System.out.println("Job A is running");
    }

}
