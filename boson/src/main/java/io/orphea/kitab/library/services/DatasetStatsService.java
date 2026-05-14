package io.orphea.kitab.library.services;

import io.orphea.kitab.library.models.DatasetStatsModel;
import io.orphea.kitab.library.repository.DatasetStatsRepository;
import io.orphea.platform.library.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static io.orphea.sharedutils.Redis.clearRelatedCaches;

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
