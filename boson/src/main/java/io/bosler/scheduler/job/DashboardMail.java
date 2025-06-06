package io.bosler.scheduler.job;

import io.bosler.passport.library.models.NotificationPreferences;
import io.bosler.passport.library.models.User;
import io.bosler.passport.library.repository.NotificationPreferencesRepository;
import io.bosler.scheduler.enums.JobExecutionStatus;
import io.bosler.scheduler.library.models.SchedulerJobInfo;
import io.bosler.scheduler.library.models.SchedulerJobLogs;
import io.bosler.scheduler.library.repository.SchedulerRepository;
import io.bosler.scheduler.library.services.MailService;
import io.bosler.scheduler.library.services.SchedulerService;
import io.bosler.subscribe.library.models.SubscriptionModel;
import io.bosler.subscribe.library.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.util.UUID;

import static io.bosler.sharedutils.Utils.convertBackStringifyArr;
import static io.bosler.sharedutils.Utils.isValidUuid;

@RequiredArgsConstructor
@DisallowConcurrentExecution
public class DashboardMail implements Job {
    @Resource
    private SubscriptionRepository subscriptionRepository;

    @Resource
    private SchedulerRepository schedulerRepository;

    @Resource
    private MailService mailService;

    @Resource
    private SchedulerService schedulerService;

    @Resource
    private SessionFactory sessionFactory;

    @Resource
    private NotificationPreferencesRepository notificationPreferencesRepository;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        System.out.println("Starting Dashboard Job !");
        UUID jobId = UUID.fromString(context.getJobDetail().getKey().getName());
        SubscriptionModel subscriptionModel = subscriptionRepository.findByJobId(jobId);
        SchedulerJobInfo schedulerJobInfo = schedulerRepository.findById(jobId).orElseThrow();
        String[] users = convertBackStringifyArr(subscriptionModel.getSendTo());

        SchedulerJobLogs log = schedulerService.createSchedulerJobLog(jobId);
        schedulerService.handleSchedulerJobLog(JobExecutionStatus.RUNNING, log.getId(), "Initialized mail sending", jobId);
        /*
            Session Error Solution
            At other places can be tried with @Transaction
         */
        try (Session session = sessionFactory.openSession()) {
            for (String userId : users) {
                String name = "user";
                String email = null;

                if (isValidUuid(userId)) {
                    User user = session.get(User.class, UUID.fromString(userId));
                    UUID notificationPreferencesId = user.getNotificationPreferencesId();

                    if (notificationPreferencesId != null) {
                        NotificationPreferences notificationPreferences = notificationPreferencesRepository.findById(notificationPreferencesId).orElseThrow();
                        if (!notificationPreferences.getSubscription())
                            continue;
                    }
                    email = user.getEmail();
                    name = user.getName();
                } else {
                    email = userId;
                }


                String dashboardLink = System.getenv("BASE_URL") + "/portal/kepler/dashboard" + "/" + subscriptionModel.getResourceId() + "/" + subscriptionModel.getTabId();
                mailService.sendMail(email, subscriptionModel.getSubject(), subscriptionModel.getBody(), name, dashboardLink);
                schedulerService.handleSchedulerJobLog(JobExecutionStatus.RUNNING, log.getId(), "Sent to " + email, jobId);
            }
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.COMPLETED, log.getId(), "Mail sending completed", jobId);
        } catch (Exception error) {
            schedulerService.handleSchedulerJobLog(JobExecutionStatus.FAILED, log.getId(), "Mail sending failed " + error.getMessage(), jobId);
            System.out.println(error);
            throw error;
        }
    }

}
