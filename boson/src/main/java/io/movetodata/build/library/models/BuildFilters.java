package io.movetodata.build.library.models;

import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.connect.library.enums.SourceTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BuildFilters {
    private String searchText;

    @Enumerated(EnumType.STRING)
    private List<BuildTrigger> trigger;

    @Enumerated(EnumType.STRING)
    private List<SourceTypeEnum> sourceType;


    @Enumerated(EnumType.STRING)
    private List<BuildStatus> status;

    private Date rangeFrom;
    private Date rangeTo;

    private List<UUID> startedBy;

    private Date finishRangeFrom;
    private Date finishRangeTo;

    private boolean showMyBuildsOnly;
    private String branch;
}
