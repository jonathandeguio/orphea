import { ResourceType } from "Apps/explorer/explorer.utils";
import axios, { AxiosResponse } from "axios";
import { openNotification } from "utils/utilities";
import {
  TJobStatusEnum,
  TScheduleFilters,
  TScheduleJobInfo,
} from "./SchedulesModal.types";

export const getDataset = async (id: string) => {
  try {
    const { data: datasetData } = await axios.get(`/kitab/${id}`);
    return datasetData;
  } catch (error) {
    openNotification(`Failed to fetch name`, " ", "error");
  }
};

export const getSchedulesAPI = async (
  resourceId: string,
  branch: string,
  resourceType: ResourceType
) => {
  try {
    const { data: job } = await axios.get(
      `/scheduler/getJob/${resourceId}/${branch}/${resourceType}`
    );
    return job;
  } catch (error) {
    openNotification(`Failed to fetch schedules`, " ", "error");
  }
};

export const putScheduleAPI = async (payload: TScheduleJobInfo) => {
  try {
    const { data: schedule } = await axios.put(`/scheduler/schedule`, payload);

    return schedule;
  } catch (error) {
    openNotification(`Failed to put schedule`, " ", "error");
  }
};

export const getSourcesAPI = async (id: string, branch: string) => {
  try {
    const { data } = await axios.get(`/bezier/${id}/${branch}/getParents`);

    return data.nodes;
  } catch (error) {
    openNotification(`Failed to get sources`, " ", "error");
  }
};

export const getDatasetsDetailsByIdAPI = async (datasetIds: any) => {
  try {
    const { data } = await axios.post(`/kitab/dataset/byIds`, datasetIds);

    return data;
  } catch (error) {
    openNotification(`Failed to get datasets details`, " ", "error");
  }
};

export const deleteSchedulesByResourceIdAPI = async (resourceId: string) => {
  try {
    const { data } = await axios.delete(`/scheduler/${resourceId}`);
    return data;
  } catch (error) {
    openNotification(`Failed to delete schedules`, " ", "error");
  }
};

export const actionScheduleAPI = async (
  jobId: string,
  action: TJobStatusEnum
) => {
  try {
    const { data } = await axios.post(`/scheduler/${jobId}/${action}`);

    return data;
  } catch (error) {
    openNotification(
      "Error Performing " + action + " on Schedule",
      " ",
      "error"
    );
    return {};
  }
};

export const getAllSchedulesAPI = (
  page: number,
  pageSize: number,
  filters: TScheduleFilters
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/scheduler/getAll?page=${page}&elementPerPage=${pageSize}&direction=dsc&key=createdAt`,
    filters
  );
};

export const getScheduleLogsAPI = (
  page: number,
  pageSize: number,
  jobId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/scheduler/logs/${jobId}?page=${page}&elementPerPage=${pageSize}&direction=dsc&key=startedAt`
  );
};

export const getScheduleAPI = (
  jobId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/scheduler/${jobId}`);
};
