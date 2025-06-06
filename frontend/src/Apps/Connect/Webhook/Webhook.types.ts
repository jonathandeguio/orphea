import { IWebhookCallData } from "../Sources/RestAPIConnector/RestAPIConnector.types";

export enum RestAPIBodyTypeEnum {
  NONE = "NONE",
  FORMDATA = "FORMDATA",
  RAW = "RAW",
  JSON = "JSON",
}

export enum RestAPIMethodTypeEnum {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
  DELETE = "DELETE",
}
export interface IWebhookRequest {
  id?: string;
  domainId: string;
  path: string;
  bodyType: RestAPIBodyTypeEnum.NONE;
  queryParams: string | [];
  headers: string | [];
  formData: string | [];
  rawBody: "";
  method: RestAPIMethodTypeEnum.GET;
}

export interface IWebhook {
  id?: string;
  name: string;
  parent: string | undefined;
  description?: string;
  sourceId: string | undefined;
  requests?: IWebhookRequest[];
}

export interface IWebhookExecutionResult {
  id: string;
  webhookId: string;
  executedAt: number;
  executedBy: string;
  calls: IWebhookCallData[];
}
