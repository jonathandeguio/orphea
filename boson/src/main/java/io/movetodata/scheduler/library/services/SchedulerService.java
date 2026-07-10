package io.movetodata.scheduler.library.services;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import io.movetodata.build.library.repository.BuildSpecificationsRepository;
import io.movetodata.connect.library.models.Link;
import io.movetodata.connect.library.services.LinkService;
import io.movetodata.dataset.library.Keys.DatasetMappingKey;
import io.movetodata.dataset.library.models.DatasetMappingModel;
import io.movetodata.dataset.library.repository.DatasetMappingRepository;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.passport.exception.BadRequestException;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.scheduler.component.JobScheduleCreator;
import io.movetodata.scheduler.enums.JobExecutionStatus;
import io.movetodata.scheduler.enums.JobStatus;
import io.movetodata.scheduler.enums.ScheduleTriggerType;
import io.movetodata.scheduler.job.DashboardMail;
import io.movetodata.scheduler.job.DirectLoadDataset;
import io.movetodata.scheduler.job.Python;
import io.movetodata.scheduler.job.Simplejob;
import io.movetodata.scheduler.library.Specifications.SchedulerJobInfoSpecification;
import io.movetodata.scheduler.library.models.ScheduleFilters;
import io.movetodata.scheduler.library.models.SchedulerJobInfo;
import io.movetodata.scheduler.library.models.SchedulerJobLogs;
import io.movetodata.scheduler.library.models.TriggerModel;
import io.movetodata.scheduler.library.repository.SchedulerJobLogsRepository;
import io.movetodata.scheduler.library.repository.SchedulerRepository;
import io.movetodata.scheduler.library.repository.TriggerRepository;
import io.movetodata.sharedutils.models.PageSettings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SchedulerService {
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final SchedulerRepository schedulerRepository;
    private final TriggerRepository triggerRepository;
    private final JobScheduleCreator scheduleCreator;
    private final ApplicationContext context;
    private final BuildSpecificationsRepository buildSpecificationsRepository;
    private final DatasetMappingRepository datasetMappingRepository;
    private final SchedulerJobLogsRepository schedulerJobLogsRepository;
    private final LinkService linkService;
    private final PlatformConfigService platformConfigService;

    public static String convertUnixToQuartz(String unixCronExpression) {
        CronDefinition unixCronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        CronParser unixCronParser = new CronParser(unixCronDefinition);
        Cron parsedUnixCron = unixCronParser.parse(unixCronExpression);

        CronMapper cronMapper = CronMapper.fromUnixToQuartz();

        Cron quartzCron = cronMapper.map(parsedUnixCron);

        return quartzCron.asString();
    }

    public SchedulerJobInfo getByResourceIdAndBranch(UUID resourceId, String branch, ResourceType resourceType) {
        return schedulerRepository.findByResourceIdAndBranchAndResourceType(resourceId, branch, resourceType);
    }

    @Transactional
    public void delete(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.findById(jobId).orElseThrow();
        schedulerRepository.delete(getJobInfo);
        schedulerFactoryBean.getScheduler().deleteJob(new JobKey(String.valueOf(getJobInfo.getJobId())));
    }

    @Transactional
    public void pause(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.getReferenceById(jobId);
        getJobInfo.setJobStatus(JobStatus.PAUSED);
        schedulerRepository.save(getJobInfo);

        schedulerFactoryBean.getScheduler().pauseJob(new JobKey(String.valueOf(getJobInfo.getJobId())));
        log.info(">>>>> jobName = [" + jobId + "]" + " paused.");
    }

    @Transactional
    public void resume(UUID jobId) throws SchedulerException {
        SchedulerJobInfo getJobInfo = schedulerRepository.getReferenceById(jobId);
        getJobInfo.setJobStatus(JobStatus.RUNNING);
        schedulerRepository.save(getJobInfo);
        schedulerFactoryBean.getScheduler().resumeJob(new JobKey(String.valueOf(getJobInfo.getJobId())));
        log.info(">>>>> jobName = [" + jobId + "]" + " resumed.");
    }

    @Transactional
    public UUID manageJob(SchedulerJobInfo schedulerJobInfo, TriggerModel trigger, String request) throws Exception {
        if (schedulerJobInfo.getTriggerType().equals(ScheduleTriggerType.CRON)) {
            // Setting Job Class
            if (schedulerJobInfo.getResourceType().equals(ResourceType.DATASET)) {
                schedulerJobInfo.setJobClass(Python.class.getName());
            } else if (schedulerJobInfo.getResourceType().equals(ResourceType.DASHBOARD)) {
                schedulerJobInfo.setJobClass(DashboardMail.class.getName());
            } else if (schedulerJobInfo.getResourceType().equals(ResourceType.CONNECT)) {
                schedulerJobInfo.setJobClass(DirectLoadDataset.class.getName());
            }

            // In case of cron Expression
            log.info("INPUT CRON : " + trigger.getTriggerValue());
            String cronExpression = convertUnixToQuartz(trigger.getTriggerValue());
            log.info("CONVERTED CRON : " + cronExpression);

            triggerRepository.save(trigger);
            schedulerRepository.save(schedulerJobInfo);
            log.info("Job Info: {}", schedulerJobInfo);
            log.info(">>>>> jobName = [" + schedulerJobInfo.getJobId() + "]" + request);

            if (request.equals("update")) {
                log.info("GOING HERE in update");
                return updateScheduleJob(schedulerJobInfo, schedulerJobInfo.getTriggerType(), cronExpression, trigger.getRepeatTime());
            } else if (request.equals("add")) {
                log.info("GOING HERE in add");
                return scheduleNewJob(schedulerJobInfo, schedulerJobInfo.getJobClass(), schedulerJobInfo.getTriggerType(), cronExpression, trigger.getRepeatTime());

            } else {
                throw new BadRequestException("Invalid request type in manage Job");
            }

        } else if (schedulerJobInfo.getTriggerType().equals(ScheduleTriggerType.SOURCE)) {
            // For the formality, done via post transform.
            schedulerJobInfo.setJobClass(Simplejob.class.getName());
            triggerRepository.save(trigger);
            return schedulerRepository.save(schedulerJobInfo).getJobId();
        } else {
            throw new BadRequestException("Invalid trigger type");
        }
    }

    @Transactional
    public UUID scheduleNewJob(SchedulerJobInfo jobInfo, String jobClass, ScheduleTriggerType triggerType, String triggerValue, Long repeatTime) throws ClassNotFoundException, SchedulerException {
        log.info("---------- NEW JOB ------------");
        log.info("JOB CLASS : " + jobClass);
        log.info("Trigger TYpe : " + triggerType);
        log.info("Trigger Value : " + triggerValue);
        log.info("Repeat Time : " + repeatTime);

        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder
                    .newJob((Class<? extends QuartzJobBean>) Class.forName(jobClass))
                    .withIdentity(String.valueOf(jobInfo.getJobId())).build();

            if (!scheduler.checkExists(jobDetail.getKey())) {
                jobDetail = scheduleCreator.createJob(
                        (Class<? extends QuartzJobBean>) Class.forName(jobClass), false, context,
                        String.valueOf(jobInfo.getJobId()), jobInfo.getJobGroup());

                Trigger trigger;
                if (triggerType.equals(ScheduleTriggerType.CRON)) {
                    trigger = scheduleCreator.createCronTrigger(String.valueOf(jobInfo.getJobId()), new Date(),
                            triggerValue, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, TimeZone.getTimeZone(platformConfigService.getPlatformConfig().getTimezone()));
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(String.valueOf(jobInfo.getJobId()), new Date(),
                            repeatTime, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                }
                scheduler.scheduleJob(jobDetail, trigger);

                if (jobInfo.getJobStatus().equals(JobStatus.PAUSED)) {
                    scheduler.pauseJob(jobDetail.getKey());
                } else {
                    jobInfo.setJobStatus(JobStatus.SCHEDULED);
                }

                log.info(">>>>> JOB CREATED = [" + jobInfo.getJobId() + "]" + jobInfo.getJobStatus());
                return schedulerRepository.save(jobInfo).getJobId();
            } else {
                // Reverting the job creation part. As it failed due to some reason
                schedulerRepository.deleteById(jobInfo.getJobId());
                log.error("scheduleNewJobRequest.jobAlreadyExist");
                throw new SchedulerException();
            }
        } catch (ClassNotFoundException e) {
            // Reverting the job creation part. As it failed due to some reason
            schedulerRepository.deleteById(jobInfo.getJobId());
            log.error("Class Not Found - {}", jobInfo.getResourceType(), e);
            throw new ClassNotFoundException();
        } catch (SchedulerException e) {
            // Reverting the job creation part. As it failed due to some reason
            schedulerRepository.deleteById(jobInfo.getJobId());
            log.error(e.getMessage(), e);
            throw new SchedulerException();
        }
    }

    @Transactional
    public UUID updateScheduleJob(SchedulerJobInfo jobInfo, ScheduleTriggerType triggerType, String triggerValue, Long repeatTime) throws SchedulerException {
        log.info("---------- UPDATE JOB ------------");
        log.info("Trigger TYpe : " + triggerType);
        log.info("Trigger Value : " + triggerValue);
        log.info("Repeat Time : " + repeatTime);
        Trigger newTrigger;
        if (triggerType.equals(ScheduleTriggerType.CRON)) {
            newTrigger = scheduleCreator.createCronTrigger(String.valueOf(jobInfo.getJobId()), new Date(),
                    triggerValue, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, TimeZone.getTimeZone(platformConfigService.getPlatformConfig().getTimezone()));
        } else {
            newTrigger = scheduleCreator.createSimpleTrigger(String.valueOf(jobInfo.getJobId()), new Date(), repeatTime,
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(String.valueOf(jobInfo.getJobId())), newTrigger);
            if (jobInfo.getJobStatus().equals(JobStatus.PAUSED)) {
                jobInfo.setJobStatus(JobStatus.PAUSED);
                schedulerFactoryBean.getScheduler().pauseJob(new JobKey(String.valueOf(jobInfo.getJobId())));
            } else {
                jobInfo.setJobStatus(JobStatus.RUNNING);
            }

            log.info(">>>>> Job Updated = [" + jobInfo.getJobId() + "]" + " updated and " + jobInfo.getJobStatus());
            return schedulerRepository.save(jobInfo).getJobId();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
            throw new SchedulerException();
        }
    }

    @Transactional
    public void resolveBuilder(SchedulerJobInfo schedulerJobInfo) {
        // In case of dataset, repository is the builder
        if (schedulerJobInfo.getResourceType().equals(ResourceType.DATASET)) {
            Map<String, Object> link = linkService.existsDatasetLinkCheck(schedulerJobInfo.getResourceId(), schedulerJobInfo.getBranch());
            UUID builder = null;
            if ((boolean) link.get("status")) {
                builder = ((Link) link.get("link")).getId();
                schedulerJobInfo.setResourceType(ResourceType.CONNECT);
            } else {
                DatasetMappingKey datasetMappingKey = new DatasetMappingKey(schedulerJobInfo.getResourceId(), schedulerJobInfo.getBranch());
                DatasetMappingModel datasetMappingModel = datasetMappingRepository.getReferenceById(datasetMappingKey);
                builder = buildSpecificationsRepository.findByTransactionId(datasetMappingModel.getCurrentTransaction()).getRepository();
            }

            schedulerJobInfo.setBuilder(builder);
            schedulerRepository.save(schedulerJobInfo);
        }
        // In case of Connect, the link itself is the builder
        else if (schedulerJobInfo.getResourceType().equals(ResourceType.CONNECT)) {
            schedulerJobInfo.setBuilder(schedulerJobInfo.getResourceId());
            schedulerRepository.save(schedulerJobInfo);
        }
    }

    public Page<SchedulerJobInfo> getSchedulesPage(@NonNull PageSettings pageSetting, ScheduleFilters scheduleFilters) {
        Sort schedulesSort = pageSetting.buildSort();
        Pageable schedulesPage = PageRequest.of(pageSetting.getPage(), pageSetting.getElementPerPage(), schedulesSort);

        Specification<SchedulerJobInfo> spec = Specification.where(null);

        if (scheduleFilters.getSearchText() != null && !scheduleFilters.getSearchText().isEmpty()) {
            spec = spec.and(SchedulerJobInfoSpecification.partialSearchOnId(scheduleFilters.getSearchText()));
        }
        if (scheduleFilters.getScheduleTriggerType() != null && !scheduleFilters.getScheduleTriggerType().isEmpty()) {
            spec = spec.and(SchedulerJobInfoSpecification.hasTrigger(scheduleFilters.getScheduleTriggerType()));
        }
        if (scheduleFilters.getJobStatus() != null && !scheduleFilters.getJobStatus().isEmpty()) {
            spec = spec.and(SchedulerJobInfoSpecification.hasStatus(scheduleFilters.getJobStatus()));
        }
        if (scheduleFilters.getRangeFrom() != null) {
            spec = spec.and(SchedulerJobInfoSpecification.isInRangeFrom(scheduleFilters.getRangeFrom()));
        }
        if (scheduleFilters.getRangeTo() != null) {
            spec = spec.and(SchedulerJobInfoSpecification.isInRangeTo(scheduleFilters.getRangeTo()));
        }
        if (scheduleFilters.getLastExecutionDateFrom() != null) {
            spec = spec.and(SchedulerJobInfoSpecification.isInLastExecutionDateFrom(scheduleFilters.getLastExecutionDateFrom()));
        }
        if (scheduleFilters.getLastExecutionDateTo() != null) {
            spec = spec.and(SchedulerJobInfoSpecification.isInLastExecutionDateTo(scheduleFilters.getLastExecutionDateTo()));
        }

        spec = spec.and(SchedulerJobInfoSpecification.isNotInTrash());

        return schedulerRepository.findAll(spec, schedulesPage);
    }

    public List<ScheduleTriggerType> getListOfAllScheduleTriggerType() {
        return List.of(ScheduleTriggerType.CRON, ScheduleTriggerType.SOURCE);
    }

    public List<JobStatus> getListOfAllJobStatus() {
        return List.of(JobStatus.SCHEDULED,
                JobStatus.RUNNING,
                JobStatus.DELETED,
                JobStatus.PAUSED);
    }

    public SchedulerJobLogs createSchedulerJobLog(UUID jobId) {
        SchedulerJobInfo jobInfo = schedulerRepository.findById(jobId).orElseThrow();

        SchedulerJobLogs log = new SchedulerJobLogs();
        log.setStartedAt(new Date());
        log.setJobExecutionStatus(JobExecutionStatus.RUNNING);
        log.setJobId(jobId);
        schedulerJobLogsRepository.save(log);

        // Setting last execution of cron here
        jobInfo.setLastExecution(new Date());

        schedulerRepository.save(jobInfo);

        return log;
    }

    public void handleSchedulerJobLog(JobExecutionStatus jobExecutionStatus, UUID logId, String logText, UUID jobId) {
        SchedulerJobLogs log = schedulerJobLogsRepository.findById(logId).orElseThrow();
        List<String> logTexts = log.getExecutionLogsDetails();
        logTexts.add(logText);

        log.setJobExecutionStatus(jobExecutionStatus);
        log.setExecutionLogsDetails(logTexts);

        if (Objects.equals(jobExecutionStatus, JobExecutionStatus.COMPLETED) || Objects.equals(jobExecutionStatus, JobExecutionStatus.FAILED)) {
            SchedulerJobInfo jobInfo = schedulerRepository.findById(jobId).orElseThrow();
            jobInfo.setLastJobExecutionStatus(jobExecutionStatus);

            if (Objects.equals(jobExecutionStatus, JobExecutionStatus.COMPLETED)) {
                jobInfo.setSuccessExecutionCount((jobInfo.getSuccessExecutionCount() != null ? jobInfo.getSuccessExecutionCount() : 0) + 1);
            }
            if (Objects.equals(jobExecutionStatus, JobExecutionStatus.FAILED)) {
                jobInfo.setFailureExecutionCount((jobInfo.getFailureExecutionCount() != null ? jobInfo.getFailureExecutionCount() : 0) + 1);
            }

            log.setEndedAt(new Date());
            schedulerRepository.save(jobInfo);
        }

        schedulerJobLogsRepository.save(log);
    }

    public Page<SchedulerJobLogs> getScheduleLogs(PageSettings pageSetting, UUID jobId) {
        Sort schedulesSort = pageSetting.buildSort();
        Pageable schedulesLogsPage = PageRequest.of(pageSetting.getPage(), pageSetting.getElementPerPage(), schedulesSort);

        return schedulerJobLogsRepository.findByJobId(jobId, schedulesLogsPage);
    }
}

