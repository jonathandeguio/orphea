package io.movetodata.dataset.library.services.DataHealth;

import io.movetodata.dataset.library.models.DataHealth.DataHealthLogModel;
import io.movetodata.dataset.library.repository.DataHealthLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataHealthLogService {
    private final DataHealthLogRepository dataHealthLogRepository;

    public List<DataHealthLogModel> getLogs(UUID id) {
        if (dataHealthLogRepository.existsByHealthCheckId(id)) {
            return dataHealthLogRepository.findByHealthCheckId(id);
        }
        return new ArrayList<>();
    }
}
