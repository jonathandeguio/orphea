package io.movetodata.scheduler.job;

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

        
        
        

//        SchedulerRepository schedulerRepository = null;

//        

//        
//        
//        


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