package io.movetodata.scheduler.library.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "schedule_trigger")
public class TriggerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID triggerId;
    private String triggerValue; // String value containing cronExpression or source ds
    private String operator = "and"; // and
    private Long repeatTime = 1L; // Needed in by source case
    //    private String jobClass; // This isnt sent from frontend, it will be replaced in backend based on resource type
    private Date lastBuild; // source , to keep track about the source last build
    private Boolean sourceUpdatedByBuild = false; // source, if source got updated via build then make it true, after all updated it make all false, and make last build as new build date
}
