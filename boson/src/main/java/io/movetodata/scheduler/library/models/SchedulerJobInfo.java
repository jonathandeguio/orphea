package io.movetodata.scheduler.library.models;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.scheduler.enums.JobExecutionStatus;
import io.movetodata.scheduler.enums.JobStatus;
import io.movetodata.scheduler.enums.ScheduleTriggerType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo {

    @CreationTimestamp
    public Date createdAt = new Date();
    @LastModifiedDate
    public Date updatedAt = new Date();
    public UUID createdBy;
    public UUID updatedBy;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID jobId;
    private String jobName;
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    // TODO BRANCH : remove from here and replace with default and do sanity
    private String branch = "master"; // Only needed in case of dataset
    private String jobGroup; // Dont send from frontend

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;

    @Enumerated(EnumType.STRING)
    private ResourceStatus resourceStatus = ResourceStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private JobStatus previousJobStatus;

    // Build builder, required just in case of dataset and ignite resource type not on subscribe
    // Don't send from frontend
    private UUID builder;
    // Not required from frontend
    private String jobClass;

    @Enumerated(EnumType.STRING)
    private ScheduleTriggerType triggerType; // cron, source

    @OneToMany(targetEntity = TriggerModel.class, cascade = CascadeType.ALL)
    private List<TriggerModel> triggers;

    private Date lastExecution;

    @Enumerated(EnumType.STRING)
    private JobExecutionStatus lastJobExecutionStatus;

    private Long successExecutionCount;
    private Long failureExecutionCount;
}
