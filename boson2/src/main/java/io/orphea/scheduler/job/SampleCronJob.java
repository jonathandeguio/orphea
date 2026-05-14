package io.orphea.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
//@RequiredArgsConstructor
//@NoArgsConstructor
public class SampleCronJob extends QuartzJobBean {

//    private final SchedulerService schedulerService;

//    public SampleCronJob(SchedulerService schedulerService) {
//        this.schedulerService = schedulerService;
//    }

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("SampleCronJob Start................");

        System.out.println(context.getJobDetail());
        System.out.println(context.getJobDetail().getKey());
        System.out.println(context.getJobDetail().getKey().getName());

//        SchedulerRepository schedulerRepository = null;

//        System.out.println(schedulerRepository.findAll());

//        System.out.println(schedulerService.getByName(context.getJobDetail().getKey().getName()));
//        System.out.println(schedulerJobInfo.getDatasetId().toString());
//        System.out.println(schedulerJobInfo.getBranch());


//        BuildSpecification buildSpecification = buildSpecificationsRepository.findByDatasetIdAndBranch(buildProperties.getId(), buildProperties.getBranch());
//        String repository = buildSpecification.getRepository();
//        String branch = buildSpecification.getBranch();
//        String scriptPath = buildSpecification.getScriptPath();
//        int cores = buildProperties.getCores();
//        String memory = buildProperties.getMemory();
//        int failureRetries = buildProperties.getFailureRetries();
//
//        return build(principal,
//                repository,
//                branch,
//                scriptPath,
//                cores,
//                memory,
//                failureRetries);


        log.info("SampleCronJob End................");


    }
}