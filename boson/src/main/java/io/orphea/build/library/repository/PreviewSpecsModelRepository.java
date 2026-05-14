package io.orphea.build.library.repository;

import io.orphea.build.library.keys.PreviewSpecsKey;
import io.orphea.build.library.models.PreviewSpecsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreviewSpecsModelRepository extends JpaRepository<PreviewSpecsModel, PreviewSpecsKey> {
}