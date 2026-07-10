package io.movetodata;

import io.movetodata.controller.PythonTransformController;
import io.movetodata.controller.SqlTransformController;
import io.movetodata.utils.Utils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PythonTransform {

    // TODO : not working yet!

    public static void main(String[] args) throws Exception {

        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");

        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);

        List<String> variables = Arrays.asList("BUILD_ID",
                "REPOSITORY_ID",
                "BRANCH",
                "SCRIPT_PATH",
//                "BRANCH_ID",
                "COMMIT_ID",
                "JULIA_HOST",
                "JULIA_PORT",
                "ACCESS_TOKEN");

        if (!Utils.checkEnvVariables(variables)) {
            throw new RuntimeException("Missing one or more required environment variables");
        }

        String repositoryId = System.getenv("REPOSITORY_ID");
        String branch = System.getenv("BRANCH");
        String scriptPath = System.getenv("SCRIPT_PATH");

        PythonTransformController pythonTransformController = new PythonTransformController();
        pythonTransformController.transform(UUID.fromString(repositoryId), branch, scriptPath);

    }
}

