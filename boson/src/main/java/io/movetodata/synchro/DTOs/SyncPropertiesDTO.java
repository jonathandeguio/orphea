package io.movetodata.synchro.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SyncPropertiesDTO {
    private UUID id;
    private UUID datasetId;
    private String branch;
    private String tableName;
    private boolean autoSyncOnBuild;
    private List<List<String>> syncIndexes;
    private UUID sourceId;
    private Boolean isDataMartSyncSpec;
    private UUID dataMartId;
}
