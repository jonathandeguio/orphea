import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { ICsvPreprocessing } from "components/CsvPreprocessing/CsvPreprocessing.types";
import { ScheduleTriggerType } from "components/bottomBar/Schedules/SchedulesModal.constants";
import { WriteModeEnum } from "global";
import { IWebhookRequest } from "../Webhook/Webhook.types";

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
  writeMode: WriteModeEnum;
  trigger: ScheduleTriggerType;
  datasetId: string;
  build: boolean;
  cronExpression: string;

  // Folder connector
  subFolder: string | undefined;

  // Sharepoint specific
  fileId: string | undefined;
  sheetName: string | undefined;
  fileType: ResourceSubTypeEnum | undefined;

  // Requests
  requests?: IWebhookRequest[];
  responseParam?: string;
  csvPreprocessing?: ICsvPreprocessing;
}
