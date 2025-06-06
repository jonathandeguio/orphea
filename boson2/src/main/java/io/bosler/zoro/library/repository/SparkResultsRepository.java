package io.bosler.zoro.library.repository;

import io.bosler.zoro.library.models.SchemaModel;
import io.bosler.zoro.library.models.SparkResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SparkResultsRepository
        extends JpaRepository<SparkResults, UUID> {
}