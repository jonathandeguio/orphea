package io.movetodata.platform.library.repository;

import io.movetodata.platform.library.models.DataMartConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataMartConfigRepository extends JpaRepository<DataMartConfigModel, String> {
    DataMartConfigModel findByConfig(String config);

    boolean existsByConfig(String config);
}
