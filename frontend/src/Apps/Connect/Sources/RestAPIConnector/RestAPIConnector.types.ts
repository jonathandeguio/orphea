export enum TRestAuthTypeEnum {
  NONE = "NONE",
  APIKEY = "APIKEY",
  BEARERTOKEN = "BEARERTOKEN",
}
export enum TAPITestEnum {
  PLAIN_RESPONSE = "Response",
  PARSED_JSON = "Parsed JSON",
  INVALID_JSON = "Invalid JSON",
}
export interface IRestDomain {
  id?: string | undefined;
  protocol: "https" | "http";
  apiTitle: string;
  domain: string | undefined;
  authType: TRestAuthTypeEnum;
  bearerToken?: string;
  apiKeyName?: string;
  apiKeyValue?: string;
  port: number;
}

export interface IWebhookCallData {
  id: string;
  url: string;
  fullUrl: string;
  method: string;
  apiTitle:string;
  extraErrors: string;
  requestHeaders: string;
  responseHeaders: string;
  requestBody: string;
  responseBody: string;
  status: string;
  key: string;
}

export type ParsedValue = string | number | boolean | string[];
