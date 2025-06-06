import axios, { AxiosResponse } from "axios";
import { getSQLFormatLink } from "utils/utilities";
import { ISharepointConnector } from "./Sources/Source";

/**
 * Regenerates the agent code
 * @param agentId Id of agent
 */
export const regenerateAgentCodeAPI = (
  agentId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/agent/${agentId}/regenerate`);
};

/**
 * update connect element(agent,source,link)
 * @param type type of connect element(agent,source,link)
 * @param body body of post call to send
 */
export const handleConnectUpdateAPI = (
  type: string,
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/${type}/update`, body);
};

/**
 * get Parent  details
 * @param parentId Parent Id
 */
export const getParentAPI = (
  parentId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/folder/${parentId}`);
};

/**
 * get Parent  details
 * @param id Element Id
 * @param type Element Type (agent,source,link)
 */
export const getConnectElementAPI = (
  id: string,
  type: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/${type}/GetById/${id}`);
};

/**
 * get Agent Stats  details
 * @param id Element Id
 
 */
export const getAgentStatsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/agent/${id}/stats`);
};

/**
 * Start a Connect Build
 * @param id Build Id
 
 */
export const ConnectBuildAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/link/Build/${id}`);
};

/**
 * Preview a Connect Link
 * @param id Build Id
 
 */
export const PreviewAPI = (
  id: string,
  body: any,
  cancelToken: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/link/preview/${id}`, body, { cancelToken });
};

/**
 * Preview a Source Link
 * @param id Build Id
 
 */
export const previewSourceAPI = (
  id: string,
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/source/preview/${id}`, body);
};

/**
 * get Dataset Details
 * @param datasetId Dataset Id
 
 */
export const getDatasetAPI = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${datasetId}`);
};

export const getDatasetDetailsAPI = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/dataset/${datasetId}/${branch}`);
};

/**
 * Fetch Formatted SQl 
 * @param body Body
 
 */
export const fetchFormattedSqlAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`${getSQLFormatLink()}/lsp/formatSQL`, body);
};

/**
 * Test DB Connection
 * @param body Body
 
 */
export const testDBConnectionAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/source/testConnection`, body);
};

/**
 * Get source details
 * @param id source id
 
 */
export const getSourceDetailsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/source/GetById/${id}`);
};

/**
 * Check folder path
 * @param body Body
 
 */
export const checkFolderPathAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/source/checkFolderPath`, body);
};

/**
 * Check folder path
 * @param body Body
 */
export const isDatasetFreeToLinkAPI = (
  datasetId: any
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/isDatasetFreeToLink/${datasetId}`);
};

export const existsDatasetLink = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/link/${datasetId}/${branch}/existsDatasetLink`);
};

export const getSourceContentMetaData = (
  sourceId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/source/${sourceId}/getSourceContentMetaData`);
};

export const getTableColumnsAPI = (sourceId: string, tableName: string) => {
  return axios.get(`/connect/source/${sourceId}/${tableName}/columns`);
};

export const getSharepointFolderChilderAPI = (
  sourceId: string,
  folderId: string
) => {
  return axios.get(`/connect/source/${sourceId}/children/${folderId}`);
};

export const testSharepointConnectionAPI = (payload: ISharepointConnector) => {
  return axios.post(`/connect/source/testSharepointConnection`, payload);
};
