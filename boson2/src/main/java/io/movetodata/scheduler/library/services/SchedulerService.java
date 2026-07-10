package io.movetodata.scheduler.library.services;

import io.movetodata.kitab.library.repository.DatasetRepository;
import io.movetodata.scheduler.component.JobScheduleCreator;
import io.movetodata.scheduler.job.Python;
import io.movetodata.scheduler.job.Simplejob;
import io.movetodata.scheduler.library.models.SchedulerJobInfo;
import io.movetodata.scheduler.library.repository.SchedulerRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@Service
public class SchedulerService {

    @Autowired
    private static SchedulerService schedulerService;
    //private static Scanner schedulerRepository;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SchedulerRepository schedulerRepository;

    @Autowired
    private JobScheduleCreator scheduleCreator;

    @Autowired
    private ApplicationContext context;


    public static SchedulerMetaData getMetaData() throws SchedulerException {
        SchedulerMetaData metaData = getMetaData();
        return metaData;
    }


    public List<SchedulerJobInfo> getAllJobList() {
        return schedulerRepository.findAll();
    }

    public SchedulerJobInfo getByDatasetIdAndBranch(UUID datasetId, String branch) {
        return schedulerRepository.findByDatasetIdAndBranch(datasetId, branch);
    }

    public void deleteBydatasetIdandBranch(UUID datasetId, String branch) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.findByDatasetIdAndBranch(datasetId, branch);

        //schedulerRepository.delete(getJobInfo.get(0));
        schedulerRepository.deleteBydatasetIdAndBranch(datasetId, branch);
        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(getJobInfo.getJobName()));
    }

    public void pauseBydatasetIdandBranch(UUID datasetId, String branch) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.findByDatasetIdAndBranch(datasetId, branch);

        getJobInfo.setJobStatus("PAUSED");
        schedulerRepository.save(getJobInfo);

        schedulerFactoryBean.getScheduler().pauseJob(new JobKey(getJobInfo.getJobName()));
    }

    public void resumeBydatasetIdandBranch(UUID datasetId, String branch) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.findByDatasetIdAndBranch(datasetId, branch);

        getJobInfo.setJobStatus("RESUME");
        schedulerRepository.save(getJobInfo);

        schedulerFactoryBean.getScheduler().resumeJob(new JobKey(getJobInfo.getJobName()));
    }

    //public void updateByDatasetIdAndBranch(SchedulerJobInfo schedulerJobInfo ) throws SchedulerException {
    //    Trigger newTrigger;
    //    List<SchedulerJobInfo> getJobInfo = schedulerRepository.findByDatasetIdAndBranch(schedulerJobInfo);
    //    if (getJobInfo.get(0).getCronJob()) {
    //        newTrigger = scheduleCreator.createCronTrigger(getJobInfo.get(0).getJobName(), new Date(),
    //                getJobInfo.get(0).getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
    //    } else {
    //        newTrigger = scheduleCreator.createSimpleTrigger(getJobInfo.get(0).getJobName(), new Date(), getJobInfo.get(0).getRepeatTime(),
    //                SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
    //    }
    //    try {
    //        schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(getJobInfo.get(0).getJobName()), newTrigger);
    //        getJobInfo.get(0).setJobStatus("UPDATED");
    //        if (getJobInfo.size() == 1){
    //            getJobInfo.get(0).setJobStatus("UPDATED");
    //             schedulerRepository.save(getJobInfo.get(0));
    //        }
    //        log.info(">>>>> jobName = [" + schedulerJobInfo +  "]" + " updated.");
    //        //return schedulerRepository.save(getJobInfo.get(0)).getId();
    //    } catch (SchedulerException e) {
    //        log.error(e.getMessage(), e);
    //        throw new SchedulerException();
    //    }
    //}


    public void delete(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.getByJobId(jobId);
        schedulerRepository.delete(getJobInfo);
        log.info(">>>>> jobName = [" + jobId + "]" + " deleted.");
        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(getJobInfo.getJobName()));
    }


    public SchedulerJobInfo get(UUID jobId) {
        log.info(">>>>> jobName = [" + jobId + "]" + " get.");
        return schedulerRepository.getByJobId(jobId);
    }

    public SchedulerJobInfo getByName(String jobName) {
        log.info(">>>>> jobName = [" + jobName + "]" + " get.");
        return schedulerRepository.findByJobName(jobName);
    }


    public boolean startJobNow(SchedulerJobInfo jobInfo) {
        try {
            SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());

            getJobInfo.setJobStatus("SCHEDULED & STARTED");
            schedulerRepository.save(getJobInfo);
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName()));
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " scheduled and started now.");
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    public boolean pause(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.getByJobId(jobId);
        getJobInfo.setJobStatus("PAUSED");
        schedulerRepository.save(getJobInfo);

        schedulerFactoryBean.getScheduler().pauseJob(new JobKey(getJobInfo.getJobName()));
        log.info(">>>>> jobName = [" + jobId + "]" + " paused.");
        return true;
    }

    public boolean resume(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.getByJobId(jobId);
        getJobInfo.setJobStatus("RESUMED");
        schedulerRepository.save(getJobInfo);
        schedulerFactoryBean.getScheduler().resumeJob(new JobKey(getJobInfo.getJobName()));
        log.info(">>>>> jobName = [" + jobId + "]" + " resumed.");
        return true;
    }


    @SuppressWarnings("deprecation")
    public String add(SchedulerJobInfo schedulerJobInfo) throws Exception {

        if (schedulerJobInfo.getCronExpression().length() > 0) {

            // Hack to resolve second thing.
            String cronExpression = schedulerJobInfo.getCronExpression();
            cronExpression = cronExpression.substring(0, cronExpression.length() - 1) + '?';
            schedulerJobInfo.setCronExpression(cronExpression);

            schedulerJobInfo.setJobClass(Python.class.getName());
            schedulerJobInfo.setCronJob(true);
            schedulerJobInfo.setTriggerType("cron");
        } else {
            schedulerJobInfo.setJobClass(Simplejob.class.getName());
            schedulerJobInfo.setCronJob(false);
            schedulerJobInfo.setRepeatTime((long) 1);

            schedulerJobInfo.setTriggerType("bySource");

            schedulerRepository.save(schedulerJobInfo);
            return "Job added by source";
        }

        // set JobName to datasetId
        schedulerJobInfo.setJobName(schedulerJobInfo.getDatasetId().toString());

        if (StringUtils.isEmpty(schedulerJobInfo.getJobId())) {
            log.info("Job Info: {}", schedulerJobInfo);
            log.info(">>>>> jobName = [" + schedulerJobInfo.getJobName() + "]" + " added.");
            return scheduleNewJob(schedulerJobInfo);
        } else {
            log.info(">>>>> jobName = [" + schedulerJobInfo.getJobName() + "]" + " updated.");
            return updateScheduleJob(schedulerJobInfo);
        }

    }

    @SuppressWarnings("deprecation")
    public String update(SchedulerJobInfo schedulerJobInfo) throws Exception {

        if (schedulerJobInfo.getCronExpression().length() > 0) {

            // Hack to resolve second thing.
            String cronExpression = schedulerJobInfo.getCronExpression();
            cronExpression = cronExpression.substring(0, cronExpression.length() - 1) + '?';
            schedulerJobInfo.setCronExpression(cronExpression);

            schedulerJobInfo.setJobClass(Python.class.getName());
            schedulerJobInfo.setCronJob(true);
            schedulerJobInfo.setTriggerType("cron");
        } else {
            schedulerJobInfo.setJobClass(Simplejob.class.getName());
            schedulerJobInfo.setCronJob(false);
            schedulerJobInfo.setRepeatTime((long) 1);

            schedulerJobInfo.setTriggerType("bySource");

            schedulerRepository.save(schedulerJobInfo);
            return "Job added by source";
        }

        // set JobName to datasetId
        schedulerJobInfo.setJobName(schedulerJobInfo.getDatasetId().toString());

        if (StringUtils.isEmpty(schedulerJobInfo.getJobId())) {
            log.info("Job Info: {}", schedulerJobInfo);
            log.info(">>>>> jobName = [" + schedulerJobInfo.getJobName() + "]" + " added.");
            return scheduleNewJob(schedulerJobInfo);
        } else {
            log.info(">>>>> jobName = [" + schedulerJobInfo.getJobName() + "]" + " updated.");
            return updateScheduleJob(schedulerJobInfo);
        }

    }

    @SuppressWarnings("unchecked")
    private String scheduleNewJob(SchedulerJobInfo jobInfo) throws ClassNotFoundException, SchedulerException {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder
                    .newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {

                jobDetail = scheduleCreator.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()), false, context,
                        jobInfo.getJobName(), jobInfo.getJobGroup());

                Trigger trigger;
                if (jobInfo.getCronJob()) {
                    trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(),
                            jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(),
                            jobInfo.getRepeatTime(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                }
                scheduler.scheduleJob(jobDetail, trigger);
                jobInfo.setJobStatus("SCHEDULED");
                log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " scheduled.");
                return schedulerRepository.save(jobInfo).getJobId();
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
                throw new SchedulerException();
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
            throw new ClassNotFoundException();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerException();
        }
    }

    private String updateScheduleJob(SchedulerJobInfo jobInfo) throws SchedulerException {
        Trigger newTrigger;
        if (jobInfo.getCronJob()) {
            newTrigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(),
                    jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        } else {
            newTrigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()), newTrigger);
            jobInfo.setJobStatus("EDITED & SCHEDULED");
            log.info(">>>>> jobName = [" + jobInfo.getJobName() + "]" + " updated and scheduled.");
            return schedulerRepository.save(jobInfo).getJobId();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerException();
        }
    }
}

