package io.movetodata.controller;

import com.amazonaws.thirdparty.apache.codec.binary.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ApiException;
import io.movetodata.library.models.SchemaModel;
import io.movetodata.library.models.SparkResults;
import io.movetodata.utils.ApiCaller;
import io.movetodata.utils.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.movetodata.utils.SparkUtils.columnDataType;
import static org.apache.spark.sql.functions.*;

@Service
@RequiredArgsConstructor
public class ColumnStatsController {

    public void columnStats(UUID datasetId, String branch, String column, UUID resultId) throws Exception {

        try {

            Dataset<Row> dfTotal = SparkUtils.getSparkDF(datasetId, branch, -1);

            // Get the list of column names
            String[] colNames = dfTotal.columns();

            // Check if the list of column names contains the column that you are looking for
            boolean columnExists = Arrays.asList(colNames).contains(column);

            if (columnExists) { // The column exists in the DataFrame

                Map<String, Object> counts = new HashMap<>();

                Dataset<Row> lengths = null;
                Dataset<Row> distribution = null;


                if (Objects.equals(columnDataType(dfTotal, column), "string")) {

                    Dataset<Row> dfCases = dfTotal.withColumn(column,
                            when(
                                    col(column).rlike("^[A-Z ]*$"), "UPPER")
                                    .when(
                                            col(column).rlike("^[a-z ]*$"), "LOWER")
                                    .when(
                                            col(column).rlike("^[a-zA-Z ]*$"), "MIXED")
                                    .otherwise("Alphanumeric")
                    );
                    dfCases = dfCases.select(col(column));

                    counts.put("MixedCase", dfCases.filter(col(column).contains("MIXED")).count());
                    counts.put("Lowercase", dfCases.filter(col(column).contains("LOWER")).count());
                    counts.put("Uppercase", dfCases.filter(col(column).contains("UPPER")).count());
                    counts.put("Alphanumeric", dfCases.filter(col(column).contains("Alphanumeric")).count());


                    counts.put("Numeric", dfTotal.filter(dfTotal.col(column).cast("int").isNotNull()).count());

                    counts.put("Empty", dfTotal.filter(dfTotal.col(column).isNaN()).count());

                    // Create a column with the length of each value in the specified column
                    Column lengthCol = functions.length(dfTotal.col(column));

                    // Create a column with the trimmed version of each value in the specified column
                    Column trimmedCol = functions.trim(dfTotal.col(column));

                    // Create a column with a boolean value indicating whether each value in the specified column needs to be trimmed
                    Column needsTrimCol = lengthCol.gt(functions.length(trimmedCol));

                    // Count the number of values in the specified column that need to be trimmed
                    long needsTrimCount = dfTotal.filter(needsTrimCol).count();


                    counts.put("Needs Trim", needsTrimCount);

                    distribution = dfTotal
                            .groupBy(column)
                            .count()
                            .sort(desc("count"))
                            .limit(300);

                    distribution = distribution
                            .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                            .withColumnRenamed(column, "name");


                    lengths = dfTotal
                            .withColumn("length", length(dfTotal.col(column)))
                            .groupBy("length")
                            .count()
                            .orderBy("length")
                            .limit(300);

                    lengths = lengths
                            .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                            .withColumnRenamed("length", "name");


                } else if (Objects.equals(columnDataType(dfTotal, column), "integer") || Objects.equals(columnDataType(dfTotal, column), "bigint") || Objects.equals(columnDataType(dfTotal, column), "double")) {

                    counts.put("Min", dfTotal.agg(min(dfTotal.col(column))).first().get(0));
                    counts.put("Max", dfTotal.agg(max(dfTotal.col(column))).first().get(0));

                    counts.put("Mean", Math.round((Double) dfTotal.select(mean(column)).first().get(0)));
                    counts.put("Standard Deviation", Math.round((Double) dfTotal.select(stddev(column)).first().get(0)));

                    counts.put("Empty", dfTotal.filter(dfTotal.col(column).isNaN()).count());

                    counts.put("Negatives", dfTotal.agg(sum(functions.when(dfTotal.col(column).lt(0), 1).otherwise(0))).first().getLong(0));
                    counts.put("Positives", dfTotal.agg(sum(functions.when(dfTotal.col(column).gt(0), 1).otherwise(0))).first().getLong(0));


                    distribution = dfTotal
                            .groupBy(column)
                            .count()
                            .sort(desc("count"))
                            .limit(300);

                    distribution = distribution
                            .withColumn("percentage", distribution.col("count").divide(distribution.select(sum("count")).first().getLong(0)).multiply(100))
                            .withColumnRenamed(column, "name");


                    lengths = dfTotal
                            .withColumn("length", length(dfTotal.col(column)))
                            .groupBy("length")
                            .count()
                            .orderBy("length")
                            .limit(300);

                    lengths = lengths
                            .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                            .withColumnRenamed("length", "name");

                } else if (Objects.equals(columnDataType(dfTotal, column), "date") || Objects.equals(columnDataType(dfTotal, column), "timestamp")) {

                    SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");

                    counts.put("Min", sdf.format(dfTotal.agg(min(dfTotal.col(column))).first().get(0)));
                    counts.put("Max", sdf.format(dfTotal.agg(max(dfTotal.col(column))).first().get(0)));

                    lengths = dfTotal.withColumn("name", date_format(dfTotal.col(column), "EEEE")).groupBy("name").count();

                    lengths = lengths
                            .withColumn("percentage", lengths.col("count").divide(lengths.select(sum("count")).first().getLong(0)).multiply(100))
                            .withColumnRenamed("length", "name");

//                    counts.put( dfTotal.withColumn("year", year(dfTotal.col(columnName))).groupBy("year").count();
                    distribution = dfTotal.withColumn("name", month(dfTotal.col(column))).groupBy("name").count();
//                    distribution = dfTotal.withColumn("name", days(dfTotal.col(columnName))).groupBy("name").count();

                } else if (Objects.equals(columnDataType(dfTotal, column), "boolean")) {

                    // Count the number of true values in the boolean column
                    long trueCount = dfTotal.filter(col(column).equalTo(true)).count();

                    // Count the number of false values in the boolean column
                    long falseCount = dfTotal.filter(col(column).equalTo(false)).count();

                    counts.put("True", trueCount);
                    counts.put("False", falseCount);

                }

                counts.put("Distinct", dfTotal.agg(functions.countDistinct(column)).first().get(0));

                counts.put("Rows", dfTotal.count());
                counts.put("Columns", dfTotal.columns().length);
                counts.put("Nulls", dfTotal.filter(dfTotal.col(column).isNull()).count());

                Map<String, Object> result = new HashMap<>();

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


                result.put("columnDataType", columnDataType(dfTotal, column));
                result.put("distribution", distributionRows);
                result.put("lengths", lengthRows);
                result.put("counts", counts);


                Map<String, Object> result1 = new HashMap<>();

                result.put("result", result1);

                SparkResults sparkResults = new SparkResults();
                sparkResults.setId(resultId);
                sparkResults.setDatasetId(datasetId);
                sparkResults.setBranch(branch);
                sparkResults.setColumnName(column);
                sparkResults.setResults(result);

                // call API to send results
                Map<String, String> performed = ApiCaller.callApi("/api/dataset/sparkResults", "POST", System.getenv("ACCESS_TOKEN"), sparkResults, "json");


            } else {
                throw new NoSuchFieldException("Error: Column not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Something gone wrong " + e.getMessage());
        }

    }
}
