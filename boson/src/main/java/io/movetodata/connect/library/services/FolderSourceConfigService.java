package io.movetodata.connect.library.services;

import io.movetodata.connect.library.models.FolderSourceConfig;
import io.movetodata.connect.library.repository.FolderSourceConfigRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FolderSourceConfigService {
    private final FolderSourceConfigRepository folderSourceConfigRepository;

    public FolderSourceConfigService(FolderSourceConfigRepository folderSourceConfigRepository) {
        this.folderSourceConfigRepository = folderSourceConfigRepository;
    }

    public FolderSourceConfig findById(UUID id) {
        return folderSourceConfigRepository.findById(id).orElseThrow();
    }

    public FolderSourceConfig save(FolderSourceConfig folderSourceConfig) {
        return folderSourceConfigRepository.save(folderSourceConfig);
    }
}
