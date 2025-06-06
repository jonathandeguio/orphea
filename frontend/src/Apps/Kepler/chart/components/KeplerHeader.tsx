import React from "react";
/** @jsxImportSource @emotion/react */
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { RootState, ThunkAppDispatch } from "redux/types/store";

import { Resource } from "Apps/explorer/explorer";
import {
  Badge,
  Col,
  Divider,
  MenuProps,
  Popover,
  Row,
  Typography
} from "antd";
import {
  HistoryIcon,
  LinkIcon
} from "assets/icons/boslerActionIcons";
import { GraphIcon } from "assets/icons/boslerChartIcons";
import { FolderIcon } from "assets/icons/boslerFileIcons";
import { MonitorIcon } from "assets/icons/boslerMiscellaneousIcons";
import { PopOutIcon } from "assets/icons/boslerNavigationIcon";
import { TableIcon } from "assets/icons/boslerTableIcons";
import { PlatformPagesEnum } from "common/enums";
import Avatars from "components/Avatars/Avatars";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import Comments from "components/Comments/Comments.view";
import { BoslerInfoPopover } from "components/CommonUI/BoslerInfoPopover/BoslerInfoPopover.view";
import DatasetHistoryCalenderPopover from "components/DatasetHistory/Calender/DatasetHistoryCalenderPopover";
import EmbedModal from "components/Modals/EmbedModal";
import CustomBreadCrumb from "components/Nav/Manage/breadCrumb";
import BoslerLoader from "components/boslerLoader";
import SourcesTargets from "helpers/SourcesTargets";
import { useFileExplorerService } from "hooks/useFileExplorerService";
import { useHotkeys } from "react-hotkeys-hook";
import { Link } from "react-router-dom";
import {
  getIconUrlPath,
  getLanguageLabel,
  isDefined,
  isUseCaseBasedOptionActivate,
} from "utils/utilities";
import {
  TAllowedModes,
  resourceModeUpdate,
} from "../../../../redux/actions/resourcePermissionActions";
import { changeVersionChart } from "../../../../redux/actions/versionActions";
import {
  DATASET_HISTORY_MODE,
  EDIT_MODE,
  VERSION_MODE,
  VIEWER_MODE,
  VIEWER_PERMISSION,
} from "../../../../redux/constants/resourcePermissionConstants";
import CreateNewDashboardModal from "../../utils/CreateNewDashboardModal";
import { fetchChartDashboards } from "../charts.api";
import { duplicateChartHandler } from "../charts.utils";
import KeplerHeaderActionBtn from "./KeplerHeaderActionBtn";
import KeplerHeaderDuplicate from "./KeplerHeaderDuplicate";

const { Text } = Typography;

function KeplerHeader({
  showDialog,
  datasetId,
}: {
  showDialog: boolean;
  datasetId: string;
}) {
  const { id: CHART_ID } = useParams();

  if (!isDefined(CHART_ID)) {
    throw new Error("Id is undefined, unable to load chart!");
  }

  const chart = useSelector((state: RootState) => state.kepler.chart);
  const query = useSelector((state: RootState) => state.kepler.query);
  const customize = useSelector((state: RootState) => state.kepler.customize);

  const dispatch = useDispatch<ThunkAppDispatch>();

  const [resource, setResource] = useState<Resource>();

  const [chartDashboards, setChartDashboards] = useState([]);

  const [createDashboardModal, setCreateDashboardModal] = useState(false);
  const [datasetDetails, setDatasetDetails] = useState({});

  const [showPanel, setShowPanel] = useState(false);

  const [openEmbedModal, setOpenEmbedModal] = useState<boolean>(false);

  const resourcePermission = useSelector(
    (state) => (state as $TSFixMe).resourcePermission[CHART_ID]
  );

  const { info } = useSelector((state) => (state as $TSFixMe).license);

  const toggleVersionViewMode = (): void => {
    const currentMode: boolean = resourcePermission.mode === VERSION_MODE;
    let newMode: TAllowedModes;

    if (currentMode) {
      newMode = VIEWER_MODE;
    } else {
      newMode = VERSION_MODE;
    }

    dispatch(resourceModeUpdate(newMode, CHART_ID));
  };

  useEffect(() => {
    fetchChartDashboards(CHART_ID).then(({ data }) => {
      setChartDashboards(data);
    });
  }, [createDashboardModal]);

  const { getFileIndex } = useFileExplorerService();
  useEffect(() => {
    if (chart != undefined) {
      getFileIndex(chart.id).then(({ data }) => {
        setResource(data);
      });
    }
  }, [chart]);

  useEffect(() => {
    getIconUrlPath(datasetId).then((result: any) => {
      setDatasetDetails(result);
    });
  }, []);

  useHotkeys("D", (event: any) => {
    event.preventDefault();
    duplicateChartHandler(chart, query);
  });

  useHotkeys("N", (event: any) => {
    event.preventDefault();
    setCreateDashboardModal(true);
  });

  useHotkeys("H", (event: any) => {
    event.preventDefault();
    changeVersionChart({
      versionId: undefined,
    });
    toggleVersionViewMode();
  });

  const items: MenuProps["items"] = [
    {
      key: "header",
      label: (
        <>
          <div style={{ marginLeft: "1.5rem", marginRight: "1.5rem" }}>
            {getLanguageLabel("dashboards")}
          </div>
        </>
      ),
      disabled: true,
    },
    {
      key: "divider",
      label: <Divider style={{ margin: "4px 0 10px 0" }}></Divider>,
      disabled: true,
    },
    ...chartDashboards?.map((dash: any) => ({
      label: (
        <div>
          <Link to={`/portal/kepler/DASHBOARD/${dash?.id}`}>
            <span className="text-and-icon-center">
              <MonitorIcon /> {dash?.name}
            </span>
          </Link>
        </div>
      ),
      key: `dashboard-${dash?.id}`,
    })),
  ];

  if (!isDefined(chart) && !isDefined(resource)) return <BoslerLoader />;
  return (
    <div
      className="kepler-container-header"
      onMouseEnter={() => setShowPanel(true)}
      onMouseLeave={() => setShowPanel(false)}
    >
      <CustomBreadCrumb />

      {createDashboardModal && (
        <CreateNewDashboardModal
          id={CHART_ID}
          createDashboardModal={createDashboardModal}
          setCreateDashboardModal={setCreateDashboardModal}
        />
      )}

      <div className="kepler-container-header-btns">
        <BoslerInfoPopover id={CHART_ID} type={"CHART"} />
        <Popover
          title={
            <>
              <Row justify="space-between" align="middle">
                <Col>
                  <div className="text-and-icon-center">
                    <BoslerButton
                      icon={<HistoryIcon size={20} />}
                      minimal
                      onClick={() => {
                        toggleVersionViewMode();
                      }}
                    >
                      {getLanguageLabel("version")}
                    </BoslerButton>
                  </div>
                </Col>
                <Col className="key-binding">
                  <div className="text-and-icon-center">H</div>
                </Col>
              </Row>
            </>
          }
          content={getLanguageLabel("chartChangeHistoryAndVersions")}
        >
          <BoslerButton
            icononly
            icon={<HistoryIcon size={20} />}
            minimal
            trimicononlypadding
            onClick={() => {
              toggleVersionViewMode();
            }}
          />
        </Popover>
        <Popover content={<>Embed {getLanguageLabel("dataLink")}</>}>
          <BoslerButton
            onClick={() => {
              setOpenEmbedModal(true);
            }}
            icon={<LinkIcon size={20} />}
            minimal
            icononly
            trimicononlypadding
          ></BoslerButton>
        </Popover>
        <div className="text-and-icon-center">
          <Popover
            title={
              <Link to={`/portal/bezier/${chart?.id}/master`} target="_blank">
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
                <SourcesTargets id={CHART_ID} branch={"master"} />
              </>
            }
            placement="bottom"
            overlayStyle={{ width: "20rem" }}
            // trigger={"click"}
          >
            <Link to={`/portal/bezier/${chart?.id}/master`}>
              <BoslerButton
                icon={<GraphIcon />}
                icononly={true}
                minimal
                trimicononlypadding
              ></BoslerButton>
            </Link>
          </Popover>
        </div>
        <DatasetHistoryCalenderPopover
          datasetId={datasetId}
          page={PlatformPagesEnum.KEPLER}
        />
        {datasetDetails && (
          <Popover
            content={
              <>
                <Row align="middle" style={{ marginBottom: 8 }}>
                  <BoslerButton
                    icon={<HistoryIcon />}
                    minimal
                    iconColor={(datasetDetails as any).color}
                    onClick={() => {
                      if (
                        resourcePermission &&
                        resourcePermission.mode === DATASET_HISTORY_MODE
                      ) {
                        if (
                          resourcePermission.permission === VIEWER_PERMISSION
                        ) {
                          dispatch(
                            resourceModeUpdate(VIEWER_MODE, CHART_ID as string)
                          );
                        } else {
                          dispatch(
                            resourceModeUpdate(EDIT_MODE, CHART_ID as string)
                          );
                        }
                      } else {
                        dispatch(
                          resourceModeUpdate(
                            DATASET_HISTORY_MODE,
                            CHART_ID as string
                          )
                        );
                      }
                    }}
                  >
                    {getLanguageLabel("viewHistoricalData")}
                  </BoslerButton>
                </Row>
                <Divider style={{ margin: "8px 0" }} />
                <Link
                  to={`/portal/kitab/dataset/${datasetId}/master`}
                  target="_blank"
                >
                  <Row
                    justify="space-between"
                    align="middle"
                    style={{ marginBottom: 8 }}
                  >
                    <Col>
                      <TableIcon color={(datasetDetails as any).color} />
                    </Col>
                    <Col>
                      <Text>
                        {(datasetDetails as any)?.name || "Unknown Dataset"}
                      </Text>
                    </Col>
                  </Row>
                </Link>

                <Link to={`/portal/kitab/dataset/${datasetId}/master`}>
                  <Row justify="space-between" align="middle">
                    <Col>
                      <FolderIcon size={22} />
                    </Col>
                    <Col>
                      <Text>
                        {(datasetDetails as any)?.path || "Unknown Path"}
                      </Text>
                    </Col>
                  </Row>
                </Link>
              </>
            }
            placement="bottom"
          >
            <BoslerButton
              icon={(datasetDetails as any)?.icon}
              icononly
              minimal
              trimicononlypadding
              iconColor={(datasetDetails as any).color}
            />
          </Popover>
        )}

        {isUseCaseBasedOptionActivate(
          "KEPLER",
          info.displayBlockedFeatures,
          info.product
        ) && chartDashboards?.length > 0 && (
          <Badge
            count={chartDashboards.length}
            style={{ marginRight: "4px", marginTop: "5px"}}
            size="small"
            color={"#52c41a"}
          >
            <Popover
              content={
                <div
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "5px",
                  }}
                >
                  {items.map((item: any) => (
                    <div>{item.label}</div>
                  ))}
                </div>
              }
            >
              <BoslerButton
                icon={<MonitorIcon />}
                minimal
                icononly
                trimicononlypadding
                // menuItems={items}
                // actionIcon={
                //   KEPLER_USE_CASES.includes(info.product) ? <></> : <KeyIcon />
                // }
              />
            </Popover>
          </Badge>
        )}
        <Comments id={CHART_ID} />
        <Avatars link={`/topic/${CHART_ID}`} />
        <KeplerHeaderActionBtn
          id={CHART_ID}
          showDialog={showDialog}
          chart={chart}
          query={query}
          customize={customize}
        />
        <KeplerHeaderDuplicate id={CHART_ID} chart={chart} query={query} />
      </div>
      <EmbedModal
        openEmbedModal={openEmbedModal}
        setOpenEmbedModal={setOpenEmbedModal}
      />
    </div>
  );
}

export default KeplerHeader;
