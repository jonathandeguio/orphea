package io.movetodata;

import io.movetodata.controller.ColumnStatsController;
import io.movetodata.controller.SynchroController;
import io.movetodata.utils.Utils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ColumnStats {

    public static void main(String[] args) throws Exception {

        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);


        List<String> variables = Arrays.asList("DATASET_ID", "BRANCH", "RESULTS_ID", "COLUMN", "ACCESS_TOKEN");

        if(!Utils.checkEnvVariables(variables)){
            throw new RuntimeException("Missing one or more required environment variables");
        }

        String datasetId = System.getenv("DATASET_ID");
        String branch = System.getenv("BRANCH");
        String column = System.getenv("COLUMN");
        String resultId = System.getenv("RESULTS_ID");


        ColumnStatsController columnStatsController = new ColumnStatsController();
        columnStatsController.columnStats(UUID.fromString(datasetId), branch, column, UUID.fromString(resultId));

    }
}

