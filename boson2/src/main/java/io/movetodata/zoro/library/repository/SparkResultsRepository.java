package io.movetodata.zoro.library.repository;

import io.movetodata.zoro.library.models.SchemaModel;
import io.movetodata.zoro.library.models.SparkResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SparkResultsRepository
        extends JpaRepository<SparkResults, UUID> {
}