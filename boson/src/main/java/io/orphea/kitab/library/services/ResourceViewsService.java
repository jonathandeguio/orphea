package io.orphea.kitab.library.services;

import io.orphea.kitab.library.enums.ViewAction;
import io.orphea.kitab.library.models.ResourceViewsModel;
import io.orphea.kitab.library.repository.ResourceViewsRepository;
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
