import {
  createChartApi,
  createDashboardApi,
  createDatasetApi,
  createFolderApi,
} from "Apps/explorer/explorer.api";
import { DEFAULT_RESOURCE_NAMES } from "Apps/explorer/explorer.constants";
import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { ResourceTypeEnum } from "Apps/explorer/explorer.utils";
import { useDispatch } from "react-redux";
import {
  appendUniqueKey,
  generateUUID,
  openNotification,
} from "utils/utilities";
import { addNewResource } from "../../../../redux/fileIndexSlice";
import { DEFAULT_BRANCH } from "./useResourceCreateHandler.constants";
import { IUseResourceCreateHandler } from "./useResourceCreateHandler.interfaces";

/** refactor - In future we should have single api for a resource creation.
 * rn we can different api calls but logic in single place.
 */
export const useCreateResourceHandler =
  (): IUseResourceCreateHandler.IUseResourceCreateHandlerReturn => {
    const dispatch = useDispatch();

    const navigator = useNavigateHelper();

    const resourceCreateCommonSuccessCallback = ({
      data,
      moveToResource,
      successCallback,
    }: IUseResourceCreateHandler.IResourceCreateCommonSuccessCallback) => {
      if (data?.id) {
        dispatch(addNewResource(data));
        if (moveToResource) navigator(data?.id);
      }
      if (successCallback) successCallback(data);
    };

    const createNewFolder = ({
      name,
      description,
      parentId,
      moveToResource = true,
      successCallback,
      failureCallback,
    }: IUseResourceCreateHandler.ICreateResourceBase) => {
      createFolderApi({
        name: name || appendUniqueKey(DEFAULT_RESOURCE_NAMES.FOLDER),
        description,
        parent: parentId,
        type: ResourceTypeEnum.FOLDER,
      })
        .then(({ data }) =>
          resourceCreateCommonSuccessCallback({
            data,
            moveToResource,
            successCallback,
          })
        )
        .catch((error) => {
          openNotification("Failed to create Folder", "", "error");
          if (failureCallback) failureCallback(error);
        });
    };

    const createNewDataset = ({
      name,
      description,
      parentId,
      moveToResource = true,
      successCallback,
      failureCallback,
    }: IUseResourceCreateHandler.ICreateResourceBase) => {
      createDatasetApi({
        name: name || appendUniqueKey(DEFAULT_RESOURCE_NAMES.DATASET),
        description,
        parent: parentId,
        type: ResourceTypeEnum.DATASET,
      })
        .then(({ data }) =>
          resourceCreateCommonSuccessCallback({
            data,
            moveToResource,
            successCallback,
          })
        )
        .catch((error) => {
          openNotification("Failed to create Dataset", "", "error");
          if (failureCallback) failureCallback(error);
        });
    };

    const createNewDashboard = ({
      name,
      description,
      parentId,
      moveToResource = true,
      successCallback,
      failureCallback,
    }: IUseResourceCreateHandler.ICreateResourceBase) => {
      createDashboardApi({
        name: name || appendUniqueKey(DEFAULT_RESOURCE_NAMES.DASHBOARD),
        description,
        parent: parentId,
      })
        .then(({ data }) =>
          resourceCreateCommonSuccessCallback({
            data,
            moveToResource,
            successCallback,
          })
        )
        .catch((error) => {
          openNotification("Failed to create Dashboard", "", "error");
          if (failureCallback) failureCallback(error);
        });
    };

    const getDefaultChartConfigWithDatasetId = (
      datasetId: string,
      branch: string
    ) => {
      return {
        datasetId: datasetId,
        branch: branch,
        chartType: "VerticalAxisChart",
        rowLimit: 50000,
        filter: [],
        series: [
          {
            id: generateUUID(),
            seriesName: "Series",
            columnName: undefined,
            sort: "asc",
            seriesIndex: "left",
            seriesType: "barChart",
            reversed: false,
          },
        ],
        xaxis: undefined,
        xaxisSort: "asc",
        xaxisTimeGrain: "date",
      };
    };

    const createNewChart = ({
      name,
      description,
      parentId,
      datasetId,
      moveToResource = true,
      branch = DEFAULT_BRANCH,
      successCallback,
      failureCallback,
    }: IUseResourceCreateHandler.ICreateChartResource) => {
      createChartApi({
        name: name || appendUniqueKey(DEFAULT_RESOURCE_NAMES.CHART),
        description,
        parent: parentId,
        chartConfig: getDefaultChartConfigWithDatasetId(datasetId, branch),
        datasetId,
        branch,
      })
        .then(({ data }) =>
          resourceCreateCommonSuccessCallback({
            data,
            moveToResource,
            successCallback,
          })
        )
        .catch((error) => {
          openNotification("Failed to create Chart", "", "error");
          if (failureCallback) failureCallback(error);
        });
    };

    return {
      createNewFolder,
      createNewDataset,
      createNewDashboard,
      createNewChart,
    };
  };
