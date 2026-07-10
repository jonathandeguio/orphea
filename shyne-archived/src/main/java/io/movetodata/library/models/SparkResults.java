package io.movetodata.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SparkResults {

    UUID id;
    UUID datasetId;
    String branch;
    String columnName;
    Map<String, Object> results;
}
