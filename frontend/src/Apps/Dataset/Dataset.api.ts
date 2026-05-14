import axios, { AxiosResponse } from "axios";

/**
 * get Dataset Details
 * @param datasetId Dataset Id
 
 */
export const getDatasetAPI = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${datasetId}`);
};

/**
 * fetches user details
 * @param id User Id
 */
export const fetchUserDetailsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/${id}`);
};

/**
 * gets all the dataset transactions
 * @param datasetId Dataset ID
 * @param branch Current dataset branch
 */
export const getDatasetMappingAPI = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/dataset/datasetMapping/${datasetId}/${branch}`);
};

/**
 * gets all the dataset transactions
 * @param datasetId Dataset ID
 * @param branch Current dataset branch
 * @param date Current Local Date in string
 */
export const getDatasetMappingTransactionsAPI = (
  datasetId: string,
  branch: string,
  date: string | null
): Promise<AxiosResponse<any, any>> => {
  if (date) {
    return axios.get(
      `/dataset/datasetMapping/transactions/${datasetId}/${branch}?date=${date}`
    );
  } else {
    return axios.get(
      `/dataset/datasetMapping/transactions/${datasetId}/${branch}`
    );
  }
};

/**
 * Update dataset history count and source
 * @param datasetId Dataset ID
 * @param branch Current dataset branch
 * @param source Current history source : "DATASET" | "PLATFORM"
 * @param count number of transactions
 */
export const updateDatasetHistorySourceAndCountAPI = (
  datasetId: string,
  branch: string,
  source: "DATASET" | "PLATFORM",
  count: number
): Promise<AxiosResponse<any, any>> => {
  return axios.put(
    `/dataset/datasetMapping/update/${datasetId}/${branch}/${source}/${count}`
  );
};

/**
 * fetches all dataset branches
 * @param datasetId dataset's id
 */
export const getDatasetBranchesAPI = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/branch/datasetBranches/${datasetId}`);
};
