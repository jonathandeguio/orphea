package io.movetodata.connect.library.services;

import io.movetodata.connect.library.models.SharePointSourceConfig;
import io.movetodata.connect.library.repository.SharepointConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SharepointConfigService {
    @Autowired
    private SharepointConfigRepository sharepointConfigRepository;

    public SharePointSourceConfig save(SharePointSourceConfig sharePointSourceConfig) {
        SharePointConnectorService.enrichSiteIdAndDriveId(sharePointSourceConfig);
        return sharepointConfigRepository.save(sharePointSourceConfig);
    }

    public SharePointSourceConfig findById(UUID sharePointSourceConfigId) {
        return sharepointConfigRepository.findById(sharePointSourceConfigId).orElse(null);
    }
}
