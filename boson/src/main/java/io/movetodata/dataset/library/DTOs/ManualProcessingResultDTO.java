package io.movetodata.dataset.library.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.HashMap;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualProcessingResultDTO {
    Dataset<Row> dfTotal;
    HashMap<String, String> dateFormatMap;
}
