package io.bosler.connect.library.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bosler.build.library.enums.WriteModeEnum;
import io.bosler.connect.library.Serializers.LinkConfigSerializer;
import io.bosler.kitab.library.models.IResource;
import io.bosler.scheduler.enums.ScheduleTriggerType;
import io.bosler.sharedutils.BoslerUtils;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonSerialize(using = LinkConfigSerializer.class)
@Table(name = "connect_links")
public class Link implements BoslerUtils, IResource {
    @Id
    private UUID id;
    // TODO: Should also be shift in jdbcLinkConfig, but will require data migration
    private String script;

    private UUID linkConfigId;

    private boolean dataLiveLoad = false;
    private String type;

    private UUID datasetId;
    // TODO BRANCH : Remove this and verify default branch is always set
    private String branch = "master";

    private UUID sourceId;
    @Enumerated(EnumType.STRING)
    private WriteModeEnum writeMode = WriteModeEnum.SNAPSHOT;
    private ScheduleTriggerType trigger = ScheduleTriggerType.NONE;  // none, cron, watcher
    private Date build;  // this should be used to launch builds
    private UUID buildId;  // this should be used to launch builds
    private String cronExpression;
}
