package io.movetodata.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ColumnStatsModel {
    private UUID datasetId;
    private String branch;
    private String column;
}

