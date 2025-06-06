package io.bosler.connect.library.services;

import io.bosler.connect.library.models.FolderLinkConfig;
import io.bosler.connect.library.repository.FolderLinkConfigRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FolderLinkConfigService {
    private final FolderLinkConfigRepository folderLinkConfigRepository;

    public FolderLinkConfigService(FolderLinkConfigRepository folderLinkConfigRepository) {
        this.folderLinkConfigRepository = folderLinkConfigRepository;
    }

    public FolderLinkConfig findById(UUID id) {
        return folderLinkConfigRepository.findById(id).orElseThrow();
    }

    public FolderLinkConfig save(FolderLinkConfig folderSourceConfig) {
        return folderLinkConfigRepository.save(folderSourceConfig);
    }
}
