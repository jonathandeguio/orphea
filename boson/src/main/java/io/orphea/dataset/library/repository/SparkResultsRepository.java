package io.orphea.dataset.library.repository;

import io.orphea.dataset.library.models.SparkResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SparkResultsRepository
        extends JpaRepository<SparkResults, UUID> {
}