import axios from "axios";
import { IGitLog } from "./types";

/**
 * Fetch logs
 * @param id Repo id
 * @param branch branch
 */
export const getLogsApi = (id: string, branch: string) => {
  return axios.get<IGitLog[]>(`/fractal/${id}/${branch}/logs`);
};

/**
 * Fetch file History
 * @param id Repo id
 * @param filePath file you need history of
 */
export const getFileHistoryApi = (id: string, filePath: string) => {
  return axios.get<IGitLog[]>(`/fractal/${id}/fileHistory`, {
    params: {
      filePath,
    },
  });
};
