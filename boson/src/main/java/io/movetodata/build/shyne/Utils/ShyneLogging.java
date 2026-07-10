package io.movetodata.build.shyne.Utils;

import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.BobEnums.BuildType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShyneLogging {

    public void log(String level, BuildStage buildStage, String message, String debug, BuildType buildType) throws Exception {
        log(level, buildStage, message, debug, buildType, null, null);
    }

    public void log(String level, BuildStage buildStage, String message, String debug, BuildType buildType, UUID checkpointDataset, BuildStatus checkpointDatasetStatus) throws Exception {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
        String formattedDate = formatter.format(now);
        String logEntry = formattedDate + " " + level.toUpperCase() + " Running: " + message + " " + debug;

        System.out.println(logEntry);


        HashMap<String, String> apiResponse = ApiCaller.callApi("/api/build/" + System.getenv("BUILD_ID") + "/log",
                "POST",
                System.getenv("BUILD_TOKEN"),
                createPayload(level, buildStage, message, debug, checkpointDataset, checkpointDatasetStatus),
                "json");

        if (!apiResponse.equals("200")) {
            String errorMessage = formattedDate + " ERROR Running: " + message + " " + debug;
        }

        if (level.equals(BuildStatus.ERROR)) {
            String errorExceptionMessage = formattedDate + " [status]: " + message + " " + debug;
            Exception errorException = new Exception(errorExceptionMessage);

            throw errorException;
        }
    }

    public void finish(String message, String debug, BuildType buildType) throws Exception {
        this.log("INFO", BuildStage.FINISHED, message, debug, buildType);
    }

    public void info(String message, String debug, BuildType buildType) throws Exception {
        this.log("INFO", BuildStage.RUNNING, message, debug, buildType);
    }

    public void error(String message, String debug, BuildType buildType) throws Exception {
        this.log("ERROR", BuildStage.FINISHED, message, debug, buildType);
    }

    public void checkpoint(UUID datasetId, BuildStatus buildStatus, BuildType buildType) throws Exception {
        this.log("INFO", BuildStage.RUNNING, "", "", buildType, datasetId, buildStatus);
    }

    private Map<String, Object> createPayload(String level, BuildStage buildStage, String message, String debug, UUID checkpointDataset, BuildStatus checkpointDatasetStatus) throws Exception {

        Map<String, Object> payload = new HashMap<>();

        payload.put("status", level);
        payload.put("stage", buildStage);
        payload.put("message", message);
        payload.put("debug", debug);
        payload.put("checkpointDataset", checkpointDataset);
        payload.put("checkpointStatus", checkpointDatasetStatus);

        return payload;
    }
}

