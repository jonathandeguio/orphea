package io.orphea.accessManager.library.repository;

import io.orphea.accessManager.library.models.AccessRequestModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccessManagerRepository extends JpaRepository<AccessRequestModel, UUID> {
    Page<AccessRequestModel> findAll(Specification<AccessRequestModel> spec, Pageable pageable);
}
