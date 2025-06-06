package io.bosler.build.library.services;

import io.bosler.build.BobEnums.BuildLanguage;
import io.bosler.build.BobEnums.BuildLaunchedBy;
import io.bosler.build.BobEnums.BuildTrigger;
import io.bosler.build.BobEnums.BuildType;
import io.bosler.build.library.models.BuildLog;
import io.bosler.build.shyne.controller.local.SqlPreviewLocal;
import io.bosler.platform.library.models.SparkConfigModel;
import io.bosler.platform.library.repository.SparkConfigRepository;
import io.bosler.sharedutils.Exceptions.EnvConfigurationException;
import io.kubernetes.client.openapi.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreviewService {
    private final K8Service k8Service;
    private final SqlPreviewLocal sqlPreviewLocal;
    private final BuildLogService buildLogService;
    private final SparkConfigRepository sparkConfigRepository;

    public ResponseEntity<Object> buildTransform(UUID userId,
                                                 UUID repositoryId,
                                                 String branch,
                                                 String scriptPath,
                                                 String branchId,
                                                 String commitId,
                                                 int cores,
                                                 String memory,
                                                 int numberOfExecutors,
                                                 int failureRetries,
                                                 BuildTrigger trigger,
                                                 BuildLaunchedBy launchedBy,
                                                 BuildType buildType,
                                                 BuildLanguage buildLanguage,
                                                 String sql,
                                                 String rowLimit,
                                                 String fileName,
                                                 String lineNo
    ) throws Exception {

        if (buildLanguage == BuildLanguage.PYTHON) {
            sql = "not required";
        }

        UUID buildId = UUID.randomUUID();
        log.info("Created Build Id : " + buildId);

        HashMap<String, String> envVars = k8Service.settingEnvironmentVariables(repositoryId, branch, scriptPath, branchId, commitId, cores, memory, buildId, userId, buildType, buildLanguage, trigger, rowLimit, fileName, lineNo);

        BuildTrigger specificTrigger = (buildLanguage == BuildLanguage.SQL) ? BuildTrigger.SQL : BuildTrigger.PYTHON;
        BuildLog buildLog = buildLogService.initialBuildLogSetupWithSockets(buildId, repositoryId, userId, specificTrigger, launchedBy, scriptPath, branch);

        handleBuildType(envVars, trigger, cores, memory, numberOfExecutors, failureRetries, buildLanguage, userId, buildType, sql);

        return new ResponseEntity<>(buildLog, HttpStatus.OK);

    }

    @Async
    public void handleBuildType(HashMap<String, String> envVars, BuildTrigger trigger, int cores, String memory, int numberOfExecutors, int failureRetries, BuildLanguage buildLanguage, UUID userId, BuildType buildType, String sql) throws Exception {
        SparkConfigModel sparkConfigModel = sparkConfigRepository.findByConfig("platform");

        if (buildLanguage == BuildLanguage.PYTHON) {
            if (buildType == BuildType.DEFAULT) {
                if (Objects.equals(sparkConfigModel.getPythonBuild(), "local")) {
                    throw new EnvConfigurationException("Python build is not available locally yet.");
                } else if (Objects.equals(sparkConfigModel.getPythonBuild(), "kubernetes")) {
                    k8Service.podRelatedTasksInTransform(failureRetries, numberOfExecutors, cores, memory, UUID.fromString(envVars.get("BUILD_ID")), buildType, buildLanguage, trigger, envVars);
                }
            } else if (buildType == BuildType.PREVIEW) {
                if (Objects.equals(sparkConfigModel.getPythonPreview(), "local")) {
                    throw new EnvConfigurationException("Python preview is not available locally yet.");
                } else if (Objects.equals(sparkConfigModel.getPythonPreview(), "kubernetes")) {
                    k8Service.podRelatedTasksInTransform(failureRetries, numberOfExecutors, cores, memory, UUID.fromString(envVars.get("BUILD_ID")), buildType, buildLanguage, trigger, envVars);
                }
            }
        } else if (buildLanguage == BuildLanguage.SQL) {
            if (buildType == BuildType.DEFAULT) {
                if (Objects.equals(sparkConfigModel.getSqlBuild(), "local")) {
                    throw new EnvConfigurationException("Sql build is not available locally yet.");
                } else if (Objects.equals(sparkConfigModel.getSqlBuild(), "kubernetes")) {
                    k8Service.podRelatedTasksInTransform(failureRetries, numberOfExecutors, cores, memory, UUID.fromString(envVars.get("BUILD_ID")), buildType, buildLanguage, trigger, envVars);
                }
            } else if (buildType == BuildType.PREVIEW) {
                if (Objects.equals(sparkConfigModel.getSqlPreview(), "local")) {
                    String tempPath = System.getenv("BOSLER_MOUNT_PATH") + "/SQLTransformTemp/" + UUID.randomUUID();
                    envVars.put("TEMP_PATH", tempPath);
                    envVars.put("SQL", sql);
                    try {
                        sqlPreviewLocal.SQLTransform(envVars, BuildType.PREVIEW, userId);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }

                } else if (Objects.equals(sparkConfigModel.getSqlPreview(), "kubernetes")) {
                    k8Service.podRelatedTasksInTransform(failureRetries, numberOfExecutors, cores, memory, UUID.fromString(envVars.get("BUILD_ID")), buildType, buildLanguage, trigger, envVars);
                }
            }
        } else {
            throw new EnvConfigurationException("Build language not configured while handling build");
        }


    }

    public void buildSparkColumnStats(HashMap<String, String> envVars, UUID userId) throws IOException, ApiException {
        UUID buildId = UUID.fromString(envVars.get("BUILD_ID"));
        String branch = envVars.get("BRANCH");
        buildLogService.initialBuildLogSetupWithSockets(buildId, null, userId, BuildTrigger.COLUMNSTATS, BuildLaunchedBy.MANUAL, null, branch);
        k8Service.podRelatedTasksInTransform(0, 1, 1, "1024M", UUID.fromString(envVars.get("BUILD_ID")), BuildType.DEFAULT, BuildLanguage.SQL, BuildTrigger.COLUMNSTATS, envVars);
    }

    public void buildSparkDatabaseSync(HashMap<String, String> envVars, UUID userId, BuildLaunchedBy buildLaunchedBy) throws IOException, ApiException {
        k8Service.podRelatedTasksInTransform(0, 1, 1, "1024M", UUID.fromString(envVars.get("BUILD_ID")), BuildType.DEFAULT, BuildLanguage.SQL, BuildTrigger.SYNCHRO, envVars);
    }
}
