package io.bosler.synchro.library.services;

import io.bosler.connect.library.enums.SourceAuthTypeEnum;
import io.bosler.connect.library.enums.SourceTypeEnum;
import io.bosler.connect.library.models.DatabaseSourceConfig;
import io.bosler.connect.library.services.SourceService;
import io.bosler.kitab.library.enums.ResourceSubtype;
import io.bosler.kitab.library.enums.ResourceType;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.services.ResourceService;
import io.bosler.platform.library.models.DataMartModel;
import io.bosler.platform.library.services.PlatformConfigService;
import io.bosler.sharedutils.Utils;
import io.bosler.synchro.library.models.SyncSpecification;
import io.bosler.synchro.library.repository.DataMartRepository;
import io.bosler.synchro.library.repository.SyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMartService {
    private final DataMartRepository dataMartRepository;
    private final SyncRepository syncRepository;
    private final ResourceService resourceService;
    private final PlatformConfigService platformConfigService;
    private final SourceService sourceService;

    public DatabaseSourceConfig getDatabaseSourceConfig(UUID dataMartId) {
        DataMartModel dataMartModel = dataMartRepository.findById(dataMartId).orElse(null);
        if (dataMartModel == null) return null;
        DatabaseSourceConfig databaseSourceConfig = new DatabaseSourceConfig();
        databaseSourceConfig.setDatabase(dataMartModel.getDatabase());
        databaseSourceConfig.setUsername(dataMartModel.getUsername());
        databaseSourceConfig.setPassword(Utils.encodeBase64(dataMartModel.getPassword().getBytes()));
        databaseSourceConfig.setServer(dataMartModel.getServer());
        databaseSourceConfig.setPort(dataMartModel.getPort());
        databaseSourceConfig.setDbmsType(SourceTypeEnum.POSTGRES);
        databaseSourceConfig.setAuthType(SourceAuthTypeEnum.DEFAULT);

        return databaseSourceConfig;
    }

    public ResourceModel getDataMartSourceContentMetaData(UUID dataMartId) throws Exception {
        DataMartModel dataMartModel = dataMartRepository.findById(dataMartId).orElse(null);
        if (dataMartModel == null) return null;
        DatabaseSourceConfig databaseSourceConfig = getDatabaseSourceConfig(dataMartId);
        ResourceModel resourceModel = new ResourceModel();
        resourceModel.setId(dataMartId);
        resourceModel.setName(dataMartModel.getName());
        resourceModel.setType(ResourceType.POSTGRESSOURCE);
        resourceModel.setSubType(ResourceSubtype.NONE);

        return sourceService.getSqlTreeData(databaseSourceConfig, resourceModel, ResourceType.POSTGRESSOURCE);
    }

    public List<SyncSpecification> getUnInitializedDatasetDataMarts(UUID datasetId, String branch) {
        List<SyncSpecification> dataMartSyncSpecs = new ArrayList<>();
        if (!platformConfigService.getPlatformConfig().getDataMartEnabled()) {
            return dataMartSyncSpecs;
        }
        ResourceModel resourceModel = resourceService.findById(datasetId).orElseThrow();
        List<DataMartModel> dataMartModels = dataMartRepository.findAllByProject(resourceModel.getProject());

        for (DataMartModel dataMartModel : dataMartModels) {
            SyncSpecification syncSpecification = syncRepository.findByDatasetIdAndBranchAndDataMartId(datasetId, branch, dataMartModel.getId());
            if (syncSpecification == null && dataMartModel.isEnabled()) {
                syncSpecification = new SyncSpecification();
                syncSpecification.setTableName(resourceModel.getName());
                syncSpecification.setDatasetId(datasetId);
                syncSpecification.setBranch(branch);
                syncSpecification.setAutoSyncOnBuild(true);
                syncSpecification.setSourceId(dataMartModel.getId());
                syncSpecification.setSyncIndexes(new ArrayList<>());
                syncSpecification.setDataMartSyncSpec(true);
                syncSpecification.setDataMartId(dataMartModel.getId());

                dataMartSyncSpecs.add(syncSpecification);
            }
        }

        return dataMartSyncSpecs;
    }

}


