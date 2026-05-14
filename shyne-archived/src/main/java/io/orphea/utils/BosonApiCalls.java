package io.orphea.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.BobEnums.BuildStage;
import io.orphea.BobEnums.BuildStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BosonApiCalls {

    public static void startTransaction(UUID datasetId, String branch) throws Exception {

        try {
            HashMap<String, String> startTransaction = ApiCaller.callApi("/api/kitab/transaction/" + datasetId + "/" + branch + "/start?buildId=" + System.getenv("BUILD_ID"), "GET", System.getenv("ACCESS_TOKEN"), null, null);
        } catch (Exception e) {
            throw new Exception("Unable to start the transaction on the dataset. There is already another transaction running.");
        }

    }

    public static void endTransaction(UUID datasetId, String branch) throws Exception {

        HashMap<String, String> endTransaction = ApiCaller.callApi("/api/kitab/transaction/" + datasetId + "/" + branch + "/end?buildId=" + System.getenv("BUILD_ID"), "GET", System.getenv("ACCESS_TOKEN"), null, null);

    }

    public static HashMap<String, String> getIdByPath(String path) throws Exception {

        HashMap<String, String> payload = new HashMap<>();

        payload.put("path", path);

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/getIdByPath", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");


        return response;

    }

    public static HashMap<String, String> getPhysicalPath(UUID datasetId, String branch, String transactinId) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/dataset/physicalPath/" + datasetId + "/" + branch + "/" + transactinId, "GET", System.getenv("ACCESS_TOKEN"), null, null);

        return response;

    }

    public static HashMap<String, String> getEncoding(UUID datasetId, String branch) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/dataset/" + datasetId + "/" + branch + "/encoding", "GET", System.getenv("ACCESS_TOKEN"), null, null);

        return response;

    }

    public static HashMap<String, String> createTransaction(String datasetId, String branch) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/dataset/datasetMapping/createTransaction/" + datasetId + "/" + branch + "/" + System.getenv("BUILD_ID"), "GET", System.getenv("ACCESS_TOKEN"), null, null);

        return response;

    }

    public static HashMap<String, String> getBranchType(String datasetId, String branch) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/branch/" + datasetId + "/" + branch + "/getType", "GET", System.getenv("ACCESS_TOKEN"), null, null);

        return response;

    }

    public static HashMap<String, String> getSchema(String datasetId, String branch, String transactionId) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/dataset/schema/" + datasetId + "/" + branch + "/" + transactionId, "GET", System.getenv("ACCESS_TOKEN"), null, null);

        return response;

    }


//    api_response = Orphea().api("POST", f"/api/dataset/datasetMapping/createTransaction/{dataset_id}/{branch}")

    public static void resolveBezierLinks(ArrayList<String> sourceList, String targetDatasetId, String sourceBranch, String targetBranch) throws Exception {
        String repository = System.getenv("REPOSITORY_ID");
        String branch = System.getenv("BRANCH");

        Map<String, Object> payload = new HashMap<>();
        payload.put("sourceDatasets", sourceList);
        payload.put("sourceBranch", sourceBranch);
        payload.put("targetDataset", targetDatasetId);
        payload.put("targetBranch", targetBranch);
        payload.put("repositoryId", repository);
        payload.put("repositoryBranch", System.getenv("BRANCH"));
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("buildId", System.getenv("BUILD_ID"));

        HashMap<String, String> response = ApiCaller.callApi("/api/bezier/resolveBezierLinks", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

    }

    public static void resolveBranch(String repositoryId, String datasetId, String branch) throws Exception {

        Map<String, String> payload = new HashMap<>();
        payload.put("datasetId", datasetId);
        payload.put("repositoryId", repositoryId);
        payload.put("branch", branch);

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/branch/resolveBranch", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

    }

    public static void existBranch(String datasetId, String branch) throws Exception {

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/branch/branches/" + datasetId + "/" + branch, "GET", System.getenv("ACCESS_TOKEN"), null, null);

        ObjectMapper objectMapper = new ObjectMapper();

        String responseCodeExistsBranch = objectMapper.readTree(response.get("responseCode")).asText();

        if (!Objects.equals(responseCodeExistsBranch, "200")) {
            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, branch + " doesn't exist", null);
        }

    }

    public static boolean existsBuildSpecificationWithAnother(String repositoryId, String datasetId, String branch, String language, String scriptPath, String transactionId) throws Exception {
        Map<String, String> payload = new HashMap<>();

        payload.put("datasetId", datasetId);
        payload.put("branch", branch);
        payload.put("transactionId", transactionId);

        payload.put("language", language);
        payload.put("scriptPath", scriptPath);

        payload.put("repository", repositoryId);


        HashMap<String, String> response = ApiCaller.callApi("/api/build/getBuildSpecificationWithAnother", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

        return true;
    }

    public static boolean resolveTargetDataset(String datasetId, String datasetLogicalPath) throws Exception {
        String parentIdResponse = getParentId(datasetLogicalPath).get("response");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parentIdResponse1 = objectMapper.readTree(parentIdResponse);

        String parentId = parentIdResponse1.get("Message").asText();

        String datasetName = datasetLogicalPath.substring(datasetLogicalPath.lastIndexOf('/') + 1);

        try {
            // Check if dataset already exists
            ApiCaller.callApi("/api/kitab/" + datasetId + "/updateTime", "GET", System.getenv("ACCESS_TOKEN"), null, null);
        } catch (Exception e) {
            // Create new dataset if it doesn't exist
            if (parentId == null) {
                return false;
            }
            Map<String, String> payload = new HashMap<>();
            payload.put("name", datasetName);
            payload.put("description", "Build pipeline");
            payload.put("parent", parentId);
            ApiCaller.callApi("/api/kitab/dataset/create?source=build", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");
        }
        return true;
    }


    public static HashMap<String, String> getParentId(String path) throws Exception {

        Map<String, String> payload = new HashMap<>();

        payload.put("path", path);

        HashMap<String, String> response = ApiCaller.callApi("/api/kitab/getParentIdByPath", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

        return response;

    }

    public static void updateBuildLog() throws Exception {

        Map<String, String> payload = new HashMap<>();

        payload.put("buildId", System.getenv("BUILD_ID"));
        payload.put("branch", System.getenv("BRANCH"));
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("sparkApplicationId", System.getenv("SPARK_APPLICATION_ID"));

        HashMap<String, String> response = ApiCaller.callApi("/api/build/updateBuildLog", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

    }


    public static void createBuildSpecification(String datasetId, String branch, String transactionId) throws Exception {

        Map<String, String> payload = new HashMap<>();
        payload.put("repository", System.getenv("REPOSITORY_ID"));
        payload.put("branch", System.getenv("BRANCH"));
        payload.put("branchId", System.getenv("BRANCH_ID"));
        payload.put("commitId", System.getenv("COMMIT_ID"));
        payload.put("datasetId", datasetId);
        payload.put("transactionId", transactionId);
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("language", "sql");
        payload.put("buildId", System.getenv("BUILD_ID"));

        HashMap<String, String> response = ApiCaller.callApi("/api/build/createBuildSpecification", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

    }

    public static void postTransform(String datasetId, String transactionId) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("repository", System.getenv("REPOSITORY_ID"));
        payload.put("branch", System.getenv("BRANCH"));
        payload.put("branchId", System.getenv("BRANCH_ID"));
        payload.put("commitId", System.getenv("COMMIT_ID"));
        payload.put("datasetId", datasetId);
        payload.put("transactionId", transactionId);
        payload.put("scriptPath", System.getenv("SCRIPT_PATH"));
        payload.put("language", "python");
        payload.put("buildId", System.getenv("BUILD_ID"));

        HashMap<String, String> response = ApiCaller.callApi("/api/build/postTransform", "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

    }

    public static void BosonLog(BuildStatus funnelStatus, BuildStage buildStage, String message, String debug) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // Format the date and time to the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
        String formattedDate = now.format(formatter);

        System.out.printf("%s %s %s: %s %s%n", formattedDate, funnelStatus, buildStage, message, debug);

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", funnelStatus);
        payload.put("stage", buildStage);
        payload.put("message", message);
        payload.put("debug", debug);

        HashMap<String, String> response = ApiCaller.callApi(String.format("/api/build/%s/log", System.getenv("BUILD_ID")), "POST", System.getenv("ACCESS_TOKEN"), payload, "json");

        if (funnelStatus == BuildStatus.ERROR) {
            throw new Exception(String.format("%s [status]: %s %s%n", formattedDate, message, debug));
        }
    }
}
