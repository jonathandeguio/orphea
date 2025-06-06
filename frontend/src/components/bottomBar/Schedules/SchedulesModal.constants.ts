import { getYesterdayDate } from "utils/utilities";
import { TScheduleFilters } from "./SchedulesModal.types";

export const DEFAULT_BRANCH = "master";
export const DEFAULT_CRON = "0 * * * *";
export const DEFAULT_RETRY_COUNT = 3;
export const DEFAULT_RADIO = 1;
export const SCHEDULED_DATASET_MESSAGE = "Scheduling dataset : ";
export const BY_TIME = "byTime";
export const BY_SOURCE = "bySource";
export const FAILURE_RETRIVES = "failureRetrives";
export const TITLE_TEXT = "scheduleService";
export const CLOSE_TEXT = "close";
export const OPERATORS_OPTIONS = [
  { label: "and", value: "and" },
  { label: "or", value: "or" },
];
export const DEFAULT_OPERATOR = "and";
export const DATASET_PLACEHOLDER = "Select Dataset";
export const OPERATOR_PLACEHOLDER = "Select Operator";
export const ADD = "Add";
export const SCHEDULE_AVAILABLE_TEXT = "Schedule Available";
export const SCHEDULE_BY_SOURCE_TEXT = "By Source, check on source tab";
export const UPDATE_SCHEDULE_TEXT = "update";
export const SCHEDULE_TEXT = "schedule";

export const getDefaultSchedulesFilter = () => {
  return {
    searchText: undefined,
    scheduleTriggerType: [],
    jobStatus: [],
    rangeFrom: undefined,
    rangeTo: undefined,
    lastExecutionDateFrom: getYesterdayDate(),
    lastExecutionDateTo: undefined,
  } as unknown as TScheduleFilters;
};

export enum ScheduleTriggerType {
  CRON = "CRON",
  SOURCE = "SOURCE",
  NONE = "NONE",
}

export enum JobStatusEnum {
  DELETED = "DELETED",
  SCHEDULED = "SCHEDULED",
  RUNNING = "RUNNING",
  PAUSED = "PAUSED",
}
