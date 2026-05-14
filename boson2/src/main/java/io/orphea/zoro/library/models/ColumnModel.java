package io.orphea.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ColumnModel {
    private UUID datasetID;

    private String branch;

    private String column;

    private String type;
}