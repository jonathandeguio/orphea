package io.orphea.dataset.library.services;

import io.orphea.dataset.library.models.SparkResults;
import io.orphea.dataset.library.repository.SparkResultsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SparkResultsService {
    private final SparkResultsRepository sparkResultsRepository;

    public void deleteById(UUID id) {
        sparkResultsRepository.deleteById(id);
    }
    public SparkResults save(SparkResults sparkResults) {
        return sparkResultsRepository.save(sparkResults);
    }
    public Optional<SparkResults> findById(UUID id) {
        return sparkResultsRepository.findById(id);
    }
}
