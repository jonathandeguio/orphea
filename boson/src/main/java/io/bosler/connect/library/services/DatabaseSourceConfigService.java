package io.bosler.connect.library.services;

import io.bosler.connect.library.models.DatabaseSourceConfig;
import io.bosler.connect.library.repository.DatabaseSourceConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class DatabaseSourceConfigService {
    private final DatabaseSourceConfigRepository databaseSourceConfigRepository;

    public DatabaseSourceConfigService(DatabaseSourceConfigRepository databaseSourceConfigRepository) {
        this.databaseSourceConfigRepository = databaseSourceConfigRepository;
    }

    public DatabaseSourceConfig findById(UUID id) {
        return databaseSourceConfigRepository.findById(id).orElseThrow();
    }

    public DatabaseSourceConfig save(DatabaseSourceConfig databaseSourceConfig) {
        return databaseSourceConfigRepository.save(databaseSourceConfig);
    }
}
