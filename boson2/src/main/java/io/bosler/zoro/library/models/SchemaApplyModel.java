package io.bosler.zoro.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.sql.types.StructType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SchemaApplyModel {
    private StructType schema;
    private CustomSchemaModel customSchema;
}