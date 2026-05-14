package io.orphea.dataset.library.DataHealthFactory;

import io.orphea.build.library.models.BuildLog;
import io.orphea.dataset.library.enums.DataHealthTypeEnum;
import io.orphea.dataset.library.models.DataHealth.DataHealthModel;

public interface IDataHealth {
    DataHealthTypeEnum getDataHealthType();

    void performHealthCheck(DataHealthModel dataHealthModel, BuildLog buildLog) throws Exception;

}
