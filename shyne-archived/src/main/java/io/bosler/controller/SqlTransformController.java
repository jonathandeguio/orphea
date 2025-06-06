package io.bosler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bosler.BobEnums.BuildStage;
import io.bosler.BobEnums.BuildStatus;
import io.bosler.utils.BosonApiCalls;
import io.bosler.utils.SparkUtils;
import io.bosler.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.jvnet.hk2.annotations.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bosler.utils.BackingFS.deleteDatasetFiles;
import static io.bosler.utils.BackingFS.getDatasetPath;

@Service
@RequiredArgsConstructor
public class SqlTransformController {

    public void transform(UUID repositoryId, String branch, String scriptPath) throws Exception {

        BufferedReader reader = null;

        BosonApiCalls.updateBuildLog();

        File file = new File(System.getenv("BOSLER_MOUNT_PATH") + "/tmp/BoslerTransformTemporary/" + repositoryId);

        BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.PREPARING, "Preparing build", null);
        BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.PREPARING, "Cloning REPOSITORY and Checkout", null);
        if (!file.exists()) {
            Git.cloneRepository()
                    .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
                    .setDirectory(file)
                    .call();
        } else {
            FileUtils.deleteDirectory(file);

            Git.cloneRepository()
                    .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
                    .setDirectory(file)
                    .call();
        }

        BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.PREPARING, "REPOSITORY Cloned and Checkout successful", null);

        // TODO : checkout branch based on commit_id

//        if (System.getenv().containsKey("COMMIT_ID")) {
//
//
//
//            if (System.getenv("COMMIT_ID") != null) {
//
//                String commitId = System.getenv("COMMIT_ID");
//
//                BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.PREPARING, "COMMIT_ID "+ commitId + " found using it for checkout " , null);
//                Repository repository = Utils.getRepository(String.valueOf(repositoryId), branch);
//
//                Git git = new Git(repository);
//
//                ObjectId commitObjectId = repository.resolve(commitId);
//
//                if (commitObjectId != null) {
//                    git.checkout().setStartPoint(commitObjectId.getName()).call();
//                    System.out.println("Checked out commit: " + commitId);
//                } else {
//                    System.out.println("Commit not found: " + commitId);
//
//                    BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.PREPARING, "Not able to checkout.", null);
//                    throw new Exception("Not able to checkout.");
//                }
//
////                Ref checkoutCommand1 = new Git(repository).checkout().setName(commitId).call();
////
////                if (repository.getObjectDatabase().exists() && Utils.branchExists(String.valueOf(repositoryId), branch)) {
////
////                    Ref checkoutCommand = new Git(repository).checkout().setName(branch).call();
////                } else {
////
////                    BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.PREPARING, "Not able to checkout.", null);
////                    throw new Exception("Not able to checkout.");
////                }
//            }
//        }

        reader = new BufferedReader(new InputStreamReader(new FileInputStream(System.getenv("BOSLER_MOUNT_PATH") + "/tmp/BoslerTransformTemporary/" + repositoryId + "/" + scriptPath), StandardCharsets.UTF_8));

//        reader.lines().forEach(System.out::println);

        StringBuilder content = new StringBuilder();

        try {

            Map<String, Map<String, String>> resolveTargetDatasetMap = resolveDatasets(reader, content);

            Map<String, String> sources = resolveTargetDatasetMap.get("sources");
            Map<String, String> targets = resolveTargetDatasetMap.get("targets");

//            System.out.println("targets " + targets);
//            System.out.println("sources " + sources);


            reader.close();

            String targetDatasetLogicalPath = null;
            String targetDatasetId = null;
            for (Map.Entry<String, String> entry : targets.entrySet()) {
                targetDatasetLogicalPath = entry.getKey();
                targetDatasetId = entry.getValue();
            }

//            BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.RUNNING, "Starting Transform on " + targetDatasetLogicalPath, null);

            // check if build spec exists with some other dataset, if yes then abort
            if (targetDatasetId == null) {

                BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Target dataset is not resolved", "dataset " + targetDatasetLogicalPath);
                throw new Exception("Target dataset is not resolved" + targetDatasetId);
            }

            BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.RUNNING, "Awaiting resource allocation", null);


            // Create transaction here :
            ObjectMapper objectMapper = new ObjectMapper();

            // check branch exists , the target dataset should have at least master branch when you are building on non-master
            if (!Objects.equals(branch, "master")) {
                BosonApiCalls.existBranch(targetDatasetId, "master");
            }

            String transactionId = objectMapper.readTree(BosonApiCalls.createTransaction(targetDatasetId, branch).get("response")).get("transactionId").asText();

            BosonApiCalls.existsBuildSpecificationWithAnother(String.valueOf(repositoryId), targetDatasetId, branch, "sql", scriptPath, transactionId);

            // resolve sources to Ids and put in a map
            ArrayList<String> sourcesList = new ArrayList();
            for (Map.Entry<String, String> entry : sources.entrySet()) {

                if (Objects.equals(entry.getValue(), targetDatasetId)) {
                    BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Source and target can not be same", String.format("Same source(%s) and target(%s) datasets found : ", entry.getKey(), targetDatasetLogicalPath));
                    throw new Exception("Source and target can not be same");
                }

                String sourceDatasetId = entry.getValue();
                sourcesList.add(sourceDatasetId);

            }



            BosonApiCalls.createBuildSpecification(targetDatasetId, branch, transactionId);

            BosonApiCalls.resolveBranch(String.valueOf(repositoryId), targetDatasetId, branch);

            // start transaction, run the script, end transaction
            transformSQL(content.toString(), sources, "master", targetDatasetId, branch, targetDatasetLogicalPath, transactionId);

            BosonApiCalls.resolveBezierLinks(sourcesList, targetDatasetId, "master", branch);

            // temp files
            deleteDatasetFiles(UUID.fromString(targetDatasetId), "/BoslerTransformTemporary/");


        } catch (IOException e) {
            e.printStackTrace();
            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Build failed with error", e.getMessage());
            throw new Exception("Build failed with error " + e);
        }
    }

    public void transformSQL(String sql, Map<String, String> sources, String sourceBranch, String targetDatasetId, String targetBranch, String targetDatasetLogicalPath, String transactionId) throws Exception {

        try {

            // Delete temporary files
            try {
//                System.out.println("Deleting temp files 1");

//                System.out.println("Deleting temporary files : " + getDatasetPath(UUID.fromString(targetDatasetId)) + "/BoslerTransformTemporary/");

                FileUtils.deleteDirectory(new File(getDatasetPath(UUID.fromString(targetDatasetId)) + "/BoslerTransformTemporary/"));
            } catch (Exception e) {
                System.out.println("deleting files " + e);
            }

            // Initialize Spark session
            SparkSession sparkSession = SparkUtils.sparkSession();
            // Process source datasets

            BosonApiCalls.BosonLog(BuildStatus.INFO, BuildStage.RUNNING, "Processing " + targetDatasetId, null);

            for (Map.Entry<String, String> entry : sources.entrySet()) {
                String sourceDatasetLogicalPath = entry.getKey();
                String sourceDatasetId = entry.getValue();
                // Get source table
                Dataset<Row> sourceTable = null;
                try {
                    sourceTable = SparkUtils.getSparkDF(UUID.fromString(sourceDatasetId), sourceBranch, -1);
                } catch (Exception e) {

                    e.printStackTrace();
                    BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Error while getting source dataset " + sourceDatasetId + " " + sourceDatasetLogicalPath, e.getMessage());
                    throw new Exception("Error while getting source dataset " + sourceDatasetId + " " + sourceDatasetLogicalPath);
                }

                // Split the path using the forward slash character as a delimiter
                String[] pathParts = sourceDatasetLogicalPath.split("/");
                String sourceView = pathParts[pathParts.length - 1].replaceAll("[^a-zA-Z0-9]", "_");

                String[] parts = sourceDatasetId.split("-");
                String lastPart = parts[parts.length - 1];

                sourceTable.createOrReplaceTempView(lastPart);

//                System.out.println("sourceTable table");
//                sourceTable.show();
//                System.out.println("sourceTable table");
            }


            // Execute SQL query
//            System.out.println(sql);
            try {
                sparkSession.sql(sql);
            } catch (Exception e) {

                e.printStackTrace();
                BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Error while running SQL", e.getMessage());
                throw new Exception("Error while running SQL" + e);
            }

            String[] parts = targetDatasetId.split("-");
            String lastPart = parts[parts.length - 1];

            Dataset<Row> outputTable = sparkSession.table(lastPart);

            // Start transaction
            BosonApiCalls.startTransaction(UUID.fromString(targetDatasetId), targetBranch);

            // Save output table to Backing FS
            try {

                ObjectMapper objectMapper = new ObjectMapper();

                String datasetPath = objectMapper.readTree(BosonApiCalls.getPhysicalPath(UUID.fromString(targetDatasetId), targetBranch, transactionId).get("response")).get("physicalPath").asText();

                outputTable.write().mode(SaveMode.Overwrite).format("parquet").save(datasetPath);

                BosonApiCalls.postTransform(targetDatasetId, transactionId);

            } catch (Exception e) {

                e.printStackTrace();
                BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Unable to write the dataset", e.getMessage());
                throw new Exception("Unable to write the dataset " + e);
            }

//            System.out.println("outputTable table");
//            outputTable.show();
//            System.out.println("outputTable table");

            // End transaction
            BosonApiCalls.endTransaction(UUID.fromString(targetDatasetId), targetBranch);

            // Stop Spark session
            sparkSession.stop();
            // Delete temporary files
            try {
//                System.out.println("Deleting temp files 2");
                FileUtils.deleteDirectory(new File(getDatasetPath(UUID.fromString(targetDatasetId)) + "/BoslerTransformTemporary/"));
                FileUtils.deleteDirectory(new File(System.getenv("BOSLER_MOUNT_PATH") + "/tmp/BoslerTransformTemporary/" + System.getenv("REPOSITORY_ID")));
            } catch (Exception e) {
                System.out.println("deleting files " + e);
            }

        } catch (Exception e) {

            e.printStackTrace();
            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Build failed with error", e.getMessage());
            throw new Exception("Build failed with error " + e);
        }
    }

    public static Map<String, Map<String, String>> resolveDatasets(BufferedReader reader, StringBuilder content) throws Exception {
        Map<String, String> sources = new HashMap<>();
        Map<String, String> targets = new HashMap<>();
        Pattern commentPattern = Pattern.compile("(?m)^\\s*--.*");

        String line;
        while ((line = reader.readLine()) != null) {

            line = commentPattern.matcher(line).replaceAll("");  // remove comments

            String targetDatasetLogicalPath = getTargetDatasetLogicalPath(line); // get Logical path from the line if it is there ... like /Projects/Test/Data/dataset
            if (targetDatasetLogicalPath != null) {

                String targetDatasetId = getOrCreateDatasetId(targetDatasetLogicalPath);  // this is to create or get dataset Id, this makes api calls

                String[] parts = targetDatasetId.split("-");
                String lastPart = parts[parts.length - 1];

                line = replaceTargetDataset(line, targetDatasetLogicalPath, lastPart, targetDatasetId);
                targets.put(targetDatasetLogicalPath, targetDatasetId);
            } else {

                Matcher sourceMatcher = getSourceMatcher(line);

                while (sourceMatcher.find()) {
                    String sourceDatasetLogicalPath = sourceMatcher.group(1);
                    String sourceDatasetId = getSourceDatasetId(sourceDatasetLogicalPath);

                    String[] parts = sourceDatasetId.split("-");
                    String lastPart = parts[parts.length - 1];

                    line = line.replace(sourceDatasetLogicalPath, lastPart);
                    if (!sources.containsKey(sourceDatasetLogicalPath)) {
                        sources.put(sourceDatasetLogicalPath, sourceDatasetId);
                    }
                }
            }

            content.append(line).append("\n");
        }

        Map<String, Map<String, String>> result = new HashMap<>();

        result.put("targets", targets);
        result.put("sources", sources);

        return result;
    }

    private static String getTargetDatasetLogicalPath(String line) {
        Pattern targetPattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+`(.*)`");
        Matcher targetMatcher = targetPattern.matcher(line);
        return targetMatcher.find() ? targetMatcher.group(1) : null;
    }

    private static String getOrCreateDatasetId(String targetDatasetLogicalPath) throws Exception {
        String targetDatasetId;
        String targetDatasetResponse = BosonApiCalls.getIdByPath(targetDatasetLogicalPath).get("response");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode targetDatasetResponseJson = objectMapper.readTree(targetDatasetResponse);

        if (Objects.equals(targetDatasetResponseJson.get("Status").asText(), "true") &&
                Objects.equals(targetDatasetResponseJson.get("Type").asText(), "DATASET")) {

            targetDatasetId = targetDatasetResponseJson.get("Message").asText();
        } else {
            BosonApiCalls.resolveTargetDataset(null, targetDatasetLogicalPath);
            String response1 = BosonApiCalls.getIdByPath(targetDatasetLogicalPath).get("response");
            JsonNode targetDatasetResponseJson1 = objectMapper.readTree(response1);

            targetDatasetId = targetDatasetResponseJson1.get("Message").asText();

        }

        return targetDatasetId;
    }

    private static String replaceTargetDataset(String line, String targetDatasetLogicalPath, String targetView, String targetDatasetId) throws Exception {
        return line.replace("`" + targetDatasetLogicalPath + "`",
                "`" + targetView + "`" + "\n USING parquet \n OPTIONS ('path' '" +
                        getDatasetPath(UUID.fromString(targetDatasetId)) + "/BoslerTransformTemporary/" +
                        targetView + "/" + System.getenv("BRANCH") + "/" + UUID.randomUUID() + "')");
    }

    private static Matcher getSourceMatcher(String line) {
        Pattern sourcePattern = Pattern.compile("`(/Projects[^`]+)`");
        return sourcePattern.matcher(line);
    }

    private static String getSourceDatasetId(String sourceDatasetLogicalPath) throws Exception {
        try {
            String response = BosonApiCalls.getIdByPath(sourceDatasetLogicalPath).get("response");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode sourceDatasetResponse = objectMapper.readTree(response);

            if (Objects.equals(sourceDatasetResponse.get("Status").asText(), "true") &&
                    Objects.equals(sourceDatasetResponse.get("Type").asText(), "DATASET")) {
                return sourceDatasetResponse.get("Message").asText();
            } else {

                throw new Exception("Source dataset not found. Can not proceed further " + sourceDatasetLogicalPath);
            }
        } catch (Exception e) {

            BosonApiCalls.BosonLog(BuildStatus.ERROR, BuildStage.RUNNING, "Source dataset not found. Can not proceed further " + sourceDatasetLogicalPath, e.getMessage());
            throw new Exception("Source dataset not found. Can not proceed further " + sourceDatasetLogicalPath);
        }


    }

//    public static void readUnicodeJava11(String fileName) {
//
//        try (FileReader fr = new FileReader(fileName, StandardCharsets.UTF_8);
//             BufferedReader reader = new BufferedReader(fr)) {
//
//            String str;
//            while ((str = reader.readLine()) != null) {
//                System.out.println(str);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void waitHere() {

        long startTime = System.currentTimeMillis();
        long duration = 3 * 60 * 1000; // 3 minutes in milliseconds
        long endTime = startTime + duration;

        while (System.currentTimeMillis() < endTime) {
            System.out.print("");
        }
    }

}
