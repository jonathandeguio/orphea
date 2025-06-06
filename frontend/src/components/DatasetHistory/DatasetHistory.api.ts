import axios, { AxiosResponse } from "axios";

export const abortDatasetTransactionAPI = (
  datasetId: string,
  transactionId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/dataset/datasetMapping/${datasetId}/${transactionId}/abort`
  );
};
