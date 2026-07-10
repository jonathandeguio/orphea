package io.movetodata.build.library.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.movetodata.build.library.dto.BuildPreviewResultRequest;
import io.movetodata.build.library.dto.SourceDataset;
import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.enums.ResourceType;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.platform.library.repository.CacheRepository;
import io.movetodata.sharedutils.Exceptions.ResourceNotFoundException;
import io.movetodata.sharedutils.Redis;
import io.movetodata.sharedutils.RedisKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.movetodata.sharedutils.Utils.isValidUuid;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class FunnelUtils {
    private final ResourceService resourceService;
    private final CacheRepository cacheRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public List<SourceDataset> verifyFunnelSources(List<SourceDataset> sources) throws ResourceNotFoundException {
        List<SourceDataset> verifiedSources = new ArrayList<>();
        for (SourceDataset source : sources) {
            String sourceAsIdOrPath = source.getSource();
            if (isValidUuid(sourceAsIdOrPath)) {
                verifiedSources.add(source);
            } else {
                ResourceModel resourceModel = getResourceByPath(sourceAsIdOrPath, false);
                System.out.println("Source not found : " + sourceAsIdOrPath);
                if (resourceModel == null) {
                    throw new ResourceNotFoundException("Source not found : " + sourceAsIdOrPath);
                }
                if (resourceModel.getType() != ResourceType.DATASET) {
                    throw new ResourceNotFoundException("Provided Source " + sourceAsIdOrPath + " (" + resourceModel.getName() + ")  is not a dataset.");
                }
                verifiedSources.add(new SourceDataset((resourceModel.getId()).toString(), source.getBranch()));
            }
        }
        return verifiedSources;
    }

    public ResourceModel getResourceByPath(String path, boolean parentPath) {
        UUID resourceId = null;
        String[] individualPath = path.split("/");

        ResourceModel tempModel = null;

        int terminatingIndex = parentPath ? individualPath.length - 1 : individualPath.length;
        for (int i = 2; i < terminatingIndex; i++) {
            tempModel = resourceService.findByNameAndParentAndStatus(individualPath[i], resourceId, ResourceStatus.ACTIVE);
            if (tempModel == null) {
                return null;
            }
            resourceId = tempModel.getId();
        }

        return tempModel;
    }

    @Transactional
    public void setPreviewResult(UUID userId, BuildPreviewResultRequest request, UUID repoId, String type) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String targetName = "Unnamed";
        try {
            ResourceModel resourceModel = resourceService.findById(UUID.fromString(request.getTarget())).orElseThrow();
            targetName = resourceModel.getName();
        } catch (Exception e) {
            String[] parts = request.getTarget().split("/");
            int index = parts.length - 1;
            if (index >= 0) {
                targetName = parts[index];
            }
            log.error(e.getMessage());
        }
        Map<String, Object> existingResult = getPreviewResult(request.getBuildId());

        // Convert the existing Map to a List of Map objects
        List<Map<String, Object>> resultList = new ArrayList<>();
        existingResult.forEach((key, value) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", key);
            result.put("data", value);
            resultList.add(result);
        });

        // Append the new name and data as a Map
        Map<String, Object> newResult = new HashMap<>();
        newResult.put("name", targetName);
        newResult.put("data", request);
        resultList.add(newResult);

        String resultString = objectMapper.writeValueAsString(resultList);

        Redis.setCache(RedisKeyGenerator.preview(request.getBuildId()), resultString, cacheRepository);
    }

    @Transactional
    public Map<String, Object> getPreviewResult(UUID previewId) throws JsonProcessingException {
        String resultString = Redis.getCache(RedisKeyGenerator.preview(previewId), cacheRepository);
        if (resultString == null) {
            return new HashMap<>();
        }
        // Convert JSON string to a List of Map objects
        List<Map<String, Object>> resultList = objectMapper.readValue(resultString, List.class);

        // Convert List to a Map
        Map<String, Object> resultMap = new HashMap<>();
        for (Map<String, Object> result : resultList) {
            resultMap.put((String) result.get("name"), result.get("data"));
        }

        return resultMap;
    }

}
