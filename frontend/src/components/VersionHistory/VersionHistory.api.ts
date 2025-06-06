import axios from "axios";

export const getDashboardVersionsAPI = (dashboardId: string) => {
  return axios.get(`/kepler/dashboards/getVersions/${dashboardId}`);
};

export const createVersionAPI = (
  resourceId: string,
  pageType: "dashboard" | "chart",
  name: string,
  versionId?: number
) => {
  if (pageType == "dashboard") {
    if (versionId != undefined) {
      return axios.post(
        `/kepler/dashboards/createVersion/${resourceId}/${name}?v=${versionId}`
      );
    }
    return axios.post(`/kepler/dashboards/createVersion/${resourceId}/${name}`);
  } else if (pageType == "chart") {
    if (versionId != undefined) {
      return axios.post(
        `/kepler/charts/createVersion/${resourceId}/${name}?v=${versionId}`
      );
    }
    return axios.post(`/kepler/charts/createVersion/${resourceId}/${name}`);
  }
};

export const getChartVersionsAPI = (chartId: string) => {
  return axios.get(`/kepler/charts/getVersions/${chartId}`);
};

export const getResourceHistoryAPI = (resourceId: string) => {
  return axios.get(`/kepler/history/${resourceId}`);
};

export const renameVersionAPI = (
  resourceId: string,
  versionId: string,
  name: string
) => {
  return axios.put(`/kepler/rename`, {
    resourceId: resourceId,
    versionId: versionId,
    name: name,
  });
};
