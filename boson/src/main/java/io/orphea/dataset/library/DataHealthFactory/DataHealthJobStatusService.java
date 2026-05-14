package io.orphea.dataset.library.DataHealthFactory;

import io.orphea.build.BobEnums.BuildStage;
import io.orphea.build.BobEnums.BuildStatus;
import io.orphea.build.BobEnums.BuildTrigger;
import io.orphea.build.library.models.BuildLog;
import io.orphea.dataset.library.enums.DataHealthTypeEnum;
import io.orphea.dataset.library.models.DataHealth.DataHealthLogModel;
import io.orphea.dataset.library.models.DataHealth.DataHealthModel;
import io.orphea.dataset.library.repository.DataHealthLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataHealthJobStatusService implements IDataHealth {
    private final DataHealthLogRepository dataHealthLogRepository;

    @Override
    public DataHealthTypeEnum getDataHealthType() {
        return DataHealthTypeEnum.JOBSTATUS;
    }


    @Override
    public void performHealthCheck(DataHealthModel dataHealthModel, BuildLog buildLog) throws Exception {
        DataHealthLogModel dataHealthLogModel = new DataHealthLogModel();
        boolean isPassed = false;
        if (Objects.equals(buildLog.getTrigger(), BuildTrigger.CONNECT)) {
            isPassed = buildLog.getStage().equals(BuildStage.FINISHED) && buildLog.getStatus().equals(BuildStatus.SUCCESS);
        } else {
            isPassed = buildLog.getCheckpointStatus().equals(BuildStatus.SUCCESS);
        }

        String message = isPassed ? "Job succeeded" : "Job failed";
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
