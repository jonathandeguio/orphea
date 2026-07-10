package io.movetodata.dataset.library.DataHealthFactory;

import io.movetodata.build.BobEnums.BuildStage;
import io.movetodata.build.BobEnums.BuildStatus;
import io.movetodata.build.library.models.BuildLog;
import io.movetodata.dataset.library.enums.DataHealthTypeEnum;
import io.movetodata.dataset.library.models.DataHealth.DataHealthLogModel;
import io.movetodata.dataset.library.models.DataHealth.DataHealthModel;
import io.movetodata.dataset.library.repository.DataHealthLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataHealthBuildStatusService implements IDataHealth {
    private final DataHealthLogRepository dataHealthLogRepository;

    @Override
    public DataHealthTypeEnum getDataHealthType() {
        return DataHealthTypeEnum.BUILDSTATUS;
    }


    @Override
    public void performHealthCheck(DataHealthModel dataHealthModel, BuildLog buildLog) throws Exception {
        DataHealthLogModel dataHealthLogModel = new DataHealthLogModel();

        boolean isPassed = buildLog.getStage().equals(BuildStage.FINISHED) && buildLog.getStatus().equals(BuildStatus.SUCCESS);
        String message = isPassed ? "Build succeeded" : "Build failed";
        // Data Health Log
        dataHealthLogModel.setHealthCheckId(dataHealthModel.getId());
        dataHealthLogModel.setDatasetId(dataHealthModel.getDatasetId());
        dataHealthLogModel.setBranch(dataHealthLogModel.getBranch());
        dataHealthLogModel.setIsPassed(isPassed);
        dataHealthLogModel.setMessage(message);
        dataHealthLogModel.setIsCritical(false);

        dataHealthLogModel.setStartedAt(buildLog.getStartedAt());
        dataHealthLogModel.setFinishedAt(buildLog.getFinishedAt());

        dataHealthLogRepository.save(dataHealthLogModel);
    }
}
