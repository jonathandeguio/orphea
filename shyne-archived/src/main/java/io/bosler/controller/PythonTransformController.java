package io.bosler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bosler.utils.BosonApiCalls;
import io.bosler.utils.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.eclipse.jgit.api.Git;
import org.jvnet.hk2.annotations.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.bosler.utils.BackingFS.deleteDatasetFiles;
import static io.bosler.utils.BackingFS.getDatasetPathWithBranch;

@Service
@RequiredArgsConstructor
public class PythonTransformController {

    // TODO : This is not working yet!

    public void transform(UUID repositoryId, String branch, String scriptPath) throws Exception {


        BufferedReader reader;
        try {
            // Clone the repo
            File file = new File(System.getenv("BOSLER_MOUNT_PATH") + "/" + repositoryId);

            if (!file.exists()) {
                Git.cloneRepository()
                        .setURI("http://" + System.getenv("JULIA_HOST") + ":" + System.getenv("JULIA_PORT") + "/julia/" + repositoryId + "/.git")
                        .setDirectory(file)
                        .call();
            }

            // TODO : checkout branch based on commit_id

            reader = new BufferedReader(new FileReader(System.getenv("BOSLER_MOUNT_PATH") + "/" + repositoryId + "/" + scriptPath));


        } catch (Exception e) {
            System.out.println("Running it local");

            reader = new BufferedReader(new FileReader(System.getProperty("user.home") + "/bosler/" + repositoryId + "/" + "test.sql"));
        }

        StringBuilder content = new StringBuilder();

        try {

            Map<String, Object> resolveTargetDataset = resolveDatasets(reader, content);

            content = (StringBuilder) resolveTargetDataset.get("content");
            Map<String, String> sources = (Map<String, String>) resolveTargetDataset.get("sources");
            Map<String, String> targets = (Map<String, String>) resolveTargetDataset.get("targets");

            reader.close();

            String targetDatasetLogicalPath = null;
            String targetDatasetId = null;
            for (Map.Entry<String, String> entry : targets.entrySet()) {
                targetDatasetLogicalPath = entry.getKey();
                targetDatasetId = entry.getValue();
            }


            // check if build spec exists with some other dataset, if yes then abort
            BosonApiCalls.existsBuildSpecificationWithAnother(String.valueOf(repositoryId), targetDatasetId, branch, "sql", scriptPath, "");

            // resolve sources to Ids and put in a map
            // resolve_bezier_links(path, target, "master", branch)
            ArrayList<String> sourcesList = new ArrayList();
            for (Map.Entry<String, String> entry : sources.entrySet()) {
                String sourceDatasetLogicalPath = entry.getKey();
                String sourceDatasetId = entry.getValue();

//                BosonApiCalls.resolveBezierLinks(sourceDatasetId, targetDatasetId, "master", branch);

            }

            BosonApiCalls.createBuildSpecification(targetDatasetId, branch, "");

            // start transaction, run the script, end transaction
            transformSQL(content.toString(), sources, "master", targetDatasetId, branch);

            BosonApiCalls.postTransform(targetDatasetId, branch);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transformSQL(String sql, Map<String, String> sources, String sourceBranch, String targetDatasetId, String targetBranch) throws Exception {

        // Initialize Spark session
        SparkSession sparkSession = SparkUtils.sparkSession();

        // Process source datasets
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            String sourceDatasetLogicalPath = entry.getKey();
            String sourceDatasetId = entry.getValue();

            // Get source table
            Dataset<Row> sourceTable = null;
            try {
                sourceTable = SparkUtils.getSparkDF(UUID.fromString(sourceDatasetId), sourceBranch, -1);
            } catch (Exception e) {
                
                e.printStackTrace();
                throw new Exception("Error while getting source dataset " + sourceDatasetId + " " + sourceDatasetLogicalPath);
            }

            // Create a view for the source table
            String sourceView = sourceDatasetId.substring(sourceDatasetId.lastIndexOf("-") + 1);
            sourceTable.createOrReplaceTempView(sourceView);
        }

        // Execute SQL query
        System.out.println(sql);
        try {
            sparkSession.sql(sql);
        } catch (Exception e) {
            
            e.printStackTrace();
            throw new Exception("Error while running SQL " + e);
        }

        // Write result to HDFS
        String targetView = targetDatasetId.substring(targetDatasetId.lastIndexOf("-") + 1);
        Dataset<Row> outputTable = sparkSession.table(targetView);

        // Start transaction
        BosonApiCalls.startTransaction(UUID.fromString(targetDatasetId), targetBranch);

        // Create transaction here :

        System.out.println("Write Dataframe to " + getDatasetPathWithBranch(UUID.fromString(targetDatasetId), targetBranch));

        // Save output table to HDFS
        try {
            outputTable.write().mode(SaveMode.Overwrite).format("parquet").save(getDatasetPathWithBranch(UUID.fromString(targetDatasetId), targetBranch));
        } catch (Exception e) {
            
            e.printStackTrace();
            throw new Exception("Unable to write the dataset " + e);
        }

        // Delete temporary files
        deleteDatasetFiles(UUID.fromString(targetDatasetId), "temp");

        // End transaction
        BosonApiCalls.endTransaction(UUID.fromString(targetDatasetId), targetBranch);


        // Stop Spark session
        sparkSession.stop();
    }


    public Map<String, Object> resolveDatasets(BufferedReader reader, StringBuilder content) throws Exception {

        Pattern commentPattern = Pattern.compile("(?m)^--.*");

        Pattern targetPattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+`(.*)`");
        Pattern sourcePattern = Pattern.compile("(?i)(FROM|JOIN)\\s+`(.*)`");

        Map<String, String> sources = new HashMap<>();
        Map<String, String> targets = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            line = commentPattern.matcher(line).replaceAll("");

            Matcher targetMatcher = targetPattern.matcher(line);
            if (targetMatcher.find()) {
                String targetDatasetLogicalPath = targetMatcher.group(1);
                String targetDatasetId = null;
                try {
                    Object response = BosonApiCalls.getIdByPath(targetDatasetLogicalPath).get("response");

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode targetDatasetResponse = objectMapper.readTree((byte[]) response);

                    if(Objects.equals(targetDatasetResponse.get("Status").asText(), "true") && Objects.equals(targetDatasetResponse.get("Type").asText(), "dataset")) {
                        targetDatasetId = targetDatasetResponse.get("Message").asText();
                    } else {
                        System.out.println("Target Dataset not found, going to create");
                        BosonApiCalls.resolveTargetDataset(null, targetDatasetLogicalPath);
                        Object response1 = BosonApiCalls.getIdByPath(targetDatasetLogicalPath).get("response");
                        JsonNode targetDatasetResponse1 = objectMapper.readTree((byte[]) response1);

                        targetDatasetId = targetDatasetResponse1.get("Message").asText();
                    }
                } catch (Exception e) {
                    System.out.println("Not able to resolve the target dataset. ");
                }
                String targetView = targetDatasetId.substring(targetDatasetId.lastIndexOf("-") + 1);

                line = line.replace("`" + targetDatasetLogicalPath + "`", "`" + targetView + "`" + "\n USING parquet \n OPTIONS ('path' '/bosler/dataset/SQLTransformation/tmp/" + targetDatasetId + "/" + UUID.randomUUID() + "')");
                targets.put(targetDatasetLogicalPath, targetDatasetId);
            }

            Matcher sourceMatcher = sourcePattern.matcher(line);

            if (sourceMatcher.find()) {
                String sourceDatasetLogicalPath = sourceMatcher.group(1);

                String sourceDatasetId;
                try {
                    Object response = BosonApiCalls.getIdByPath(sourceDatasetLogicalPath).get("response");

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode sourceDatasetResponse = objectMapper.readTree((byte[]) response);

                    if(Objects.equals(sourceDatasetResponse.get("Status").asText(), "true") && Objects.equals(sourceDatasetResponse.get("Type").asText(), "dataset")) {
                        sourceDatasetId = sourceDatasetResponse.get("Message").asText();
                    } else {
                        
                        throw new Exception("Source dataset not found. Can not proceed further " + sourceDatasetLogicalPath);
                    }
                } catch (Exception e) {
                    
                    throw new Exception("Source dataset not found. Can not proceed further " + sourceDatasetLogicalPath);
                }

                String sourceView = sourceDatasetId.substring(sourceDatasetId.lastIndexOf("-") + 1);

                line = line.replace(sourceDatasetLogicalPath, sourceView);
                sources.put(sourceDatasetLogicalPath, sourceDatasetId);
            }

            content.append(line).append("\n");
        }

        return Map.of("content", content, "targets", targets, "sources", sources);
    }


}
