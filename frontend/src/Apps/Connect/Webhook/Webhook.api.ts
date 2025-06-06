import axios, { AxiosResponse } from "axios";
import { IWebhook } from "./Webhook.types";

export const createWebhookAPI = (
  payload: IWebhook
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/webhook/create`, payload);
};

export const updateWebhookAPI = (
  payload: IWebhook
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/webhook/update`, payload);
};

export const getWebhookAPI = (id: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/webhook/${id}`);
};

export const executeWebhookAPI = (
  id: string,
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/connect/webhook/execute/${id}`, body);
};

export const webhookExecutionResulsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/connect/webhook/executions/${id}`);
};
