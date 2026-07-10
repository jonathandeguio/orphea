package io.movetodata.dataset.library.models;

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
    private UUID filterId;

    private UUID datasetID;

    private String branch;

    private UUID transactionId;

    private String column;

    private String columnType;

    private String type;
}