import { SourceAuthTypeEnum } from "../Enums/JDBCSourceAuthTypeEnum";
import { IRestDomain } from "./RestAPIConnector/RestAPIConnector.types";

export interface IRestAPIConnector {
  domains?: IRestDomain[];
}
export interface ISharepointConnector {
  url: string;
  clientId?: string;
  clientSecret?: string;
  tenantId?: string;
}
export interface ISourceConfig extends IRestAPIConnector, ISharepointConnector {
  agentId: any[];
  id: string;
  name: string;
  description: string;
  parent: string;
  authType: keyof typeof SourceAuthTypeEnum;
  privateKey: string;
  privateKeyPassPhrase: string;
  warehouse: string;
  schema: string;
  userRole: string;
  type: string;
  path: string;
  timeout: string;
  server: string;
  port: string;
  database: string;
  username: string;
  password: string;
  token: string;
  method: string;
  dbmsType: string;
  directLoad: boolean;
  build: boolean;

  sourceConfig: string;
}
