package io.bosler;

import io.bosler.controller.SynchroController;
import io.bosler.utils.Utils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Synchro {

    public static void main(String[] args) throws Exception {

        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);

        List<String> variables = Arrays.asList("DATASET_ID", "BRANCH", "TABLE", "DB_HOST", "DATABASE", "DB_USERNAME", "DB_PASSWORD", "ACCESS_TOKEN");

        if(!Utils.checkEnvVariables(variables)){
            throw new RuntimeException("Missing one or more required environment variables");
        }

        String datasetId = System.getenv("DATASET_ID");
        String branch = System.getenv("BRANCH");

        SynchroController synchroController = new SynchroController();
        synchroController.performSync(UUID.fromString(datasetId), branch);

    }
}

