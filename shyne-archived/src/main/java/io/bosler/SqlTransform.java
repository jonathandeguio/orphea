package io.bosler;

import io.bosler.controller.SqlTransformController;
import io.bosler.utils.Utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SqlTransform {

    public static void main(String[] args) throws Exception {

        List<String> variables = Arrays.asList("BUILD_ID",
                "REPOSITORY_ID",
                "BRANCH",
                "SCRIPT_PATH",
//                "BRANCH_ID",
//                "COMMIT_ID",
                "JULIA_HOST",
                "JULIA_PORT",
                "ACCESS_TOKEN");

        if (!Utils.checkEnvVariables(variables)) {
            throw new RuntimeException("Missing one or more required environment variables");
        }

        String repositoryId = System.getenv("REPOSITORY_ID");
        String branch = System.getenv("BRANCH");
        String scriptPath = System.getenv("SCRIPT_PATH");

        SqlTransformController sqlTransformController = new SqlTransformController();
        sqlTransformController.transform(UUID.fromString(repositoryId), branch, scriptPath);

    }
}

