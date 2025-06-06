export interface IDataHealthCheck {
  form: any;
  handleSave: any;
}

export interface IDataHealthDTO {
  rule: string | undefined;
  isEnabled?: boolean | undefined;
  groups?: string | undefined;
  notes: string | undefined;
  issue?: boolean | undefined;
  id?: string | undefined;
  dataHealthType: DataHealthTypeEnum;
}

export interface IDataHealth {
  rule: string | undefined;
  isEnabled: boolean | undefined;
  groups: string | undefined;
  notes: string | undefined;
  issue: boolean | undefined;
  id: string;
  dataHealthType: DataHealthTypeEnum;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface IDataHealthLog {
  id: string;
  healthCheckId: string;
  datasetId: string;
  branch: string;

  isPassed: boolean;
  message: string;
  isCritical: boolean;

  startedAt: number;
  finishedAt: number;
}

export enum DataHealthTypeEnum {
  BUILDSTATUS = "BUILDSTATUS",
  JOBSTATUS = "JOBSTATUS",
  SYNCSTATUS = "SYNCSTATUS",

  //  Size
  DATASET_FILE_COUNT = "DATASET_FILE_COUNT",
  ROW_COUNT = "ROW_COUNT",
  TRANSACTION_FILE_COUNT = "TRANSACTION_FILE_COUNT",
  TRANSACTION_FILE_SIZE = "TRANSACTION_FILE_SIZE",

  // Content
  ALLOWED_COLUMN_VALUES = "ALLOWED_COLUMN_VALUES",

  // Schema
  COLUMN = "COLUMN",
  COLUMN_COUNT = "COLUMN_COUNT",
  SCHEMA = "SCHEMA",
}
