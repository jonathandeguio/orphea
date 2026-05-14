import { ScheduleTriggerType } from "components/bottomBar/Schedules/SchedulesModal.constants";
import { DEFAULT_CRON_JOB } from "components/common/CronJob/CronJob.constants";
import { ILink } from "./Link.types";

export const initialLinkDetails: ILink = {
  id: "",
  name: "",
  description: "",
  branch: "master",
  type: "",
  parent: "",
  sourceId: "",
  script: "",
  deleteFilesAfterUpload: false,
  dataLiveLoad: false,
  saveMode: "overwrite",
  trigger: ScheduleTriggerType.NONE,
  datasetId: "",
  build: true,
  cronExpression: DEFAULT_CRON_JOB,
};
