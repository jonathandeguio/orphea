import axios, { AxiosResponse } from "axios";
import { isDefined } from "utils/utilities";
import { LATEST_VERSION } from "../../../redux/constants/versionConstants";
import { ITabConfig } from "./Dashboard.types";
import { ISuggestedChartsFilters } from "./DashboardAddChartMenu/DashboardAddChartMenuChartsTab.types";

// Dashboard Header APIs
export const renameDashboardAPI = (
  id: string,
  name: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kepler/dashboards/${id}/${name}/rename`);
};

export const getDashboardDataAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kepler/dashboards/getById/${id}`);
};

// Dashboard Tabs APIs

export const addTabToDashboardAPI = async (id: string) => {
  const { data } = await axios.post(`/kepler/tabs/addTabToDashboard/${id}`, {});
  return data;
};

export const removeTabFromDashboardAPI = async (
  id: string,
  targetUniqueId: string
) => {
  const { data } = await axios.delete(
    `/kepler/tabs/removeTabFromDashboard/${id}/${targetUniqueId}`
  );
  return data;
};

export const getDashboardTabsAPI = async (id: string, versionId?: string) => {
  if (versionId != undefined && versionId != LATEST_VERSION) {
    const { data } = await axios.get(
      `/kepler/tabs/getDashboardTabs/${id}?v=${versionId}`
    );
    return data;
  }
  const { data } = await axios.get(`/kepler/tabs/getDashboardTabs/${id}`);
  return data;
};

export const updateDashboardTabNameAPI = async (
  tab: string,
  newValue: string
) => {
  const { data } = await axios.post(
    `/kepler/tabs/updateTab/${tab}/${newValue}`,
    {}
  );
  return data;
};

// Dashboard Add Chart Menu APIs

export const getAllChartsAPI = async (tabId: string) => {
  const { data } = await axios.get(`/kepler/charts/getCharts/${tabId}`);
  return data;
};

export const getChartPopOverInfoAPI = async (chartId: string) => {
  const { data } = await axios.get(`/kepler/charts/popOverInfo/${chartId}`);
  return data;
};

// Dashboard Grid and Elements APIs

export const updateTabCustomizeAPI = async (
  tabId: string,
  dashboardId: string,
  payload: ITabConfig
) => {
  const { data } = await axios.post(
    `/kepler/tabs/updateTabConfig/${dashboardId}/${tabId}`,
    payload
  );
  return data;
};

export const getTabCustomizeAPI = async (tabId: string) => {
  const { data } = await axios.get(`/kepler/tabs/getTabConfig/${tabId}`);
  return data;
};

export const removeTabElementAPI = async (
  dashboardId: string,
  elementId: string
) => {
  const { data } = await axios.delete(
    `/kepler/tabElements/remove/${dashboardId}/${elementId}`
  );
  return data;
};

export const updateTabElementAPI = async (
  dashboardId: string,
  tabId: string,
  payload: any
) => {
  const { data } = await axios.post(
    `/kepler/tabElements/update/${dashboardId}/${tabId}`,
    payload
  );
  return data;
};

export const manageChartOnDashboardAPI = async (payload: any) => {
  const { data } = await axios.post(`/kepler/dashboards/manage`, payload);
  return data;
};

export const manageChartOnTabAPI = async (payload: any) => {
  const { data } = await axios.post(`/kepler/tabs/manageCharts`, payload);
  return data;
};

export const createTabElementAPI = async (payload: any) => {
  const { data } = await axios.post(`/kepler/tabElements/create`, payload);
  return data;
};

export const getTabElementsAPI = (
  dashboardId: string,
  tabId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kepler/tabElements/getElements/${dashboardId}/${tabId}`);
};

// Chart Element

export const getChartDataAPI = async (payload: any) => {
  const { data } = await axios.post(`/dataset/getChartDataByIds`, payload);

  data.forEach((element: any) => {
    if (isDefined(element?.chartState?.chartCustomize)) {
      element.chartState.chartCustomize = JSON.parse(
        element.chartState.chartCustomize
      );
    } else {
      element.chartState.chartCustomize = { seriesCustomize: [] };
    }
  });
  return data;
};

// Dashboard Filters
export const getDashboardDatasets = async (dashboardId: any) => {
  const { data } = await axios.get(
    `/kepler/dashboards/getDatasets/${dashboardId}`
  );
  return data;
};

export const getTabDatasets = async (tabId: any) => {
  const { data } = await axios.get(`/kepler/tabs/getDatasets/${tabId}`);
  return data;
};

export const getDatasetColumns = async (
  datasetId: string,
  branch: string,
  transactionId: string
) => {
  const { data } = await axios.get(
    `/dataset/schema/${datasetId}/${branch}/${transactionId}/columns`
  );
  return data;
};

export const moveTabAPI = (
  tabId: string,
  newPosition: number
): Promise<AxiosResponse<any, any>> => {
  return axios.put(`/kepler/tabs/move/${tabId}/${newPosition}`);
};

export const getSuggestedChartsAPI = (
  dashboardId: string,
  body: ISuggestedChartsFilters,
  page: number,
  pageSize: number,
  sortby: string,
  sortOrder: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/kepler/dashboards/getSuggestedCharts/${dashboardId}?page=${page}&elementPerPage=${pageSize}&direction=${sortOrder}&key=${sortby}`,
    body
  );
};
