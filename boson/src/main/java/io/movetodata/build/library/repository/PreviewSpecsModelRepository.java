package io.movetodata.build.library.repository;

import io.movetodata.build.library.keys.PreviewSpecsKey;
import io.movetodata.build.library.models.PreviewSpecsModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreviewSpecsModelRepository extends JpaRepository<PreviewSpecsModel, PreviewSpecsKey> {
}