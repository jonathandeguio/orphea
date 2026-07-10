package io.movetodata.build.library.services;

import io.movetodata.kitab.library.enums.ResourceStatus;
import io.movetodata.kitab.library.models.ResourceModel;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.sharedutils.Exceptions.EnvConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KitabService {

    private final ResourceService resourceService;

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

    public String getPhysicalEndpoint() throws EnvConfigurationException {
        String endpoint;
        if (System.getenv("BACKING_FS").equals("s3")) {
            endpoint = "s3a://bosler/dataset";
        } else if (System.getenv("BACKING_FS").equals("gs")) {
            endpoint = "gs://" + System.getenv("GS_BUCKET") + "/bosler/dataset";
        } else if (System.getenv("BACKING_FS").equals("hdfs")) {
            endpoint = System.getenv("HDFS_ENDPOINT") + "/bosler/dataset";
        } else if (System.getenv("BACKING_FS").equals("localfs")) {
            endpoint = System.getenv("LOCAL_FS_DIRECTORY") + "/dataset";
        } else {
            throw new EnvConfigurationException("No Backing FS defined");
        }

        return endpoint;
    }

}
