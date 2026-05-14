import { ScheduleTriggerType } from "components/bottomBar/Schedules/SchedulesModal.constants";

export interface ILink {
  id: string;
  name: string;
  description: string;
  branch: string;
  type: string;
  parent: string;
  sourceId: string;
  script: string;
  deleteFilesAfterUpload: boolean;
  dataLiveLoad: boolean;
  saveMode: string;
  trigger: ScheduleTriggerType;
  datasetId: string;
  build: boolean;
  cronExpression: string;
}
