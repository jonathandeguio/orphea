import { IRestDomain, TRestAuthTypeEnum } from "./RestAPIConnector.types";

export const initialDomain: IRestDomain = {
  protocol: "https",
  domain: undefined,
  authType: TRestAuthTypeEnum.NONE,
  port: 443,
  apiTitle: "",
};
