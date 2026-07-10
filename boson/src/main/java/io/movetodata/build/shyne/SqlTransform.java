package io.movetodata.build.shyne;

import io.movetodata.build.BobEnums.BuildType;
import io.movetodata.build.shyne.Utils.Utils;
import io.movetodata.build.shyne.controller.k8s.SqlTransformK8S;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class SqlTransform {

    public static void main(String[] args) throws Exception {

        List<String> variables = Arrays.asList("BUILD_ID",
                "REPOSITORY_ID",
                "BRANCH",
                "SCRIPT_PATH",
                "PHYSICAL_ENDPOINT",
                "TRANSACTION_ID",
//                "BRANCH_ID",
//                "COMMIT_ID",
                "JULIA_URL",
                "BUILD_TOKEN",
                "BUILD_TYPE");

        if (!Utils.checkEnvVariables(variables)) {
            throw new RuntimeException("Missing one or more required environment variables");
        }


        String tempPath = System.getenv("BOSLER_MOUNT_PATH") + "/SQLTransformTemp/" + UUID.randomUUID();

        Map<String, String> envVars = new HashMap<>();

        envVars.put("BUILD_ID", System.getenv("BUILD_ID"));
        envVars.put("REPOSITORY_ID", System.getenv("REPOSITORY_ID"));
        envVars.put("BRANCH", System.getenv("BRANCH"));
        envVars.put("SCRIPT_PATH", System.getenv("SCRIPT_PATH"));
        envVars.put("PHYSICAL_ENDPOINT", System.getenv("PHYSICAL_ENDPOINT"));
        envVars.put("TRANSACTION_ID", System.getenv("TRANSACTION_ID"));
        envVars.put("JULIA_URL", System.getenv("JULIA_URL"));
        envVars.put("BUILD_TOKEN", System.getenv("BUILD_TOKEN"));
        envVars.put("TEMP_PATH", tempPath);
        envVars.put("ROW_LIMIT", System.getenv("ROW_LIMIT"));
        envVars.put("SOURCE", System.getenv("SOURCE"));

        SqlTransformK8S sqlTransformK8S = new SqlTransformK8S();
        sqlTransformK8S.performTransform(envVars, BuildType.valueOf(System.getenv("BUILD_TYPE")), null);
    }
}

