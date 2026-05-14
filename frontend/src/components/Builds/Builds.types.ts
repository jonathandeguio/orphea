export type TFunnelStage = "STARTING" | "PREPARING" | "RUNNING" | "FINISHED";
export type TFunnelStatus = "INFO" | "FAILED" | "ERROR" | "ABORTED";

export interface TBuildLogMessage {
  startedAt: number;
  stage: TFunnelStage;
  status: TFunnelStatus;
  message: string;
  debug: string;
}

export type TBuildTrigger = "CONNECT" | "UPLOAD" | "SQL" | "PYTHON" | "DATASET";
export type TBuildStatus = "SUCCESS" | "FAILED" | "ABORTED" | "ACTIVE";

export interface TBuildLog {
  startingLogMessages: TBuildLogMessage[];
  preparingLogMessages: TBuildLogMessage[];
  runningLogMessages: TBuildLogMessage[];
  finishedLogMessages: TBuildLogMessage[];
  id: string;
  builder: string;
  branch: string;
  scriptPath: string;
  trigger: TBuildTrigger;
  sparkApplicationId: string;
  status: TBuildStatus;

  // Checkpoint
  checkpointDataset: string;
  checkpointStatus: TBuildStatus;
  checkpointTransactionId: string;

  // Different Stages are : Starting, Preparing, Running, Finished -> Make enums
  // Different Status are : Active, success, aborted, failed -> Make enums
  stage: TFunnelStage;
  startedBy: string;
  startedAt: number;
  finishedAt: number;

  startingStageStatus: boolean;
  startingStartedAt: number;
  startingFinishedAt: number;

  preparingStageStatus: boolean;
  preparingStartedAt: number;
  preparingFinishedAt: number;

  runningStageStatus: boolean;
  runningStartedAt: number;
  runningFinishedAt: number;

  finishedStageStatus: boolean;
  finishedStartedAt: number;
  finishedFinishedAt: number;
}

export interface TBuildSpec {
  id: string;
  repository: string;
  scriptPath: string;
  language: string;
  branchId: string;
  commitId: string;

  datasetId: string;
  branch: string;
  transactionId: string;

  buildId: string;

  createdAt: number;
  createdBy: string;

  updatedAt: number;
  updatedBy: string;
}
