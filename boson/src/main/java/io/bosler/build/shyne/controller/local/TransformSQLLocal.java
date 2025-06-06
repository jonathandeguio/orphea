package io.bosler.build.shyne.controller.local;


import com.fasterxml.jackson.core.type.TypeReference;
import io.bosler.build.BobEnums.BuildStage;
import io.bosler.build.BobEnums.BuildStatus;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.BobEnums.FunnelStatus;
import io.bosler.build.library.dto.BuildPreviewResultRequest;
import io.bosler.build.library.dto.LiveDatasetFunnelConfig;
import io.bosler.build.library.dto.SourceDataset;
import io.bosler.build.library.services.BuildLogService;
import io.bosler.build.library.services.FunnelUtils;
import io.bosler.build.shyne.Utils.BosonApiCalls;
import io.bosler.build.shyne.Utils.ShyneSparkUtils;
import io.bosler.build.shyne.models.ResourcePathOrId;
import io.bosler.dataset.library.DTOs.ColumnDTO;
import io.bosler.dataset.library.models.SchemaModel;
import io.bosler.kitab.library.enums.ResourceSubtype;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.bosler.build.shyne.Utils.SQLTransformUtils.jacksonStringParser;
import static io.bosler.build.shyne.Utils.Utils.isValidUUID;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class TransformSQLLocal {
    //    private final FunnelService funnelService;
    private final FunnelUtils funnelUtils;
    private final BuildLogService buildLogService;
    private final SparkSession sparkSession;
    private final ShyneSparkUtils shyneSparkUtils;

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
                        UUID buildId,
                        Map<String, String> envVars
    ) throws Exception {


        try {
            if (buildType == BuildType.DEFAULT) {
                // Process source datasets
                buildLogService.updateBuildLog("Processing " + targetDatasetResponse, FunnelStatus.INFO, BuildStage.RUNNING, buildId, null);
            }

            for (int i = 0; i < sourcesResponse.size(); i++) {
                // Get source table
                Dataset<Row> sourceTable = null;
                int limit = System.getenv("ROW_LIMIT") != null ? Integer.parseInt(System.getenv("ROW_LIMIT")) : -1;
                try {
                    sourceTable = ShyneSparkUtils.getShyneDF(UUID.fromString(sourcesResponse.get(i).getSource()), sourcesTransactionIds.get(i), sourcesBranchType.get(i), sourcesEncoding.get(i), sourcesSchema.get(i), limit, buildType, sparkSession, physicalEndpoint, liveDatasetFunnelConfigs.get(i));

                } catch (Exception e) {
                    buildLogService.updateBuildLog("Error while getting source dataset " + sourcesResponse.get(i), FunnelStatus.ERROR, BuildStage.FINISHED, buildId, e.getMessage());
                    throw new Exception("Error while getting source dataset " + sourcesResponse.get(i));
                } finally {
                    if (sourcesOriginal.get(i) != null && sourcesOriginal.get(i).getViewName() != null) {
                        sourceTable.createOrReplaceTempView(sourcesOriginal.get(i).getViewName());
                    }
                }


            }

            String outputTableName = targetDatasetOriginal.get(0).getViewName();
            outputTableName = isValidUUID(outputTableName) ? outputTableName.substring(outputTableName.length() - 12) : outputTableName;
//            sparkSession.sql("DROP TABLE IF EXISTS " + outputTableName);
            Dataset<Row> outputTable = null;
            // Execute SQL query
            try {
                sparkSession.sql(sql);
                outputTable = sparkSession.table(outputTableName);
            } catch (Exception e) {
                buildLogService.updateBuildLog("Error while running SQL on " + targetDatasetResponse, FunnelStatus.ERROR, BuildStage.FINISHED, buildId, e.getMessage());
                throw new Exception("Error while running SQL " + e);
            } finally {
                sparkSession.sql("DROP TABLE IF EXISTS " + outputTableName);
            }

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
                        buildLogService.checkpoint(buildId, UUID.fromString(targetDatasetResponse), BuildStatus.SUCCESS, UUID.fromString(transactionId));
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
                        BosonApiCalls.previewPostTransform(parsedData, columnDTOList, repositoryId, scriptPath, targetDatasetOriginal.get(0).getResource());
                    } else {
                        // Resolve circular dependency here
                        // Post transform code is pasted as it is
//                        funnelService.previewPostTransform(buildPreviewResultRequest, userId, repositoryId);
                        try {
                            Thread.sleep(4000);
                            funnelUtils.setPreviewResult(userId, buildPreviewResultRequest, repositoryId, "preview");
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        buildLogService.finishBuild(buildPreviewResultRequest.getBuildId());
                    }
                }


            } catch (Exception e) {
                if (transactionId != null && targetDatasetResponse != null) {
                    buildLogService.checkpoint(buildId, UUID.fromString(targetDatasetResponse), BuildStatus.FAILED, UUID.fromString(transactionId));
                }

                buildLogService.updateBuildLog("Build failed with error on dataset ", FunnelStatus.ERROR, BuildStage.FINISHED, buildId, e.getMessage());
                throw new Exception("Build failed with error on dataset " + e);

            } finally {
                FileUtils.deleteDirectory(new File(envVars.get("TEMP_PATH")));
            }

        } catch (Exception e) {
            buildLogService.updateBuildLog("Build failed with error on dataset " + targetDatasetOriginal.get(0).getResource(), FunnelStatus.ERROR, BuildStage.FINISHED, buildId, e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
