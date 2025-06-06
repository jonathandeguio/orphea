package io.bosler.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "zoro_spark_results")
public class SparkResults {

    private @Id
    UUID id;
    UUID datasetId;
    String branch;
    String columnName;
//    HashMap<String, Object> results;
}
