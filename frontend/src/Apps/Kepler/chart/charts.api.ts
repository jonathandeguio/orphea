import axios, { AxiosHeaders, AxiosResponse } from "axios";

/**
 * changes chart description
 * @param chartId Id of chart to update
 * @param newChartDescription new description for chart
 */
export const renameChartDescription = (
  chartId: string,
  newChartDescription: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/kepler/charts/${chartId}/${newChartDescription}/renameChartDescription`
  );
};

/**
 * fetches columns schema for a dataset
 * @param datasetId Id of dataset
 * @param branch branch of dataset
 * @param transactionId transactionId of key of datasetMapping
 */
export const fetchSchema = (
  datasetId: string,
  branch: string,
  transactionId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/dataset/schema/${datasetId}/${branch}/${transactionId}/columns`
  );
};

/**
 * fetches dashboards a chart is attached to
 * @param chartId Chart Id
 */
export const fetchChartDashboards = (
  chartId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kepler/dashboards/getDashboardByChartId/${chartId}`);
};

/**
 * Creates a new Chart
 * @param chartPayload Chart Payload
 */
export const createNewChart = (
  chartPayload: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kepler/charts/new`, JSON.stringify(chartPayload));
};

/**
 * fetches dataset details
 * @param datasetId Dataset Id
 */
export const fetchDatasetDetails = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${datasetId}`);
};

/**
 * get folder details
 * @param folderId Folder Id
 */
export const getFolderDetails = (
  folderId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/folder/${folderId}`);
};

/**
 * get unique values for a column
 * @param payload columns array
 */
export const getColumnUniqueValues = (
  payload: Array<any>
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/dataset/uniqueColumnValues`, payload);
};

/**
 * creates new dashboard
 * @param payload new dashboard payload
 */
export const createnewDashboard = (
  payload: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kepler/dashboards/new`, JSON.stringify(payload));
};

/**
 * get path details
 * @param id resource id
 */
export const getPathApi = (id: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${id}/getPath`);
};

/**
 * loads a query data
 * @param body query body for fetching data
 */
export const getChartData = (
  body: any,
  signal: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/dataset/sqlConfigData`, body, { signal });
};

/**
 * Compares two dataset Schema
 * @param body query body for fetching data
 */
export const compareSchema = (
  sourceDatasetId: string,
  destinationDatasetId: string,
  sourceBranch: string,
  destinationBranch: string
) => {
  return axios.get(
    `/dataset/schema/${sourceDatasetId}/${destinationDatasetId}/${sourceBranch}/${destinationBranch}/compareSchema`
  );
};
