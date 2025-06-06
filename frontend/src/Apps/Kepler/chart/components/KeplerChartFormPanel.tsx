import { CollapserHandler } from "components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import BoslerSwitch from "components/CommonUI/BoslerSwitch/BoslerSwitch";
import DatasetHistory from "components/DatasetHistory";
import VersionHistory from "components/VersionHistory";
import BoslerLoader from "components/boslerLoader";
import { DEFAULT_BRANCH } from "components/bottomBar/Schedules/SchedulesModal.constants";
import React, { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Panel, PanelResizeHandle } from "react-resizable-panels";
import { getLanguageLabel } from "utils/utilities";
import { updateQueryError } from "../../../../redux/actions/keplerActions";
import { resourceModeUpdate } from "../../../../redux/actions/resourcePermissionActions";
import {
  DATASET_HISTORY_MODE,
  EDIT_MODE,
  VERSION_MODE,
  VIEWER_MODE,
} from "../../../../redux/constants/resourcePermissionConstants";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";
import QueryForm from "../QueryForm";
import SliderController from "../QueryForm/SliderController";
import CustomizeForm from "../customizeForm";

interface TProps {
  id: string;
  datasetId: string;
}

const ChartForm = ({ resourcePermission }: any) => {
  const [selectedTab, setSelectedTab] = useState<string>("data");
  const { dataForm } = useSelector((state: RootState) => state.kepler);
  const dispatch = useDispatch();

  useEffect(() => {
    dataForm
      ?.validateFields()
      .then(() => {
        dispatch(
          updateQueryError({
            status: "FINISHED",
            error: null,
          })
        );
      })
      .catch((errorInf: any) => {
        if (errorInf.errorFields.length > 0) {
          dispatch(
            updateQueryError({
              status: "ERROR",
              error: errorInf,
            })
          );
        } else {
          dispatch(
            updateQueryError({
              status: "FINISHED",
              error: null,
            })
          );
        }
      });
  }, [dataForm]);

  return (
    <div className="--flex-col-center">
      <SliderController />
      <BoslerSwitch
        style={{
          flex: 1,
          overflow: "auto",
        }}
        items={[
          {
            label: getLanguageLabel("data"),
            value: "data",
            children: <QueryForm />,
            // icon: <HistogramIcon />,
          },
          {
            label: getLanguageLabel("customize"),
            value: "customize",
            children: <CustomizeForm />,
          },
        ]}
        value={selectedTab}
        onChange={(newVal: string) => {
          setSelectedTab(newVal);
        }}
      />
    </div>
  );
};

const KeplerChartFormPanel = ({ id, datasetId }: TProps) => {
  const primaryPanelRef = useRef<any>(null);
  const dispatch = useDispatch<ThunkAppDispatch>();
  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[id]
  );
  const datasetMapping = useSelector(
    (state) => (state as $TSFixMe).datasetMapping[datasetId]
  );

  useEffect(() => {
    if (primaryPanelRef && primaryPanelRef.current)
      if (
        resourcePermission.mode == VIEWER_MODE ||
        resourcePermission.mode == VERSION_MODE ||
        resourcePermission.mode == DATASET_HISTORY_MODE
      ) {
        try {
          primaryPanelRef.current?.collapse();
        } catch (e) {
          console.log("ERROR : ", e);
        }
      } else if (resourcePermission.mode == EDIT_MODE) {
        primaryPanelRef.current.expand();
        try {
          primaryPanelRef.current.resize(25);
        } catch (e) {}
      }
  }, [primaryPanelRef, primaryPanelRef?.current, resourcePermission]);

  useEffect(() => {
    if (
      datasetMapping &&
      datasetMapping?.datasetMapping?.currentTransaction !=
        datasetMapping?.datasetMapping?.originalCurrentTransaction &&
      resourcePermission.mode != VIEWER_MODE
    ) {
      dispatch(resourceModeUpdate(VIEWER_MODE, id));
    }
  }, [datasetMapping]);

  if (!resourcePermission || !datasetMapping) return <BoslerLoader />;

  return (
    <>
      {resourcePermission.mode == EDIT_MODE && (
        <PanelResizeHandle className="resizablePane-collapser">
          <CollapserHandler
            primaryPanelRef={primaryPanelRef}
            alignButton="left"
          />
        </PanelResizeHandle>
      )}
      <Panel
        collapsible={true}
        ref={primaryPanelRef}
        order={2}
        style={{
          transition: "flex-grow 0.3s ease",
        }}
      >
        {resourcePermission.mode == EDIT_MODE && (
          <ChartForm resourcePermission={resourcePermission} />
        )}
      </Panel>
      <div
        style={{
          borderRight: "1px solid var(--bosler-border-color-muted)",
          width:
            resourcePermission.mode == VERSION_MODE ||
            resourcePermission.mode == DATASET_HISTORY_MODE
              ? "20vw"
              : "0",
          transition: "width 0.3s ease",
        }}
      >
        {resourcePermission.mode == VERSION_MODE && (
          <VersionHistory resourceId={id} pageType="chart" />
        )}
        {resourcePermission.mode == DATASET_HISTORY_MODE && (
          <DatasetHistory
            datasetId={datasetId}
            branch={DEFAULT_BRANCH}
            datasetMapping={datasetMapping.datasetMapping}
          />
        )}
      </div>
    </>
  );
};

export default KeplerChartFormPanel;
