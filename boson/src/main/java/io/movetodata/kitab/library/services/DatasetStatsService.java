package io.movetodata.kitab.library.services;

import io.movetodata.kitab.library.models.DatasetStatsModel;
import io.movetodata.kitab.library.repository.DatasetStatsRepository;
import io.movetodata.platform.library.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.movetodata.sharedutils.Redis.clearRelatedCaches;

@Service
@RequiredArgsConstructor
public class DatasetStatsService {
    private final DatasetStatsRepository datasetStatsRepository;
    private final CacheRepository cacheRepository;

    public void statsAndCacheCleanup(UUID datasetId, String branch) {
        if (datasetStatsRepository.existsDatasetStatsModelByDatasetIdAndBranch(datasetId, branch)) {
            DatasetStatsModel datasetStatsModel = datasetStatsRepository.findByDatasetIdAndBranch(datasetId, branch);
            datasetStatsRepository.delete(datasetStatsModel);
        }

        // Remove Redis cache
        clearRelatedCaches(datasetId, branch, cacheRepository);
    }
}
