package io.bosler.build.library.repository;

import io.bosler.build.library.models.BuildLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BuildLogRepository extends JpaRepository<BuildLog, UUID> {

    Page<BuildLog> findAll(Specification<BuildLog> spec, Pageable pageable);

}