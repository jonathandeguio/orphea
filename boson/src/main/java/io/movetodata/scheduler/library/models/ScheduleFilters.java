package io.movetodata.scheduler.library.models;

import io.movetodata.scheduler.enums.JobStatus;
import io.movetodata.scheduler.enums.ScheduleTriggerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleFilters {
    public String searchText;
    
    public Date rangeFrom;
    public Date rangeTo;

    public Date lastExecutionDateFrom;
    public Date lastExecutionDateTo;

    @Enumerated(EnumType.STRING)
    private List<ScheduleTriggerType> scheduleTriggerType;

    @Enumerated(EnumType.STRING)
    private List<JobStatus> jobStatus;
}

