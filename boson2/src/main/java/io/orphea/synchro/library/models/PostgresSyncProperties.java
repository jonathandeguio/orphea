package io.orphea.synchro.library.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PostgresSyncProperties {

    private UUID datasetId;
    private String branch;
    private String tableName;
    private boolean enabled;
    private List<List<String>> indexNames;

}
