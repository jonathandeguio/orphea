package io.movetodata.dataset.library.services.DataHealth;

import io.movetodata.build.library.models.BuildLog;
import io.movetodata.dataset.library.DTOs.DataHealthDTO;
import io.movetodata.dataset.library.DataHealthFactory.DataHealthFactory;
import io.movetodata.dataset.library.DataHealthFactory.IDataHealth;
import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import io.movetodata.dataset.library.models.DataHealth.DataHealthModel;
import io.movetodata.dataset.library.repository.DataHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataHealthService {
    private final DataHealthFactory dataHealthFactory;
    private final DataHealthRepository dataHealthRepository;

    public void put(UUID datasetId, String branch, DataHealthDTO dataHealthDTO, UUID userId) {
        DataHealthModel dataHealthModel = new DataHealthModel();

        if (dataHealthDTO.getId() != null && dataHealthRepository.findById(dataHealthDTO.getId()).isPresent()) {
            dataHealthModel = dataHealthRepository.findById(dataHealthDTO.getId()).get();
        } else {
            dataHealthModel.setCreatedAt(new Date());
            dataHealthModel.setCreatedBy(userId);
        }

        dataHealthModel.setDataHealthType(dataHealthDTO.getDataHealthType());
        dataHealthModel.setDatasetId(datasetId);
        dataHealthModel.setBranch(branch);
        dataHealthModel.setRule(dataHealthDTO.getRule());
        dataHealthModel.setNotes(dataHealthDTO.getNotes());
        dataHealthModel.setUpdatedAt(new Date());
        dataHealthModel.setUpdatedBy(userId);

        dataHealthRepository.save(dataHealthModel);
    }

    /*
        JOBSTATUS:
            PYTHON & SQL DATASET: Checked at checkpoint
            CONNECT: Checked at finished using linkId present in builder of buildLog
        SYNCSTATUS:
            CONNECT: Checked at finished using syncSpecId present in builder of buildLog
        BUILDSTATUS:
            PYTHON & SQL Dataset:
            CONNECT: Checked at finished using linkId present in builder of buildLog
     */

    public void performHealthCheck(UUID datasetId, String branch, BuildLog buildLog) throws Exception {
        List<DataHealthModel> dataHealthModels = get(datasetId, branch);

        for (DataHealthModel dataHealthModel : dataHealthModels) {
            IDataHealth dataHealthService = dataHealthFactory.getDataHealthService(dataHealthModel.getDataHealthType());
            dataHealthService.performHealthCheck(dataHealthModel, buildLog);
        }
    }

    public void performHealthCheckSpecificType(UUID datasetId, String branch, BuildLog buildLog, DataHealthTypeEnum type) throws Exception {
        List<DataHealthModel> dataHealthModels = get(datasetId, branch);

        for (DataHealthModel dataHealthModel : dataHealthModels) {
            if (Objects.equals(type, dataHealthModel.getDataHealthType())) {
                IDataHealth dataHealthService = dataHealthFactory.getDataHealthService(dataHealthModel.getDataHealthType());
                dataHealthService.performHealthCheck(dataHealthModel, buildLog);
            }
        }
    }

    public List<DataHealthModel> get(UUID datasetId, String branch) {
        if (dataHealthRepository.existsDataHealthModelByDatasetIdAndBranch(datasetId, branch)) {
            return dataHealthRepository.findByDatasetIdAndBranch(datasetId, branch);
        } else {
            return new ArrayList<>();
        }
    }

    public void delete(UUID id) {
        dataHealthRepository.deleteById(id);
    }

    public DataHealthModel getDataHealthCheck(UUID id) {
        return dataHealthRepository.findById(id).orElseThrow();
    }
}