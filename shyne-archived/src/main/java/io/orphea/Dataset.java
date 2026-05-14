package io.orphea;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.orphea.controller.ColumnStatsController;
import io.orphea.controller.DatasetController;
import io.orphea.library.models.FilterModel;
import io.orphea.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class Dataset {

    public static void main(String[] args) throws Exception {

        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);


        List<String> variables = Arrays.asList("DATASET_ID", "BRANCH", "RESULTS_ID", "FILTER_BASE64", "ACCESS_TOKEN");

        if(!Utils.checkEnvVariables(variables)){
            throw new RuntimeException("Missing one or more required environment variables");
        }

        String datasetId = System.getenv("DATASET_ID");
        String branch = System.getenv("BRANCH");
        String resultId = System.getenv("RESULTS_ID");
        String filterModelBase64 = System.getenv("FILTER_BASE64");

        byte[] decodedBytes = Base64.getDecoder().decode(filterModelBase64);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = new String(decodedBytes);
        FilterModel filterModel = objectMapper.readValue(json, FilterModel.class);

        if (datasetId == null || branch == null || resultId == null || filterModelBase64 == null ) {
            throw new RuntimeException("Missing one or more required environment variables");
        }

        DatasetController datasetController = new DatasetController();
        datasetController.getDataset(UUID.fromString(datasetId), branch,filterModel, UUID.fromString(resultId));

    }
}

