package io.movetodata.dataset.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashMap;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "spark_results")
public class SparkResults {

    UUID datasetId;
    String branch;
    String columnName;
    HashMap<String, Object> results;
    private @Id
    UUID id;
}
