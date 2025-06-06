import { ResourceType, ResourceTypeEnum } from "Apps/explorer/explorer.utils";
import { openNotification } from "utils/utilities";
import { JobStatusEnum, ScheduleTriggerType } from "./SchedulesModal.constants";
import {
  TJobStatusEnum,
  TScheduleJobInfo,
  TTrigger,
} from "./SchedulesModal.types";
import {
  actionScheduleAPI,
  getDataset,
  getDatasetsDetailsByIdAPI,
  getSchedulesAPI,
  getSourcesAPI,
  putScheduleAPI,
} from "./api";

export const getDatasetName = (id: string, setDatasetName: any) => {
  getDataset(id).then((dataset: any) => {
    setDatasetName(dataset.name);
  });
};

export const handleCancel = (setVisible: any) => {
  setVisible(false);
};

export const getSchedules = (
  resourceId: string,
  branch: string,
  resourceType: ResourceType,
  setSchedule: any,
  setIsLoading: any,
  setCronExpression: any,
  setRadio: any,
  setTriggers: any
) => {
  setIsLoading(true);
  getSchedulesAPI(resourceId, branch, resourceType).then(
    (currentSchedule: any) => {
      // As schedule will be single for dataset always
      setIsLoading(false);
      // currentSchedule.triggerType == "cron";
      if (!currentSchedule) {
        return;
      }
      const triggerType = currentSchedule.triggers[0].triggerType;

      if (currentSchedule.triggerType == ScheduleTriggerType.CRON) {
        setCronExpression(currentSchedule.triggers[0].triggerValue);
        setRadio(1);
      } else if (currentSchedule.triggerType == ScheduleTriggerType.SOURCE) {
        const sourceTriggers: any = [];

        currentSchedule.triggers.map((trigger: any) => {
          sourceTriggers.push({
            id: trigger.triggerValue,
            operator: trigger.operator,
            triggerType: trigger.triggerType,
            name: "name",
          });
        });
        setTriggers(sourceTriggers);
        setRadio(2);
      }
      setSchedule(currentSchedule);
    }
  );
};

export const actionSchedule = (jobId: string, action: TJobStatusEnum) => {
  actionScheduleAPI(jobId, action);
};

export const onDeleteHandler = (
  schedule: TScheduleJobInfo,
  setSchedule: any
) => {
  if (schedule.jobId) {
    actionSchedule(schedule.jobId, JobStatusEnum.DELETED);
    setSchedule(undefined);
  }
};

export const putSchedules = (
  payload: TScheduleJobInfo,
  setIsLoading: any,
  setSchedule: any
) => {
  setIsLoading(true);
  putScheduleAPI(payload).then((schedule: any) => {
    setSchedule(schedule);
    setIsLoading(false);
  });
};

export const onSchedule = (
  schedule: any,
  radio: any,
  id: string,
  branch: string,
  retry: number,
  cronExpression: string,
  triggers: TTrigger[],
  setIsLoading: any,
  setSchedule: any
) => {
  let payload: TScheduleJobInfo;
  const triggerType =
    radio == 1 ? ScheduleTriggerType.CRON : ScheduleTriggerType.SOURCE;
  if (schedule) {
    payload = schedule;
    payload.triggerType = triggerType;
  } else {
    payload = {
      resourceId: id,
      resourceType: ResourceTypeEnum.DATASET,
      branch: branch,
      jobStatus: JobStatusEnum.RUNNING,
      triggerType: triggerType,
      triggers: [],
    };
  }
  if (triggerType == ScheduleTriggerType.CRON) {
    let trigger: TTrigger = {
      triggerValue: cronExpression,
    };

    payload.triggers = [trigger];
  } else if (radio == 2) {
    if (triggers.length == 0) {
      openNotification("Add one or more sources from dropdown", " ", "error");
      return;
    }
    const scheduletriggers: TTrigger[] = [];
    triggers.map((trigger: any) => {
      scheduletriggers.push({
        operator: trigger.operator,
        triggerValue: trigger.id,
      });
    });
    payload.triggers = scheduletriggers;
  }
  putSchedules(payload, setIsLoading, setSchedule);
};

export const getSources = (
  id: string | undefined,
  branch: string | undefined,
  setIsLoading: any,
  triggers: any,
  setTriggers: any,
  setSources: any
) => {
  if (id && branch) {
    setIsLoading(true);
    getSourcesAPI(id, branch).then((sources: any) => {
      const datasetIds: any[] | undefined = [];
      sources.map((source: { id: any }) => datasetIds.push(source.id));

      const nameMap = new Map();
      getDatasetsDetailsByIdAPI(datasetIds).then((data: any) => {
        data.map((source: { id: any; name: any }) => {
          nameMap.set(source.id, source.name);
        });
        sources.map((source: { name: any; id: any }) => {
          source.name = nameMap.get(source.id);
        });

        // Remove already present triggers, from sources

        let triggersFilteredSources = sources;
        triggers.map((trigger: any) => {
          trigger.name = nameMap.get(trigger.id);
          triggersFilteredSources = triggersFilteredSources.filter(
            (source: any) => source.id != trigger.id
          );
        });
        setTriggers(triggers);
        setSources(triggersFilteredSources);
        setIsLoading(false);
      });
    });
  }
};

export const handleAdd = (
  dataset: any,
  triggers: any,
  operator: any,
  sources: any,
  setTriggers: any,
  setSources: any,
  setDataset: any
) => {
  if (!dataset) {
    openNotification("Fill out dataset", "", "warning");
    return;
  }

  const triggersLocal = [...triggers];
  triggersLocal.push({
    operator: operator,
    id: dataset.id,
    name: dataset.name,
    triggerType: ScheduleTriggerType.SOURCE,
  });

  const newSources = sources?.filter((source: any) => source.id != dataset.id);

  setTriggers(triggersLocal);
  setSources(newSources);
  setDataset(undefined);
};

export const handleRemove = (
  clickedTrigger: any,
  triggers: any,
  setTriggers: any,
  sources: any,
  setSources: any
) => {
  const triggersLocal = triggers;
  const newTriggers = triggersLocal.filter(
    (trigger: any) => trigger.id != clickedTrigger.id
  );
  setTriggers(newTriggers);
  const newSources = sources;
  newSources?.push({
    id: clickedTrigger.id,
    name: clickedTrigger.name,
  });
  setSources(newSources);
};
