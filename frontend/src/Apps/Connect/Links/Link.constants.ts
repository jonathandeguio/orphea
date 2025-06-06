import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { getInitialValues } from "components/CsvPreprocessing/CsvPreprocessing.constants";
import { ScheduleTriggerType } from "components/bottomBar/Schedules/SchedulesModal.constants";
import { DEFAULT_CRON_JOB } from "components/common/CronJob/CronJob.constants";
import { WriteModeEnum } from "global.d";
import { ILink } from "./Link.types";

export const initialLinkDetails: ILink = {
  id: "",
  // This is updated later in LinkModal.View
  name: "",
  description: "",
  branch: "master",
  type: "",
  parent: "",
  sourceId: "",
  script: "",
  deleteFilesAfterUpload: false,
  dataLiveLoad: false,
  writeMode: WriteModeEnum.SNAPSHOT,
  trigger: ScheduleTriggerType.NONE,
  datasetId: "",
  build: false,
  cronExpression: DEFAULT_CRON_JOB,
  subFolder: "",
  fileId: "",
  sheetName: "",
  fileType: ResourceSubTypeEnum.CSV,
  requests: [],
  csvPreprocessing: getInitialValues(),
};
