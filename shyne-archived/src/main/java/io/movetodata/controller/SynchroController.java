package io.movetodata.controller;

import com.amazonaws.thirdparty.apache.codec.binary.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ApiException;
import io.movetodata.library.models.SchemaModel;
import io.movetodata.utils.ApiCaller;
import io.movetodata.utils.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.apache.spark.sql.functions.*;

@Service
@RequiredArgsConstructor
public class SynchroController {

    public void performSync(UUID datasetId, String branch) throws Exception {

        Dataset<Row> dataset = SparkUtils.getSparkDF(datasetId, branch, -1);

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", System.getenv("DB_USERNAME"));
        connectionProperties.put("password", System.getenv("DB_PASSWORD"));

        // Saving data to a JDBC source
        dataset.write().mode("overwrite")
                .jdbc("jdbc:postgresql://" + System.getenv("DB_HOST") + "/" + System.getenv("DATABASE"), "public." + System.getenv("TABLE"), connectionProperties);

        // make an api call here to tell boson that it's done.
        Map<String, String> performed = ApiCaller.callApi("/api/synchro/PostgresSync/" + datasetId + "/" + branch + "/performed", "GET", System.getenv("ACCESS_TOKEN"), null, null);

    }
}
