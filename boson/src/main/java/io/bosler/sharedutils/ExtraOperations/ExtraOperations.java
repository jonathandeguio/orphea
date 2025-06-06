package io.bosler.sharedutils.ExtraOperations;

import io.bosler.dataset.library.services.DatasetMappingService;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.kitab.library.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExtraOperations {
    private final ResourceRepository resourceRepository;
    private final DatasetMappingService datasetMappingService;

    public void makeDatasetMetaData() {
        /*
         Aim : To populate, metData table of dataset
         id, buildId, buildTrigger, transactionId
         */
        List<ResourceModel> resourceModelList = resourceRepository.findAll();


//        resourceModelList.forEach(resourceModel -> {
//            if (resourceModel.getType() == ResourceType.DATASET) {
//                datasetMappingService.getCurrentTransaction(resourceModel.getId(), resourceModel.)
//            }
//        });
    }
}
