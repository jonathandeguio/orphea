package io.orphea.library.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.sql.types.StructType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SchemaModel {

    private StructType schema;
    private CustomSchemaModel customSchema;

}
