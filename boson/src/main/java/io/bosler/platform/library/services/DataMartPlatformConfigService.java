package io.bosler.platform.library.services;

import io.bosler.platform.library.models.DataMartConfigModel;
import io.bosler.platform.library.models.DataMartModel;
import io.bosler.platform.library.repository.DataMartConfigRepository;
import io.bosler.synchro.library.repository.DataMartRepository;
import io.bosler.synchro.library.repository.SyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataMartPlatformConfigService {
    private final DataMartConfigRepository dataMartConfigRepository;
    private final DataMartRepository dataMartRepository;
    private final SyncRepository syncRepository;

    public DataMartConfigModel getDataMartPlatformConfig() {
        return dataMartConfigRepository.findByConfig("platform");
    }

    public DataMartModel getDataMartModel(UUID id) {
        return dataMartRepository.findById(id).get();
    }

    public DataMartConfigModel putDataMartPlatformConfig(DataMartConfigModel dataMartConfigModelDTO, UUID userId) {
        DataMartConfigModel dataMartConfigModel = dataMartConfigRepository.findByConfig("platform");
        List<DataMartModel> existingDataMartModels = dataMartConfigModel.getDataMartModels();
        List<DataMartModel> newDataMartModels = dataMartConfigModelDTO.getDataMartModels();
        List<DataMartModel> dataMartModelList = new ArrayList<>();

        List<DataMartModel> deletedConfigs = existingDataMartModels.stream()
                .filter(existing -> newDataMartModels.stream()
                        .noneMatch(config -> config.getId() != null && config.getId().equals(existing.getId())))
                .collect(Collectors.toList());

        deletedConfigs.stream()
                .filter(Objects::nonNull)
                .forEach(deletedConfig -> syncRepository.deleteAllBySourceId(deletedConfig.getId()));

        for (DataMartModel dataMartModelDTO : newDataMartModels) {
            DataMartModel dataMartModel = new DataMartModel();

            if (dataMartModelDTO.getId() != null) {
                dataMartModel = dataMartRepository.findById(dataMartModelDTO.getId()).get();
            } else {
                dataMartModel.setCreatedAt(new Date());
                dataMartModel.setCreatedBy(userId);
            }

            dataMartModel.setName(dataMartModelDTO.getName());
            dataMartModel.setDescription(dataMartModelDTO.getDescription());

            dataMartModel.setUsername(dataMartModelDTO.getUsername());
            dataMartModel.setPassword(dataMartModelDTO.getPassword());
            dataMartModel.setDatabase(dataMartModelDTO.getDatabase());
            dataMartModel.setServer(dataMartModelDTO.getServer());
            dataMartModel.setPort(dataMartModelDTO.getPort());
            dataMartModel.setLimitRows(dataMartModelDTO.getLimitRows());
            dataMartModel.setLimitSize(dataMartModelDTO.getLimitSize());

            dataMartModel.setUpdatedAt(new Date());
            dataMartModel.setUpdatedBy(userId);
            dataMartModel.setEnabled(dataMartModelDTO.isEnabled());
            dataMartModel.setProjects(dataMartModelDTO.getProjects());

            dataMartModelList.add(dataMartModel);
        }

        dataMartConfigModel.setDataMartModels(dataMartModelList);
        return dataMartConfigRepository.save(dataMartConfigModel);
    }
}


