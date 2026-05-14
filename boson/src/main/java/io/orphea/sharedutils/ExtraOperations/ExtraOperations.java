package io.orphea.sharedutils.ExtraOperations;

import io.orphea.dataset.library.services.DatasetMappingService;
import io.orphea.kitab.library.models.ResourceModel;
import io.orphea.kitab.library.repository.ResourceRepository;
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
