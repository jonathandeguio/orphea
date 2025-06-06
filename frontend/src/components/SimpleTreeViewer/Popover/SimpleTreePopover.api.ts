import axios from "axios";

export const getTableExplanationAPI = (sourceId: string, tableName: string) => {
  return axios.get(`/connect/source/${sourceId}/${tableName}/explain`);
};
