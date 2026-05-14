// Build Status

export const ABORTED = "ABORTED";
export const FAILED = "FAILED";
export const SUCCESS = "SUCCESS";
export const COMPLETED = "COMPLETED";
export const CANCELLED = "CANCELLED";
export const ERROR = "ERROR";
export const ACTIVE = "ACTIVE";
export const INFO = "INFO";

export const BuildStatusEnum = {
  SUCCESS: "SUCCESS",
  ACTIVE: "ACTIVE",
  ERROR: "ERROR",
  ABORTED: "ABORTED",
  CANCELLED: "CANCELLED",
  FAILED: "FAILED",
  DELETED: "DELETED",
};
export const BuildLauncedByEnum = {
  MANUAL: "MANUAL",
  UNKNOWN: "UNKNOWN",
  CRON: "CRON",
  SOURCE: "SOURCE",
};

// Logs Stages
export const STARTING = "STARTING";
export const PREPARING = "PREPARING";
export const RUNNING = "RUNNING";
export const FINISHED = "FINISHED";

export enum BuildStageEnum {
  STARTING,
  PREPARING,
  RUNNING,
  FINISHED,
}

// Trigger Types
export const CONNECT = "CONNECT";
export const PYTHON = "PYTHON";
export const SQL = "SQL";
export const DATASET = "DATASET";
export const UPLOAD = "UPLOAD";
export const COLUMNSTATS = "COLUMNSTATS";
export const SYNCHRO = "SYNCHRO";

export enum TriggerTypeEnum {
  CONNECT,
  PYTHON,
  SQL,
  DATASET,
  UPLOAD,
  COLUMNSTATS,
  SYNCHRO,
}
