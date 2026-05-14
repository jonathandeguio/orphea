package io.orphea.dataset.library.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.sql.types.StructType;

import java.util.HashMap;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomSchemaApplyResultDTO {
    StructType schema;
    HashMap<String, String> dateFormatMap;
}
