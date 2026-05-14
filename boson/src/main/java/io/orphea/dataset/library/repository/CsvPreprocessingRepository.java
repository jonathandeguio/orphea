package io.orphea.dataset.library.repository;

import io.orphea.dataset.library.models.CsvPreprocessingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CsvPreprocessingRepository
        extends JpaRepository<CsvPreprocessingModel, UUID> {

}