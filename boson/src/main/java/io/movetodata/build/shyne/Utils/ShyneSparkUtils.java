package io.movetodata.build.shyne.Utils;

import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.library.dto.LiveDatasetFunnelConfig;
import io.movetodata.connect.library.enums.SourceAuthTypeEnum;
import io.movetodata.dataset.library.models.CustomSchemaModel;
import io.movetodata.dataset.library.models.SchemaModel;
import io.movetodata.kitab.library.enums.ResourceSubtype;
import lombok.AllArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ShyneSparkUtils {
    static ShyneLogging logger = new ShyneLogging();

    public static Dataset<Row> getShyneDF(UUID datasetId, String transactionId, ResourceSubtype branchType, String encoding, SchemaModel schemaModel, int limit, BuildType buildType, SparkSession sparkSession, String physicalEndpoint, LiveDatasetFunnelConfig liveDatasetFunnelConfig) throws Exception {
        CustomSchemaModel customSchema = new CustomSchemaModel();
        StructType schema = null;
        if (schemaModel != null) {
            customSchema = schemaModel.getCustomSchema();
            schema = schemaModel.getSchema();
        }


        Dataset<Row> dfTotal = querySparkForDF(datasetId, branchType, UUID.fromString(transactionId), schema, customSchema, sparkSession, physicalEndpoint, liveDatasetFunnelConfig);

        if (limit > -1) {
            dfTotal = dfTotal.limit(limit);
        }

        if (buildType == BuildType.PREVIEW) {
            if (limit > -1) {
                dfTotal = dfTotal.limit(limit);
            }
        }

        return dfTotal;
    }

    public static Dataset<Row> querySparkForDF(UUID datasetId, ResourceSubtype branchType, UUID transactionId, StructType schema, CustomSchemaModel customSchema, SparkSession sparkSession, String physicalEndpoint, LiveDatasetFunnelConfig liveDatasetFunnelConfig) throws Exception {
        String datasetPath = physicalEndpoint + "/" + datasetId + "/" + transactionId;

        Dataset<Row> dfTotal = null;
        switch (branchType) {
            case JSON:
                if (schema != null) {
                    dfTotal = sparkSession.read()
                            .option("mode", "PERMISSIVE")
                            .option("multiline", "true")
                            .option("allowComments", "true")
                            .option("ignoreNullFields", "false")
                            .json(datasetPath);
                } else {
                    dfTotal = sparkSession.read()
                            .schema(schema)
                            .option("mode", "PERMISSIVE")
                            .option("multiline", "true")
                            .option("allowComments", "true")
                            .option("ignoreNullFields", "false")
                            .json(datasetPath);
                }
                break;
            case XLS:
            case XLSX:
            case CSV:
            case RAWDATASET: {
                if (schema != null) {
                    dfTotal = sparkSession.read()
                            .schema(schema)
                            .format("csv")
                            .option("sep", customSchema.getFieldDelimiter())
                            .option("delimiter", customSchema.getFieldDelimiter())
                            .option("quote", customSchema.getEscapeCharacter())
                            .option("escape", customSchema.getEscapeCharacter())
                            .option("multiLine", true)
                            .option("ignoreLeadingWhiteSpace", true)
                            .option("header", "true")
                            .option("encoding", customSchema.getEncoding())
                            .load(datasetPath);
                } else {
                    dfTotal = sparkSession.read()
                            .format("csv")
                            .option("sep", customSchema.getFieldDelimiter())
                            .option("delimiter", customSchema.getFieldDelimiter())
                            .option("quote", customSchema.getEscapeCharacter())
                            .option("escape", customSchema.getEscapeCharacter())
                            .option("multiLine", true)
                            .option("ignoreLeadingWhiteSpace", true)
                            .option("inferSchema", "true")
                            .option("header", "true")
                            .option("encoding", customSchema.getEncoding())
                            .load(datasetPath);
                }

                break;
            }
            case PARQUET:
            case BUILDDATASET: {
                dfTotal = sparkSession.read()
                        .schema(schema)
                        .format("parquet")
                        .option("header", "true")
                        .parquet(datasetPath);
                break;
            }
            case LIVEDATASET: {
                if (liveDatasetFunnelConfig.getAuthType() != null && liveDatasetFunnelConfig.getAuthType().equals(SourceAuthTypeEnum.KEYPAIR)) {
                    dfTotal = sparkSession.read()
                            .format("jdbc")
                            .option("url", liveDatasetFunnelConfig.getUrl())
                            .option("query", liveDatasetFunnelConfig.getQuery())
                            .option("user", liveDatasetFunnelConfig.getUsername())
                            .option("privateKey", liveDatasetFunnelConfig.getPrivateKey())
                            .option("privateKeyPassphrase", liveDatasetFunnelConfig.getPrivateKeyPassphrase())
                            .option("driver", liveDatasetFunnelConfig.getDriver())
                            .load();
                } else {
                    dfTotal = sparkSession.read()
                            .format("jdbc")
                            .option("url", liveDatasetFunnelConfig.getUrl())
                            .option("query", liveDatasetFunnelConfig.getQuery())
                            .option("user", liveDatasetFunnelConfig.getUsername())
                            .option("password", liveDatasetFunnelConfig.getPassword())
                            .option("driver", liveDatasetFunnelConfig.getDriver())
                            .load();
                }

                break;
            }
            default: {
                throw new RuntimeException("Not a valid type of dataset file type present in branch model " + branchType);
            }
        }

        return dfTotal;
    }

}


