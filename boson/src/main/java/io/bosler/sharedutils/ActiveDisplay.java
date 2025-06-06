package io.bosler.sharedutils;

import io.bosler.kitab.library.enums.ResourceStatus;
import io.bosler.kitab.library.models.ResourceModel;
import io.bosler.passport.library.service.AuthzService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ActiveDisplay {
    private final AuthzService authzService;

    public List<ResourceModel> statusDisplay(UUID userId, List<ResourceModel> resourceModels, ResourceStatus status) {
        List<ResourceModel> activeResourceModel = new ArrayList<>();
        for (ResourceModel resourceModel : resourceModels) {
            if (resourceModel.getStatus() == status && (authzService.isViewer(userId, resourceModel.getId()) ||
                    authzService.isPlatformAdmin(userId) ||
                    authzService.isProjectAdmin(userId))) {

                activeResourceModel.add(resourceModel);
            }
        }

        activeResourceModel.sort(Comparator.comparing(ResourceModel::getName));
        return activeResourceModel;
    }

    public Boolean hasResourceAccess(UUID userId, ResourceModel resource) {
        return authzService.isViewer(userId, resource.getId())
                || authzService.isPlatformAdmin(userId)
                || authzService.isProjectAdmin(userId);
    }
}
