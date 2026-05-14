package io.orphea.connect.library.services;

import io.orphea.connect.library.models.SharepointLinkConfig;
import io.orphea.connect.library.repository.SharepointLinkConfigRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SharepointLinkConfigService {
    private final SharepointLinkConfigRepository sharepointLinkConfigRepository;

    public SharepointLinkConfigService(SharepointLinkConfigRepository sharepointLinkConfigRepository) {
        this.sharepointLinkConfigRepository = sharepointLinkConfigRepository;
    }

    public SharepointLinkConfig findById(UUID id) {
        return sharepointLinkConfigRepository.findById(id).orElseThrow();
    }

    public SharepointLinkConfig save(SharepointLinkConfig folderSourceConfig) {
        return sharepointLinkConfigRepository.save(folderSourceConfig);
    }
}
