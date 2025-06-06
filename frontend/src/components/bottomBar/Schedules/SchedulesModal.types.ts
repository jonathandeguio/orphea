import { ResourceType } from "Apps/explorer/explorer.utils";
import { JobStatusEnum, ScheduleTriggerType } from "./SchedulesModal.constants";

export type TJobStatusEnum = keyof typeof JobStatusEnum;
export type TScheduleTriggerType = keyof typeof ScheduleTriggerType;

export interface TTrigger {
  triggerId?: string;
  triggerValue: string; // String value containing cronExpression or source ds
  operator?: "and"; // and
  repeatTime?: number; // Needed in by source case
  //    private String jobClass; // This isnt sent from frontend, it will be replaced in backend based on resource type
  lastBuild?: number; // source , to keep track about the source last build
  sourceUpdatedByBuild?: boolean;
}
export interface TScheduleJobInfo {
  createdAt?: number;
  updatedAt?: number;
  createdBy?: string;
  updatedBy?: string;

  jobId?: string;
  jobName?: string;
  resourceId: string;
  resourceType: ResourceType;
  branch: string; // Only needed in case of datase : stringt
  jobGroup?: string; // Dont send from fronten : stringd
  jobStatus: TJobStatusEnum;
  // Build builder
  // Dont pass builder from frontend resolve on backend
  builder?: string;
  jobClass?: string;
  triggerType: TScheduleTriggerType;
  triggers: TTrigger[];

  lastExecution?: number;
  lastJobExecutionStatus?: JobExecutionStatusEnum;
  successExecutionCount?: number;
  failureExecutionCount?: number;
}

export interface TScheduleFilters {
  searchText: string | undefined;
  scheduleTriggerType: string[];
  jobStatus: string[];
  rangeFrom: number;
  rangeTo: number;
  lastExecutionDateFrom: number;
  lastExecutionDateTo: number;
}

export enum JobExecutionStatusEnum {
  STARTED = "STARTED",
  PENDING = "PENDING",
  RUNNING = "RUNNING",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
}
export interface IScheduleLog {
  id: string;
  startedAt: number;
  endedAt: number;
  jobExecutionStatus: JobExecutionStatusEnum;
  jobId: string;
  executionLogsDetails: string[];
}
