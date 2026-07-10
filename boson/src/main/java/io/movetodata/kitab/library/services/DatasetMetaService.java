package io.movetodata.kitab.library.services;

import io.movetodata.build.BobEnums.BuildTrigger;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.DatasetMetaDataModel;
import io.movetodata.kitab.library.repository.DatasetMetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DatasetMetaService implements IMetaService {
    private final DatasetMetaRepository datasetMetaRepository;

    @Autowired
    public DatasetMetaService(DatasetMetaRepository datasetMetaRepository) {
        this.datasetMetaRepository = datasetMetaRepository;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DATASET;
    }

    @Override
    public Object getMetaData(UUID id) {
        Optional<DatasetMetaDataModel> meta = datasetMetaRepository.findById(id);
        return meta.orElse(null);
    }

    public void putMetaData(UUID datasetId, UUID buildId, BuildTrigger buildTrigger, UUID transactionId) {
        Optional<DatasetMetaDataModel> meta = datasetMetaRepository.findById(datasetId);
        DatasetMetaDataModel metaData = null;
        metaData = meta.orElseGet(DatasetMetaDataModel::new);

        metaData.setId(datasetId);
        metaData.setBuildId(buildId);
        metaData.setBuildTrigger(buildTrigger);
        metaData.setTransactionId(transactionId);

        datasetMetaRepository.save(metaData);
    }

    public void setDefaultBranchPresent(UUID datasetId) throws Exception {
        Optional<DatasetMetaDataModel> meta = datasetMetaRepository.findById(datasetId);
        if (meta.isEmpty()) {
            throw new Exception("Meta Data not present");
        }
        if (!meta.get().isDefaultBranchPresent()) {
            meta.get().setDefaultBranchPresent(true);
            datasetMetaRepository.save(meta.get());
        }
    }

    public void setShowBranchSelector(UUID datasetId) throws Exception {
        Optional<DatasetMetaDataModel> meta = datasetMetaRepository.findById(datasetId);
        if (meta.isEmpty()) {
            throw new Exception("Meta Data not present");
        }
        if (!meta.get().isShowBranchSelector()) {
            meta.get().setShowBranchSelector(true);
            datasetMetaRepository.save(meta.get());
        }
    }
}

