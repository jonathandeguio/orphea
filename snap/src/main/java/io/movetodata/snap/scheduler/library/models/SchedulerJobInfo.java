package io.movetodata.snap.scheduler.library.models;

import io.movetodata.snap.kitab.library.enums.ResourceType;
import io.movetodata.snap.scheduler.enums.JobExecutionStatus;
import io.movetodata.snap.scheduler.enums.JobStatus;
import io.movetodata.snap.scheduler.enums.ScheduleTriggerType;
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
    private String branch = "master"; // Only needed in case of dataset
    private String jobGroup; // Dont send from frontend

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;
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
