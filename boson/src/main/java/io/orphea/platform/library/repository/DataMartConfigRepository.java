package io.orphea.platform.library.repository;

import io.orphea.platform.library.models.DataMartConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataMartConfigRepository extends JpaRepository<DataMartConfigModel, String> {
    DataMartConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
