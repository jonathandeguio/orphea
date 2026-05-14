package io.orphea.build.shyne.controller.local;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.build.BobEnums.BuildLanguage;
import io.orphea.build.BobEnums.BuildStage;
import io.orphea.build.BobEnums.BuildType;
import io.orphea.build.BobEnums.FunnelStatus;
import io.orphea.build.library.dto.LiveDatasetFunnelConfig;
import io.orphea.build.library.dto.PreTransformRequest;
import io.orphea.build.library.dto.SourceDataset;
import io.orphea.build.library.services.BuildLogService;
import io.orphea.build.library.services.FunnelService;
import io.orphea.build.library.services.FunnelUtils;
import io.orphea.build.shyne.Utils.BosonApiCalls;
import io.orphea.build.shyne.models.ResourcePathOrId;
import io.orphea.dataset.library.models.SchemaModel;
import io.orphea.dataset.library.services.DatasetMappingService;
import io.orphea.dataset.library.services.SparkDataService;
import io.orphea.kitab.library.enums.ResourceSubtype;
import io.orphea.kitab.library.services.BranchService;
import io.orphea.platform.library.services.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.orphea.build.shyne.Utils.SQLTransformUtils.resolveDatasets;
import static io.orphea.build.shyne.Utils.Utils.getListFromJsonNode;
import static io.orphea.sharedutils.Utils.decodeBase64;

@Slf4j(topic = "LOCAL SQL PREVIEW/BUILD")
@Component
@RequiredArgsConstructor
public class SqlPreviewLocal {

    private final FunnelUtils funnelUtils;

    private final DatasetMappingService datasetMappingService;
    private final BranchService branchService;
    private final BuildLogService buildLogService;
    private final TransformSQLLocal transformSQLLocal;
    private final PlatformConfigService platformConfigService;
    private final SparkDataService sparkDataService;
    private final FunnelService funnelService;

    @Async
    public void SQLTransform(Map<String, String> envVars, BuildType buildType, UUID userId) throws Exception {
        String defaultBranch = platformConfigService.getPlatformConfig().getDefaultBranch();
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
                buildLogService.updateBuildLog("Unable to resolve source dataset found in the code", FunnelStatus.ERROR, BuildStage.FINISHED, UUID.fromString(envVars.get("BUILD_ID")), "This is usually a problem in user code if has not specified CREATE TABLE `TARGET PATH or ID`");
            }

            if (targets.isEmpty()) {
                buildLogService.updateBuildLog("No target dataset found in the code", FunnelStatus.ERROR, BuildStage.FINISHED, UUID.fromString(envVars.get("BUILD_ID")), "This is usually a problem in user code if has not specified CREATE TABLE `TARGET PATH or ID`");
            }

            List<SourceDataset> sourcesCheck = new ArrayList<>();
            for (ResourcePathOrId resourcePathOrId : sourcesOriginal) {
                // TODO BRANCH : Specify branch in sql transforms input
                SourceDataset source = new SourceDataset(resourcePathOrId.getResource(), defaultBranch);
                sourcesCheck.add(source);
            }


            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode sqlPreTransform = null;
            JsonNode resolveTarget = null;

            if (buildType.equals(BuildType.PREVIEW)) {

                PreTransformRequest preTransformRequest = new PreTransformRequest();

                preTransformRequest.setBranch(envVars.get("BRANCH"));
                preTransformRequest.setRepositoryId(UUID.fromString(envVars.get("REPOSITORY_ID")));
                preTransformRequest.setScriptPath(envVars.get("SCRIPT_PATH"));
                preTransformRequest.setLanguage(BuildLanguage.SQL);
                preTransformRequest.setBranchId(envVars.get("BRANCH_ID"));
                preTransformRequest.setCommitId(envVars.get("COMMIT_ID"));
                preTransformRequest.setBuildId(UUID.fromString(envVars.get("BUILD_ID")));
                preTransformRequest.setSparkApplicationId(null);
                preTransformRequest.setSources(sourcesCheck);
                preTransformRequest.setBuildType(buildType);

                HashMap<String, Object> response = new HashMap<>();
                try {
                    // TODO : remove below code

                    List<SourceDataset> sources = funnelUtils.verifyFunnelSources(sourcesCheck);

                    List<UUID> sourcesTransactionIds = datasetMappingService.getDatasetsTransactions(sources);

                    List<String> sourcesBranchType = branchService.getBranchTypes(sources);

                    List<String> sourcesEncoding = branchService.getEncoding(sources, sourcesTransactionIds);

                    List<Map<String, Object>> sourcesSchema = sparkDataService.getSchemasOfSources(sources, sourcesTransactionIds);

                    List<LiveDatasetFunnelConfig> liveDatasetConfigs = funnelService.getLiveDatasetConfig(sources, sourcesBranchType);

                    response.put("sources", sources);
                    response.put("sourcesTransactionIds", sourcesTransactionIds);
                    response.put("sourcesBranchType", sourcesBranchType);
                    response.put("sourcesEncoding", sourcesEncoding);
                    response.put("sourcesSchema", sourcesSchema);
                    response.put("liveDatasetConfigs", liveDatasetConfigs);

                    response.put("transactionId", UUID.randomUUID());
                    response.put("target", targets);

                    // TODO : remove above code

                } catch (Exception e) {
                    buildLogService.updateBuildLog("Pre transform check failed", FunnelStatus.ERROR, BuildStage.FINISHED, UUID.fromString(envVars.get("BUILD_ID")), e.getMessage());
                }


//                HashMap<String, Object> response = funnelService.p6reTransformWrapper(preTransformRequest, userId);

                sqlPreTransform = objectMapper.valueToTree(response);


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


            List<SchemaModel> schemaModelList = getListFromJsonNode(sqlPreTransform, "sourcesSchema", SchemaModel.class);

            // start transaction, run the script, end transaction
            transformSQLLocal.perform(content.toString(),
                    sourcesOriginal,
                    getListFromJsonNode(sqlPreTransform, "sources", SourceDataset.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesTransactionIds", String.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesBranchType", ResourceSubtype.class),
                    getListFromJsonNode(sqlPreTransform, "sourcesEncoding", String.class),
                    schemaModelList,
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
                    UUID.fromString(envVars.get("BUILD_ID")),
                    envVars
            );
        } catch (IOException e) {
            buildLogService.updateBuildLog("Build failed with error", FunnelStatus.ERROR, BuildStage.FINISHED, UUID.fromString(envVars.get("BUILD_ID")), e.getMessage());
        } finally {
            FileUtils.deleteDirectory(new File(envVars.get("TEMP_PATH")));
        }
    }

}
