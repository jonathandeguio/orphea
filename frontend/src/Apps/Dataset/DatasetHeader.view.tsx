import { Popover, Typography } from "antd";
import { HistoryIcon } from "assets/icons/boslerActionIcons";

import { GraphIcon, GroupedColumnIcon } from "assets/icons/boslerChartIcons";
import { PopOutIcon } from "assets/icons/boslerNavigationIcon";
import Avatars from "components/Avatars/Avatars";

import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import Comments from "components/Comments/Comments.view";
import CustomBreadCrumb from "components/Nav/Manage/breadCrumb";
import SourcesTargets from "helpers/SourcesTargets";
import React from "react";
import { Link } from "react-router-dom";

import { KEPLER_USE_CASES } from "Apps/Kepler/chart/charts.utils";
import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import { KeyIcon } from "assets/icons/boslerInterfaceIcons";
import { BoslerInfoPopover } from "components/CommonUI/BoslerInfoPopover/BoslerInfoPopover.view";
import { useDispatch, useSelector } from "react-redux";
import {
  getLanguageLabel,
  isUseCaseBasedOptionActivate,
} from "utils/utilities";
import { resourceModeUpdate } from "../../redux/actions/resourcePermissionActions";
import {
  DATASET_HISTORY_MODE,
  EDIT_MODE,
  VIEWER_MODE,
  VIEWER_PERMISSION,
} from "../../redux/constants/resourcePermissionConstants";
import { ThunkAppDispatch } from "../../redux/types/store";
import BuildBtn from "./Builds/BuildBtn";
import { IDatasetDetails } from "./DatasetDetail";
import Tags from "./Tags/Tags.view";
import { useCreateResourceHandler } from "Apps/explorer/Hooks/useResourceCreateHandler/useResourceCreateHandler";
import { useFileExplorerService } from "hooks/useFileExplorerService";

const { Text, Title } = Typography;

interface TProps {
  id: string;
  branch: string;
  transactionId?: string;
  datasetDetails: IDatasetDetails;
}

const DatasetHeader = ({
  id,
  branch,
  transactionId,
  datasetDetails,
}: TProps) => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { createNewChart } = useCreateResourceHandler();
  const { getFileIndex } = useFileExplorerService();
  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[id]
  );

  const { info } = useSelector((state) => (state as $TSFixMe).license);

  return (
    <div className="dataset-splitpane-header">
      <CustomBreadCrumb />
      <div className="dataset-splitpane-header-btns">
        <BoslerInfoPopover
          id={id}
          branch={branch}
          transactionId={transactionId}
          type={datasetDetails.type}
        />
        {transactionId && (
          <Popover
            title={
              <>
                <div className="text-and-icon-center">Dataset History</div>
              </>
            }
            content={
              "View the historical data based on different transactions."
            }
          >
            <BoslerButton
              icononly
              icon={<HistoryIcon size={20} />}
              minimal
              trimicononlypadding
              onClick={() => {
                if (
                  resourcePermission &&
                  resourcePermission.mode == DATASET_HISTORY_MODE
                ) {
                  if (resourcePermission.permission == VIEWER_PERMISSION) {
                    dispatch(resourceModeUpdate(VIEWER_MODE, id as string));
                  } else {
                    dispatch(resourceModeUpdate(EDIT_MODE, id as string));
                  }
                } else {
                  dispatch(
                    resourceModeUpdate(DATASET_HISTORY_MODE, id as string)
                  );
                }
              }}
            />
          </Popover>
        )}

        <Tags id={datasetDetails.id} iconOnly={true} />

        <Popover
          title={
            <Link to={`/portal/bezier/${datasetDetails.id}/master`}>
              <div
                className="text-and-icon-center"
                style={{
                  justifyContent: "space-between",
                  width: "100%",
                  color: "var(--bosler-font-color-muted)",
                }}
              >
                {getLanguageLabel("dataLineage")}
                <PopOutIcon />
              </div>
            </Link>
          }
          content={
            <>
              <SourcesTargets id={datasetDetails.id} branch={"master"} />
            </>
          }
          placement="bottom"
          overlayStyle={{ width: "20rem" }}
          // trigger={"click"}
        >
          <Link to={`/portal/bezier/${datasetDetails.id}/master`}>
            <BoslerButton
              icon={<GraphIcon />}
              icononly={true}
              minimal
              trimicononlypadding
            ></BoslerButton>
          </Link>
        </Popover>
        <Comments id={datasetDetails.id} />
        <Avatars link={`/topic/${datasetDetails.id}`} />

        {isUseCaseBasedOptionActivate(
          "KEPLER",
          info.displayBlockedFeatures,
          info.product
        ) &&
          transactionId && (
            <Popover
              placement="bottom"
              content={getLanguageLabel("createNewChart")}
            >
              <BoslerButton
                icon={<GroupedColumnIcon />}
                intent="action"
                onClick={() => {
                  getFileIndex(id).then((data) => {
                    if (data.parent)
                      createNewChart({
                        parentId: data.parent,
                        datasetId: id,
                        branch,
                      });
                  });
                }}
                actionIcon={
                  KEPLER_USE_CASES.includes(info.product) ? <></> : <KeyIcon />
                }
              >
                {getLanguageLabel("chart")}
              </BoslerButton>
            </Popover>
          )}
        {datasetDetails.subType == ResourceSubTypeEnum.BUILDDATASET &&
          transactionId && (
            <BuildBtn
              datasetId={id}
              branch={branch}
              currentTransactionId={transactionId}
              page="DATASET"
            />
          )}
      </div>
    </div>
  );
};

export default DatasetHeader;
