package io.movetodata.dataset.library.DataHealthFactory;

import io.movetodata.build.library.models.BuildLog;
import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import io.movetodata.dataset.library.models.DataHealth.DataHealthModel;

public interface IDataHealth {
    DataHealthTypeEnum getDataHealthType();

    void performHealthCheck(DataHealthModel dataHealthModel, BuildLog buildLog) throws Exception;

}
