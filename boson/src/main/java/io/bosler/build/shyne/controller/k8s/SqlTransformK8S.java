package io.bosler.build.shyne.controller.k8s;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.library.dto.BuildPreviewResultRequest;
import io.bosler.build.library.dto.LiveDatasetFunnelConfig;
import io.bosler.build.library.dto.SourceDataset;
import io.bosler.build.shyne.Utils.BosonApiCalls;
import io.bosler.build.shyne.Utils.ShyneLogging;
import io.bosler.build.shyne.models.ResourcePathOrId;
import io.bosler.dataset.library.DTOs.ColumnDTO;
import io.bosler.dataset.library.models.SchemaModel;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.sharedutils.SparkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.bosler.build.shyne.Utils.SQLTransformUtils.jacksonStringParser;
import static io.bosler.build.shyne.Utils.SQLTransformUtils.resolveDatasets;
import static io.bosler.build.shyne.Utils.ShyneSparkUtils.getShyneDF;
import static io.bosler.build.shyne.Utils.Utils.getListFromJsonNode;
import static io.bosler.sharedutils.Utils.decodeBase64;

@Slf4j(topic = "K8s SQL PREVIEW/BUILD")
@Component
@RequiredArgsConstructor
public class SqlTransformK8S {
    ShyneLogging logger = new ShyneLogging();


    public void performTransform(Map<String, String> envVars, BuildType buildType, UUID userId) throws Exception {

        StringBuilder content = new StringBuilder();

        try {

            Map<String, List<ResourcePathOrId>> resolveTargetDatasetMap;
            List<ResourcePathOrId> sourcesOriginal;
            List<ResourcePathOrId> targets;
            if (buildType.equals(BuildType.PREVIEW)) {

                String sql = decodeBase64(envVars.get("SQL"));

                BufferedReader reader = new BufferedReader(new StringReader(sql));
                resolveTargetDatasetMap = resolveDatasets(reader, content, envVars.get("TEMP_PATH"), buildType);

                sourcesOriginal = resolveTargetDatasetMap.get("sources");
                targets = resolveTargetDatasetMap.get("targets");

                reader.close();
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/opt/" + envVars.get("REPOSITORY_ID") + "/" + envVars.get("SCRIPT_PATH")), StandardCharsets.UTF_8));
                resolveTargetDatasetMap = resolveDatasets(reader, content, envVars.get("TEMP_PATH"), buildType);

                sourcesOriginal = resolveTargetDatasetMap.get("sources");
                targets = resolveTargetDatasetMap.get("targets");

                reader.close();
            }


            if (sourcesOriginal == null) {
                logger.error("Unable to resolve source dataset found in the code",
                        "This is usually a problem in user code if has not specified CREATE TABLE `TARGET PATH or ID`", buildType);
                throw new Exception("Build failed with error ");
            }

            if (targets.isEmpty()) {
                logger.error("No target dataset found in the code",
                        "This is usually a problem in user code if has not specified CREATE TABLE `TARGET PATH or ID`", buildType);
                throw new Exception("Build failed with error ");
            }

            List<SourceDataset> sourcesCheck = new ArrayList<>();
            for (ResourcePathOrId resourcePathOrId : sourcesOriginal) {
                // TODO BRANCH : Specify branch in sql transforms input
                SourceDataset source = new SourceDataset(resourcePathOrId.getResource(), "master");
                sourcesCheck.add(source);
            }


            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode sqlPreTransform = null;
            JsonNode resolveTarget = null;

            if (buildType.equals(BuildType.PREVIEW)) {
                sqlPreTransform = objectMapper.readTree(BosonApiCalls.preTransform(sourcesCheck).get("response"));
            } else {
                resolveTarget = objectMapper.readTree(BosonApiCalls.resolveTarget(targets.get(0).getResource(), sourcesCheck).get("response"));
                sqlPreTransform = objectMapper.readTree(BosonApiCalls.preTransform(sourcesCheck).get("response"));
            }

            String transactionId = null;
            String targetResponse = null;
            try {
                transactionId = resolveTarget.get("transactionId").asText();
                targetResponse = resolveTarget.get("target").asText();
            } catch (Exception e) {
                log.error("Could not resolve target : {}", targets.get(0).getResource());
            }

            ObjectMapper mapper = new ObjectMapper();
            // start transaction, run the script, end transaction
            perform(content.toString(),
                    sourcesOriginal,
                    getListFromJsonNode(sqlPreTransform, "sources", SourceDataset.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesTransactionIds", String.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesBranchType", ResourceSubtype.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesEncoding", String.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesSchema", SchemaModel.class),
                    targets,
                    getListFromJsonNode(sqlPreTransform, "liveDatasetConfigs", LiveDatasetFunnelConfig.class),
                    targetResponse,
                    envVars.get("BRANCH"),
                    transactionId,
                    envVars.get("PHYSICAL_ENDPOINT"),
                    UUID.fromString(envVars.get("REPOSITORY_ID")),
                    envVars.get("SCRIPT_PATH"),
                    buildType,
                    userId,
                    UUID.fromString(envVars.get("BUILD_ID"))
            );

        } catch (IOException e) {
            logger.error("Build failed with error", e.getMessage(), buildType);
            throw new Exception("Build failed with error " + e);
        } finally {
            FileUtils.deleteDirectory(new File(envVars.get("TEMP_PATH")));
        }
    }

    public void perform(String sql,
                        List<ResourcePathOrId> sourcesOriginal,
                        List<SourceDataset> sourcesResponse,
                        List<String> sourcesTransactionIds,
                        List<ResourceSubtype> sourcesBranchType,
                        List<String> sourcesEncoding,
                        List<SchemaModel> sourcesSchema,
                        List<ResourcePathOrId> targetDatasetOriginal,
                        List<LiveDatasetFunnelConfig> liveDatasetFunnelConfigs,
                        String targetDatasetResponse,
                        String targetBranch,
                        String transactionId,
                        String physicalEndpoint,
                        UUID repositoryId,
                        String scriptPath,
                        BuildType buildType,
                        UUID userId,
                        UUID buildId
    ) throws Exception {
        // As in case of preview, targetDatasetResponse will be null;
        if (buildType.equals(BuildType.PREVIEW)) {
            targetDatasetResponse = targetDatasetOriginal.get(0).getViewName();
        }
        try {

            // Initialize Spark session
            try (SparkSession sparkSession = SparkUtils.createSparkSession(BuildType.DEFAULT)) {
                // Process source datasets

                logger.info("Processing " + targetDatasetResponse, null, buildType);

                for (int i = 0; i < sourcesResponse.size(); i++) {
                    // Get source table
                    Dataset<Row> sourceTable = null;
                    int limit = System.getenv("ROW_LIMIT") != null ? Integer.parseInt(System.getenv("ROW_LIMIT")) : -1;
                    try {
                        sourceTable = getShyneDF(UUID.fromString(sourcesResponse.get(i).getSource()), sourcesTransactionIds.get(i), sourcesBranchType.get(i), sourcesEncoding.get(i), sourcesSchema.get(i), limit, buildType, SparkUtils.createSparkSession(BuildType.DEFAULT), physicalEndpoint, liveDatasetFunnelConfigs.get(i));

                    } catch (Exception e) {
                        logger.error("Error while getting source dataset " + sourcesResponse.get(i), e.getMessage(), buildType);
                        throw new Exception("Error while getting source dataset " + sourcesResponse.get(i));
                    } finally {
                        if (sourcesOriginal.get(i) != null && sourcesOriginal.get(i).getViewName() != null) {
                            sourceTable.createOrReplaceTempView(sourcesOriginal.get(i).getViewName());
                        }
                    }

                }

                try {
                    sparkSession.sql(sql);
                } catch (Exception e) {
                    logger.error("Error while running SQL on " + targetDatasetResponse, e.getMessage(), buildType);
                    throw new Exception("Error while running SQL " + e);
                }

                Dataset<Row> outputTable = sparkSession.table(targetDatasetOriginal.get(0).getViewName());

                // Save output table to Backing FS
                try {

                    if (buildType == BuildType.DEFAULT) {

                        String datasetPath = physicalEndpoint + "/" + targetDatasetResponse + "/" + transactionId;
                        // Start transaction
                        Map<String, String> responseObject = BosonApiCalls.startTransaction(UUID.fromString(targetDatasetResponse), targetBranch);
                        String statusCode = responseObject.get("responseCode");

                        if (!statusCode.equals("200")) {
                            throw new Exception("Error while starting transaction");
                        }

                        outputTable.write().mode(SaveMode.Overwrite).format("parquet").save(datasetPath);
                        // End transaction
                        responseObject = BosonApiCalls.endTransaction(UUID.fromString(targetDatasetResponse), targetBranch);
                        statusCode = responseObject.get("responseCode");
                        if (!statusCode.equals("200")) {
                            throw new Exception("Error while ending transaction");
                        }

                        if (transactionId != null) {
                            //                        buildLogService.checkpoint(buildId, UUID.fromString(targetDatasetResponse), BuildStatus.SUCCESS, UUID.fromString(transactionId));
                        }

                        BosonApiCalls.postTransform(targetDatasetResponse, sourcesResponse, transactionId);
                    } else {
                        List<Map<String, Object>> parsedData = outputTable.limit(100).toJSON().collectAsList().stream().map(e -> {
                            try {
                                return jacksonStringParser(e, new TypeReference<Map<String, Object>>() {
                                });
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }).collect(Collectors.toList());

                        List<ColumnDTO> columnDTOList = new ArrayList<>();

                        for (StructField structField : outputTable.schema().fields()) {
                            columnDTOList.add(new ColumnDTO(structField.name(), structField.name(), structField.dataType().typeName()));
                        }

                        BuildPreviewResultRequest buildPreviewResultRequest = new BuildPreviewResultRequest();

                        buildPreviewResultRequest.setSchema(columnDTOList);
                        buildPreviewResultRequest.setData(parsedData);
                        buildPreviewResultRequest.setTarget(targetDatasetOriginal.get(0).getResource());
                        buildPreviewResultRequest.setBuildId(buildId);
                        buildPreviewResultRequest.setRepositoryId(repositoryId);
                        buildPreviewResultRequest.setScriptPath(scriptPath);

                        if (userId == null) { // if userId is null then it must be running in kubernetes
                            BosonApiCalls.previewPostTransform(parsedData, columnDTOList, repositoryId, scriptPath, targetDatasetResponse);
                        }
                    }


                } catch (Exception e) {
                    logger.error("Unable to write the dataset", e.getMessage(), buildType);
                    throw new Exception("Unable to write the dataset " + e);
                }

                sparkSession.stop();
            }

        } catch (Exception e) {
            logger.error("Build failed with error on dataset " + targetDatasetOriginal.get(0).getResource(), e.getMessage(), buildType);
            throw new Exception("Build failed with error " + e);
        }
    }

}
