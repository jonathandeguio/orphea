package io.orphea.dataset.library.repository;

import io.orphea.dataset.library.models.DatasetDownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DatasetDownloadLogRepository
        extends JpaRepository<DatasetDownloadLog, UUID> {

    DatasetDownloadLog findByDownloadedBy(UUID downloadedBy);

    boolean existsByDownloadedBy(UUID downloadedBy);

    List<DatasetDownloadLog> findAllByOrderByDownloadedAtDesc();

}