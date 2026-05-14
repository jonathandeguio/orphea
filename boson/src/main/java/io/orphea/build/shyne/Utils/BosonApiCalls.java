package io.orphea.build.shyne.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.build.BobEnums.BuildLanguage;
import io.orphea.build.library.dto.SourceDataset;
import io.orphea.dataset.library.DTOs.ColumnDTO;
import io.orphea.dataset.library.models.SparkResults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class BosonApiCalls {

    public static Map<String, String> startTransaction(UUID datasetId, String branch) throws Exception {

        try {
            Map<String, String> responseObject = ApiCaller.callApi("/api/kitab/transaction/" + datasetId + "/" + branch + "/start?buildId=" + System.getenv("BUILD_ID"), "GET", System.getenv("BUILD_TOKEN"), null, null);

            return responseObject;
        } catch (Exception e) {
            throw new Exception("Unable to start the transaction on the dataset. There is already another transaction running.");
        }

    }

    public static HashMap<String, String> endTransaction(UUID datasetId, String branch) throws Exception {

        HashMap<String, String> endTransaction = ApiCaller.callApi("/api/kitab/transaction/" + datasetId + "/" + branch + "/end?buildId=" + System.getenv("BUILD_ID"), "GET", System.getenv("BUILD_TOKEN"), null, null);
        return endTransaction;

    }

    public static void sendSparkResults(SparkResults sparkResults, UUID buildId) throws Exception {
        ApiCaller.callApi("/api/dataset/" + buildId + "/sparkResults", "POST", System.getenv("BUILD_TOKEN"), sparkResults, "json");
    }

    public static JsonNode getSchema(UUID datasetId, String branch, UUID transactionId) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseObject = ApiCaller.callApi("/api/dataset/schema/" + datasetId + "/" + branch + "/" + transactionId, "GET", System.getenv("BUILD_TOKEN"), null, "json");
            return objectMapper.readTree(responseObject.get("response"));

        } catch (Exception e) {
            throw new Exception("Schema fetch failed" + e.getMessage());
        }
    }

    public static JsonNode getSyncSpecification(UUID syncId) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseObject = ApiCaller.callApi("/api/synchro/sync/" + syncId, "GET", System.getenv("BUILD_TOKEN"), null, "json");
            log.info(responseObject.get("response"));
            return objectMapper.readTree(responseObject.get("response"));

        } catch (Exception e) {
            throw new Exception("Sync Spec fetch failed " + e.getMessage());
        }
    }

    public static JsonNode getDataMartModel(UUID dataMartId) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseObject = ApiCaller.callApi("/api/platform/getDataMartModel/" + dataMartId, "GET", System.getenv("BUILD_TOKEN"), null, "json");
            log.info(responseObject.get("response"));
            return objectMapper.readTree(responseObject.get("response"));

        } catch (Exception e) {
            throw new Exception("DataMart model fetch failed " + e.getMessage());
        }
    }

    public static JsonNode getDatabaseSourceConfig(UUID sourceId) throws Exception {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> responseObject = ApiCaller.callApi("/api/connect/source/" + sourceId + "/databaseSourceConfig", "GET", System.getenv("BUILD_TOKEN"), null, "json");
            return objectMapper.readTree(responseObject.get("response"));

        } catch (Exception e) {
            throw new Exception("Database Source config fetch failed " + e.getMessage());
        }
    }

    public static HashMap<String, String> preTransform(List<SourceDataset> sources) throws Exception {

        Map<String, Object> payload = new HashMap<>();

        payload.put("branch", System.getenv("BRANCH"));
        payload.put("repositoryId", System.getenv("REPOSITORY_ID"));
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("language", BuildLanguage.SQL);
        payload.put("branchId", System.getenv("BRANCH_ID"));
        payload.put("commitId", System.getenv("COMMIT_ID"));
        payload.put("buildId", System.getenv("BUILD_ID"));
        payload.put("sparkApplicationId", System.getenv("SPARK_APPLICATION_ID"));
        payload.put("sources", sources);
        payload.put("buildType", System.getenv("BUILD_TYPE"));

        HashMap<String, String> response = ApiCaller.callApi("/api/funnel/preTransform", "POST", System.getenv("BUILD_TOKEN"), payload, "json");

        return response;
    }

    public static HashMap<String, String> resolveTarget(String target, List<SourceDataset> sources) throws Exception {

        Map<String, Object> payload = new HashMap<>();

        payload.put("sources", sources);
        payload.put("target", target);
        payload.put("branch", System.getenv("BRANCH"));
        payload.put("repositoryId", System.getenv("REPOSITORY_ID"));
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("language", BuildLanguage.SQL);
        payload.put("branchId", System.getenv("BRANCH_ID"));
        payload.put("commitId", System.getenv("COMMIT_ID"));
        payload.put("buildId", System.getenv("BUILD_ID"));
        payload.put("sparkApplicationId", System.getenv("SPARK_APPLICATION_ID"));
        payload.put("buildTrigger", System.getenv("BUILD_TRIGGER"));
        payload.put("fileName", null);
        payload.put("lineNo", null);

        HashMap<String, String> response = ApiCaller.callApi("/api/funnel/resolveTarget", "POST", System.getenv("BUILD_TOKEN"), payload, "json");

        return response;
    }

    public static HashMap<String, String> postTransform(String datasetId, List<SourceDataset> sources, String transactionId) throws Exception {

        Map<String, Object> payload = new HashMap<>();

        payload.put("target", datasetId);
        payload.put("sources", sources);
        payload.put("transactionId", transactionId);
        payload.put("branch", System.getenv("BRANCH"));
        payload.put("repositoryId", System.getenv("REPOSITORY_ID"));
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("buildId", System.getenv("BUILD_ID"));
        payload.put("buildTrigger", System.getenv("BUILD_TRIGGER"));

        HashMap<String, String> response = ApiCaller.callApi("/api/funnel/postTransform", "POST", System.getenv("BUILD_TOKEN"), payload, "json");

        return response;
    }

    public static void previewPostTransform(List<Map<String, Object>> result,
                                            List<ColumnDTO> schema, UUID repositoryId, String scriptPath, String target) throws Exception {


        Map<String, Object> payload = new HashMap<>();
        payload.put("data", result);
        payload.put("schema", schema);
        payload.put("repositoryId", repositoryId);
        payload.put("scriptPath", scriptPath);
        payload.put("buildId", System.getenv("BUILD_ID"));
        payload.put("target", target);

        HashMap<String, String> apiResponse = ApiCaller.callApi("/api/funnel/previewPostTransform", "POST", System.getenv("BUILD_TOKEN"), payload, "json");

    }

}
