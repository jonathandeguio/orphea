package io.bosler.connect.library.requests;

import io.bosler.build.library.enums.WriteModeEnum;
import io.bosler.connect.library.models.RestAPIRequest;
import io.bosler.dataset.library.DTOs.CsvPreprocessingDTO;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.scheduler.enums.ScheduleTriggerType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LinkRequestDTO {
    private UUID id;
    private UUID parent;
    private String name;
    private String description;
    private String script;
    private boolean dataLiveLoad;
    private String type;
    private UUID datasetId;
    private String branch;
    private UUID sourceId;
    private WriteModeEnum writeMode;
    private ScheduleTriggerType trigger;
    private boolean build;
    private UUID buildId;
    private String cronExpression;

    // Sharepoint
    private String fileId;
    private ResourceSubtype fileType;
    private String sheetName;

    // Folder
    private String subFolder;

    // Webhook
    private List<RestAPIRequest> requests;
    private String responseParam = "@completeresponse";
    private CsvPreprocessingDTO csvPreprocessing;
}
