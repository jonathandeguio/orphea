package io.movetodata.kitab.library.services;

import io.movetodata.kitab.library.enums.ViewAction;
import io.movetodata.kitab.library.models.ResourceViewsModel;
import io.movetodata.kitab.library.repository.ResourceViewsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ResourceViewsService {
    private final ResourceViewsRepository resourceViewsRepository;

    public ResourceViewsModel logView(UUID resourceId, ViewAction viewAction, UUID userId) {
        ResourceViewsModel log = ResourceViewsModel.builder().resourceId(resourceId).viewedBy(userId).action(viewAction.toString().toLowerCase()).build();
        return resourceViewsRepository.save(log);
    }
}
