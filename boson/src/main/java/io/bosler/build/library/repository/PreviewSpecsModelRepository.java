package io.bosler.build.library.repository;

import io.bosler.build.library.keys.PreviewSpecsKey;
import io.bosler.build.library.models.PreviewSpecsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreviewSpecsModelRepository extends JpaRepository<PreviewSpecsModel, PreviewSpecsKey> {
}