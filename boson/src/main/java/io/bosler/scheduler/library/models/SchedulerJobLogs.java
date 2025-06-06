package io.bosler.scheduler.library.models;

import io.bosler.scheduler.enums.JobExecutionStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "scheduler_job_logs")
public class SchedulerJobLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;
    public Date startedAt;
    public Date endedAt;

    @Enumerated(EnumType.STRING)
    private JobExecutionStatus jobExecutionStatus;

    private UUID jobId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "execution_logs_detail")
    @CollectionTable(name = "scheduler_job_logs_execution_logs_details", joinColumns = @JoinColumn(name = "owner_id"))
    private List<String> executionLogsDetails = new ArrayList<>();

}
