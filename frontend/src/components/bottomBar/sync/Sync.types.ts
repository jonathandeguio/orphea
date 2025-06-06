export interface TCreateSync {
  datasetId: string;
  branch: string;
  tableName: string;
  indexNames: string;
  enabled: boolean;
}

export interface TSync {
  datasetId: string;
  branch: string;
  // datasetSync: TDatasetSync;
}

export interface TDatasetSync {
  key: string;
  id: string | undefined;
  datasetId: string;
  branch: string;
  sourceId: string | undefined;
  tableName: string;
  autoSyncOnBuild: boolean;
  createdAt: number | undefined;
  createdBy: string | undefined;
  updatedBy: string | undefined;
  updatedAt: number | undefined;
  syncIndexes: TSyncIndex[];
  isDataMartSyncSpec: boolean | undefined;
  dataMartId: string | undefined;
}

export interface TSyncIndex {
  key: string;
  id: undefined | string;
  columns: string[];
}

export interface TDatamartSync {
  columns?: any;
  datasetId: string;
  tableName: string;
  syncIndexes?: any;
  syncId: string;
}
