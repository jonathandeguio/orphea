export interface IPreviewDatasetBySQL {
  datasetId: string;
  branch: string;
  transactionId: string;
  query: string;
}

export interface IPreviewDatasetModel {
  datasetId: string;
  branch: string;
  transactionId: string;
  query: string;
  userId: string;
  createdAt: number;
}
export interface IPreviewDatasetBySQLResults {
  rows: any;
  cols: any;
  data: any;
}
