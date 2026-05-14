import {
  BuildLauncedByEnum,
  BuildStatusEnum,
} from "components/Builds/Builds.constants";

export interface TTransaction {
  id: string;
  datasetId: string;
  branch: string;
  buildStatus: keyof typeof BuildStatusEnum;
  launchedBy: keyof typeof BuildLauncedByEnum;
  createdAt: number;
  finishedAt: number;
  createdBy: string;
}
export interface TDatasetMapping {
  datasetId: string;
  branch: string;
  currentTransaction: string;
  originalCurrentTransaction: string;
  historyStoreType: "DATASET" | "PLATFORM";
  datasetHistory: number;
  transactions: TTransaction[];
  validDates: any[];
}
