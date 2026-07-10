package io.movetodata.dataset.library.services;

import io.movetodata.build.BobEnums.BuildLaunchedBy;
import io.movetodata.build.library.services.PreviewService;
import io.movetodata.dataset.library.models.ColumnStatsModel;
import io.movetodata.platform.library.repository.CacheRepository;
import io.movetodata.platform.library.repository.PlatformConfigRepository;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Service
public class AsyncSparkService extends SparkService {
    @Autowired
    public AsyncSparkService(CacheRepository cacheRepository, SimpMessagingTemplate template, SparkResultsService sparkResultsService, PreviewService previewService) {
        super(cacheRepository, template, sparkResultsService, previewService);
    }

    @Async
    public void asyncRunSparkColumnStatsJobWithKubernetes(HashMap<String, String> envVars, UUID userId) throws Exception {
        super.runSparkColumnStatsJobWithKubernetes(envVars, userId);
    }

    @Async
    public void asyncRunSparkSyncDatabaseJobWithKubernetes(HashMap<String, String> envVars, UUID userId, BuildLaunchedBy buildLaunchedBy) throws Exception {
        super.runSparkSyncDatabaseJobWithKubernetes(envVars, userId, buildLaunchedBy);
    }

    @Async
    public void asyncCalculateStatistics(Dataset<Row> dfTotal, ColumnStatsModel columnStatsModel, UUID resultsId) throws Exception {
        super.calculateStatistics(dfTotal, columnStatsModel, resultsId);
    }
}
