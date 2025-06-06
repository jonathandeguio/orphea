import axios, { AxiosResponse } from "axios";

export const getDataMartDatabaseContentMetaData = (
  dataMartId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/getDataMartDatabaseTreeData/${dataMartId}`);
};
