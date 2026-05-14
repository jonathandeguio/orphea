import { SourceAuthTypeEnum } from "../Enums/SourceAuthTypeEnum";

export interface ISourceConfig {
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
  url: string;
  method: string;
  dbmsType: string;
  directLoad: boolean;
  build: boolean;
}
