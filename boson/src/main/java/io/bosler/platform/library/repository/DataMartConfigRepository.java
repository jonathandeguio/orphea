package io.bosler.platform.library.repository;

import io.bosler.platform.library.models.DataMartConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataMartConfigRepository extends JpaRepository<DataMartConfigModel, String> {
    DataMartConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
