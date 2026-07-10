package io.movetodata.dataset.library.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.build.library.services.PreviewService;
import io.movetodata.dataset.library.models.ColumnStatsModel;
import io.movetodata.dataset.library.models.SparkResults;
import io.movetodata.platform.library.repository.CacheRepository;
import io.kubernetes.client.openapi.ApiException;
import lombok.AllArgsConstructor;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.StructType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.sharedutils.Redis.setCache;
import static org.apache.spark.sql.functions.*;

@Service
@AllArgsConstructor
public class SparkService {
    private final CacheRepository cacheRepository;
    private final SimpMessagingTemplate template;
    private final SparkResultsService sparkResultsService;
    private final PreviewService previewService;


    public static String columnDataType(Dataset<Row> dataset, String colName) {
        StructType schema = dataset.schema();
        return schema.apply(colName).dataType().typeName();
    }

    public static SparkResults calculateStats(Dataset<Row> dfTotal, ColumnStatsModel columnStatsModel, UUID resultsId) throws Exception {

        Map<String, Object> counts = new HashMap<>();

        Dataset<Row> lengths = null;
        Dataset<Row> distribution = null;


        if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "string")) {

            Dataset<Row> dfCases = dfTotal.withColumn(columnStatsModel.getColumn(),
                    when(
                            col(columnStatsModel.getColumn()).rlike("^[A-Z ]*$"), "UPPER")
                            .when(
                                    col(columnStatsModel.getColumn()).rlike("^[a-z ]*$"), "LOWER")
                            .when(
                                    col(columnStatsModel.getColumn()).rlike("^[a-zA-Z ]*$"), "MIXED")
                            .otherwise("Alphanumeric")
            );
            dfCases = dfCases.select(col(columnStatsModel.getColumn()));

            counts.put("MixedCase", dfCases.filter(col(columnStatsModel.getColumn()).contains("MIXED")).count());
            counts.put("Lowercase", dfCases.filter(col(columnStatsModel.getColumn()).contains("LOWER")).count());
            counts.put("Uppercase", dfCases.filter(col(columnStatsModel.getColumn()).contains("UPPER")).count());
            counts.put("Alphanumeric", dfCases.filter(col(columnStatsModel.getColumn()).contains("Alphanumeric")).count());


            counts.put("Numeric", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).cast("int").isNotNull()).count());

            counts.put("Empty", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNaN()).count());

            // Create a column with the length of each value in the specified column
            Column lengthCol = functions.length(dfTotal.col(columnStatsModel.getColumn()));

            // Create a column with the trimmed version of each value in the specified column
            Column trimmedCol = functions.trim(dfTotal.col(columnStatsModel.getColumn()));

            // Create a column with a boolean value indicating whether each value in the specified column needs to be trimmed
            Column needsTrimCol = lengthCol.gt(functions.length(trimmedCol));

            // Count the number of values in the specified column that need to be trimmed
            long needsTrimCount = dfTotal.filter(needsTrimCol).count();


            counts.put("Needs Trim", needsTrimCount);

            distribution = dfTotal
                    .groupBy(columnStatsModel.getColumn())
                    .count()
                    .sort(desc("count"))
                    .limit(300);

            distribution = distribution
                    .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed(columnStatsModel.getColumn(), "name");


            lengths = dfTotal
                    .withColumn("length", length(dfTotal.col(columnStatsModel.getColumn())))
                    .groupBy("length")
                    .count()
                    .orderBy("length")
                    .limit(300);

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");


        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "integer") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "bigint") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "double")) {

            counts.put("Min", dfTotal.agg(min(dfTotal.col(columnStatsModel.getColumn()))).first().get(0));
            counts.put("Max", dfTotal.agg(max(dfTotal.col(columnStatsModel.getColumn()))).first().get(0));

            counts.put("Mean", Math.round((Double) dfTotal.select(mean(columnStatsModel.getColumn())).first().get(0)));
            counts.put("Standard Deviation", Math.round((Double) dfTotal.select(stddev(columnStatsModel.getColumn())).first().get(0)));

            counts.put("Empty", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNaN()).count());

            counts.put("Negatives", dfTotal.agg(sum(functions.when(dfTotal.col(columnStatsModel.getColumn()).lt(0), 1).otherwise(0))).first().getLong(0));
            counts.put("Positives", dfTotal.agg(sum(functions.when(dfTotal.col(columnStatsModel.getColumn()).gt(0), 1).otherwise(0))).first().getLong(0));


            distribution = dfTotal
                    .groupBy(columnStatsModel.getColumn())
                    .count()
                    .sort(desc("count"))
                    .limit(300);

            distribution = distribution
                    .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed(columnStatsModel.getColumn(), "name");


            lengths = dfTotal
                    .withColumn("length", length(dfTotal.col(columnStatsModel.getColumn())))
                    .groupBy("length")
                    .count()
                    .orderBy("length")
                    .limit(300);

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");

        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "date") || Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "timestamp")) {

            SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");

            counts.put("Min", sdf.format(dfTotal.agg(min(dfTotal.col(columnStatsModel.getColumn()))).first().get(0)));
            counts.put("Max", sdf.format(dfTotal.agg(max(dfTotal.col(columnStatsModel.getColumn()))).first().get(0)));

            lengths = dfTotal.withColumn("name", date_format(dfTotal.col(columnStatsModel.getColumn()), "EEEE")).groupBy("name").count();

            lengths = lengths
                    .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                    .withColumnRenamed("length", "name");

//                    counts.put( dfTotal.withColumn("year", year(dfTotal.col(columnStatsModel.getColumn()))).groupBy("year").count();
            distribution = dfTotal.withColumn("name", month(dfTotal.col(columnStatsModel.getColumn()))).groupBy("name").count();
//                    distribution = dfTotal.withColumn("name", days(dfTotal.col(columnStatsModel.getColumn()))).groupBy("name").count();

        } else if (Objects.equals(columnDataType(dfTotal, columnStatsModel.getColumn()), "boolean")) {

            // Count the number of true values in the boolean column
            long trueCount = dfTotal.filter(col(columnStatsModel.getColumn()).equalTo(true)).count();

            // Count the number of false values in the boolean column
            long falseCount = dfTotal.filter(col(columnStatsModel.getColumn()).equalTo(false)).count();

            counts.put("True", trueCount);
            counts.put("False", falseCount);

        }

        counts.put("Distinct", dfTotal.agg(functions.countDistinct(columnStatsModel.getColumn())).first().get(0));

        counts.put("Rows", dfTotal.count());
        counts.put("Columns", dfTotal.columns().length);
        counts.put("Nulls", dfTotal.filter(dfTotal.col(columnStatsModel.getColumn()).isNull()).count());

        HashMap<String, Object> result = new HashMap<>();

        List<Map<String, Object>> distributionRows = new ArrayList<>();

        if (distribution != null) {
            distributionRows = distribution
                    .collectAsList()
                    .stream()
                    .map(row -> {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (String fieldName : row.schema().fieldNames()) {
                            rowMap.put(fieldName, row.getAs(fieldName));
                        }
                        return rowMap;
                    })
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> lengthRows = new ArrayList<>();

        if (lengths != null) {
            lengthRows = lengths
                    .collectAsList()
                    .stream()
                    .map(row -> {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (String fieldName : row.schema().fieldNames()) {
                            rowMap.put(fieldName, row.getAs(fieldName));
                        }
                        return rowMap;
                    })
                    .collect(Collectors.toList());
        }


        result.put("columnDataType", columnDataType(dfTotal, columnStatsModel.getColumn()));
        result.put("distribution", distributionRows);
        result.put("lengths", lengthRows);
        result.put("counts", counts);

        SparkResults sparkResults = new SparkResults();

        sparkResults.setId(resultsId);
        sparkResults.setDatasetId(columnStatsModel.getDatasetId());
        sparkResults.setBranch(columnStatsModel.getBranch());
        sparkResults.setColumnName(columnStatsModel.getColumn());
        sparkResults.setResults(result);

        return sparkResults;
    }

    public static List<Object> getDfRows(Dataset<Row> df) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> stringRows = df.toJSON().collectAsList();
        // Convert JSON strings to List<Object>
        List<Object> objects = new ArrayList<>();
        for (String jsonString : stringRows) {
            try {
                Object obj = objectMapper.readValue(jsonString, new TypeReference<Object>() {
                });
                objects.add(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return objects;
    }

    public void calculateStatistics(Dataset<Row> dfTotal, ColumnStatsModel columnStatsModel, UUID resultsId) throws Exception {
        SparkResults sparkResults = calculateStats(dfTotal, columnStatsModel, resultsId);
        ObjectMapper objectMapper = new ObjectMapper();
        String respData = objectMapper.writeValueAsString(sparkResults.getResults());
        setCache("sparkResults" + sparkResults.getDatasetId() + sparkResults.getBranch() + sparkResults.getColumnName(), respData, cacheRepository);


        SparkResults sparkResultsSaved = sparkResultsService.save(sparkResults);

        // Sending to socket
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("success");

        template.convertAndSend("/topic/sparkResults/" + sparkResultsSaved.getId(), textMessage);
    }


    void runSparkColumnStatsJobWithKubernetes(HashMap<String, String> envVars, UUID userId) throws IOException, ApiException {
        previewService.buildSparkColumnStats(envVars, userId);
    }

    void runSparkSyncDatabaseJobWithKubernetes(HashMap<String, String> envVars, UUID userId, BuildLaunchedBy buildLaunchedBy) throws IOException, ApiException {
        previewService.buildSparkDatabaseSync(envVars, userId, buildLaunchedBy);
    }

}
