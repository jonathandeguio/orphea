package io.movetodata.dataset.library.repository;

import io.movetodata.dataset.library.models.SparkResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SparkResultsRepository
        extends JpaRepository<SparkResults, UUID> {
}