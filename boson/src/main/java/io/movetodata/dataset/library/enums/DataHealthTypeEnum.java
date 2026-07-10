package io.movetodata.dataset.library.enums;

public enum DataHealthTypeEnum {
    // Status
    BUILDSTATUS,
    JOBSTATUS,
    SYNCSTATUS,

    // Time
    BUILDTIME,
    JOBTIME,
    SYNCTIME,

    //  Size
    DATASET_FILE_COUNT,
    ROW_COUNT,
    TRANSACTION_FILE_COUNT,
    TRANSACTION_FILE_SIZE,

    // Content
    ALLOWED_COLUMN_VALUES,

    // Schema
    COLUMN,
    COLUMN_COUNT,
    SCHEMA

}
