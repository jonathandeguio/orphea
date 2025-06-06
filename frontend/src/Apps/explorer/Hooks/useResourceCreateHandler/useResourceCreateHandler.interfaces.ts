import { Resource } from "Apps/explorer/explorer";
import { AxiosError } from "axios";

export namespace IUseResourceCreateHandler {
  export interface IUseResourceCreateHandlerProps {}
  export interface ICreateResourceBase {
    name?: string;
    description?: string;
    parentId: string;
    moveToResource?: boolean;
    successCallback?: (resource: Resource) => void;
    failureCallback?: (error: AxiosError) => void;
  }
  export interface ICreateChartResource extends ICreateResourceBase {
    datasetId: string;
    branch?: string;
  }

  export interface IResourceCreateCommonSuccessCallback {
    data: Resource;
    moveToResource?: boolean;
    successCallback?: (resource: Resource) => void;
  }

  export interface IUseResourceCreateHandlerReturn {
    createNewFolder: (props: ICreateResourceBase) => void;
    createNewDataset: (props: ICreateResourceBase) => void;
    createNewDashboard: (props: ICreateResourceBase) => void;
    createNewChart: (props: ICreateChartResource) => void;
  }
}
