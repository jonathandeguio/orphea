import { Checkbox, Col, MenuProps, Row, Skeleton, Table, Typography } from "antd";

import React, { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  BuildIcon,
  StopIcon,
  WarningIcon,
} from "../../../assets/icons/boslerActionIcons";
import { TickIcon } from "../../../assets/icons/boslerNavigationIcon";

import { ConnectBuildAPI } from "Apps/Connect/Connect.api";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { FilterPanel } from "components/BoslerComponents/FilterPanel/FilterPanel.view";
import {
  CollapserHandler,
  ResponsivePanel,
} from "components/BoslerComponents/ResizablePane/ResizablePaneUtil";
import useInfiniteScroll from "hooks/useInfiniteScroll";
import { useSelector } from "react-redux";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { RootState } from "redux/types/store";
import {
  decodeFromBase64,
  encodeToBase64,
  getLanguageLabel,
  getSocketClient,
  isDefined,
  openNotification,
} from "utils/utilities";
import {
  abortBuildAPI,
  buildByTriggerAPI,
  fetchResourcesListAPI,
  getAllBuildsAPI,
} from "../Builds.api";
import { ABORTED, ACTIVE, CONNECT, FAILED, SUCCESS } from "../Builds.constants";
import { TBuildTrigger } from "../Builds.types";
import { IBuildFilters } from "./BuildFilters";
import { getBuildTableColumns } from "./BuildHistoryTableHelper";
import styles from "./Builds.module.scss";
import { getDefaultBuildFilters } from "./Builds.utils";
import { BuildsFilters } from "./BuildsFilters.view";

const { Title } = Typography;

const Builds = () => {
  const { user } = useSelector((state: RootState) => state.userDetails);
  const primaryPanelRef = useRef<any>(null);
  const pageSize = 20;
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const [buildsData, setbuildsData] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMoreDataToShow, setHasMoreDataToShow] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [resourceDetailsMap, setResourceDetailsMap] = useState(new Map());

  const [tableLoading, setTableLoading] = useState(false);
  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  useEffect(() => {
    document.title = "Builds";

    let favicon = document.querySelector('link[rel="icon"]') as any;
    favicon.href = "/favicons/general/buildIcon.svg";

    return () => {
      let favicon = document.querySelector('link[rel="icon"]') as any;
      favicon.href = "/favicons/general/buildIcon.svg";

      document.title =
        isDefined(config) && isDefined(config.platformName)
          ? config.platformName
          : "Bosler";
    };
  }, []);

  const addFiltersInUrl = (filters: IBuildFilters) => {
    const base64Filters = encodeToBase64(JSON.stringify(filters));
    setSearchParams({ filters: base64Filters });
  };

  const getInitialFilters = () => {
    if (searchParams.has("filters")) {
      const base64Filters = searchParams.get("filters");
      return JSON.parse(decodeFromBase64(base64Filters as string));
    }
    return getDefaultBuildFilters(user.id);
  };

  const [filters, setFilters] = useState<IBuildFilters>(getInitialFilters());

  const items: MenuProps["items"] = [
    {
      label: <>{getLanguageLabel("failed")}</>,
      key: FAILED,
      icon: (
        <>
          <Checkbox
            checked={
              isDefined(filters.status.find((val) => val === FAILED))
                ? true
                : false
            }
          />
          <WarningIcon color="#FFA500" />
        </>
      ),
    },
    {
      label: <>{getLanguageLabel("aborted")}</>,
      key: ABORTED,
      icon: (
        <>
          <Checkbox
            checked={
              isDefined(filters.status.find((val) => val === ABORTED))
                ? true
                : false
            }
          />
          <StopIcon color={"var(--bosler-intent-danger)"} />
        </>
      ),
    },
    {
      label: <>{getLanguageLabel("success")}</>,
      key: SUCCESS,
      icon: (
        <>
          <Checkbox
            checked={
              isDefined(filters.status.find((val) => val === SUCCESS))
                ? true
                : false
            }
          />
          <TickIcon color="var(--SUCCESS_COLOR)" />
        </>
      ),
    },
  ];

  const getResourceDetails = async (data: any) => {
    const resource_list: any[] | undefined = [];
    for (let i = 0; i < data.length; i++) {
      if (data[i].builder != null) {
        resource_list.push(data[i].builder);
      }
    }

    const uniqueResourceList: string[] = resource_list.filter(
      (item, index) => resource_list.indexOf(item) === index
    );

    const { data: resourceDetailsMap } = await fetchResourcesListAPI(
      uniqueResourceList
    );
    setResourceDetailsMap(new Map(Object.entries(resourceDetailsMap)));
  };

  const resurfaceBuildLogs = useCallback(async () => {
    setIsLoading(true);
    const { data } = await getAllBuildsAPI(page, pageSize, filters);
    if (data.content.length < pageSize) setHasMoreDataToShow(false);
    setPage((prev) => prev + 1);
    setbuildsData((buildLogs) => {
      const newBuildLogs = isDefined(buildLogs)
        ? buildLogs.concat(data.content)
        : [].concat(data.content);

      return newBuildLogs;
    });

    await getResourceDetails([...buildsData, ...data.content]);

    setIsLoading(false);
  }, [page, isLoading, filters]);

  const { lastElementRef } = useInfiniteScroll({
    next: resurfaceBuildLogs,
    isLoading,
    hasMore: hasMoreDataToShow,
  });

  const resetBuilds = async () => {
    setIsLoading(true);
    setPage(0);
    const { data } = await getAllBuildsAPI(0, pageSize, filters);
    if (data.content.length < pageSize) setHasMoreDataToShow(false);
    else setHasMoreDataToShow(true);
    setPage((prev) => prev + 1);
    setbuildsData(data.content);

    await getResourceDetails(data.content);

    setIsLoading(false);
  };

  const abortBuild = useCallback(
    (buildId: string) =>
      abortBuildAPI({
        buildId: buildId,
      })
        .then(() => {
          setbuildsData((buildData: any) => {
            const newBuildData = buildData.map((ele: any) => {
              return ele.id == buildId ? { ...ele, status: ABORTED } : ele;
            });
            return newBuildData;
          });
          openNotification("Build Aborted", "", "success");
        })
        .catch((error) => openNotification(error, "", "error")),
    []
  );

  const onBuild = useCallback((trigger: TBuildTrigger, buildId: string) => {
    if (trigger == CONNECT) {
      ConnectBuildAPI(buildId)
        .then(({ data }) => {
          navigate(`/portal/builds/${data.id}`);
        })
        .catch((error) => {
          openNotification(
            "Failed to Build",
            <a href={`/portal/builds`}>Click to view logs</a>,
            "error"
          );
        });
    } else {
      buildByTriggerAPI(trigger, {
        buildId: buildId,
      })
        .then(({ data }) => {
          navigate(`/portal/builds/${data.id}`);
        })
        .catch((error) => {
          openNotification(
            "Failed to Build",
            <a href={`/portal/builds`}>Click to view logs</a>,
            "error"
          );
        });
    }
  }, []);

  useEffect(() => {
    const client = getSocketClient();

    client.activate();
    client.onConnect = (frame) => {
      client.subscribe(`/topic/build/log`, function (mail) {
        const msg = JSON.parse(mail.body).message;
        if (msg == "newBuildLog") {
          const updatedBuildLog = JSON.parse(JSON.parse(mail.body).information);

          if (isDefined(updatedBuildLog))
            setbuildsData((buildData: any) => {
              if (buildData.find((ele: any) => ele.id == updatedBuildLog.id)) {
                const newBuildData = buildData.map((ele: any) => {
                  return ele.id == updatedBuildLog?.id ? updatedBuildLog : ele;
                });

                return newBuildData;
              } else {
                return [updatedBuildLog, ...buildData];
              }
            });
        }
      });
    };

    return () => {
      client.deactivate();
    };
  }, []);

  useEffect(() => {
    setTableLoading(true);
    addFiltersInUrl(filters);
    resetBuilds().then(() => setTableLoading(false));
  }, [filters]);

  return (
    <div className="--flex-col-center">
      <Row
        justify={"space-between"}
        align="middle"
        style={{
          borderBottom: "1px solid var(--bosler-border-color-default)",
          padding: "5px 20px",
          width: "100%",
        }}
      >
        <Col>
          <Title level={3}>
            <BuildIcon size={20} />
            {getLanguageLabel("buildsHistory")}
          </Title>
        </Col>
        <Col>
          <Row justify={"center"}>
            <Col>
              <BoslerButton
                onClick={() =>
                  setFilters((filters: any) => {
                    return {
                      ...filters,
                      status: [],
                    };
                  })
                }
                intent={filters.status.length == 0 ? "primary" : "none"}
              >
                {getLanguageLabel("all")}
              </BoslerButton>
            </Col>
            <Col>
              <BoslerButton
                onClick={() =>
                  setFilters((filters: any) => {
                    return { ...filters, status: [ACTIVE] };
                  })
                }
                intent={
                  filters.status.length == 1 &&
                  filters.status.find((val) => val === ACTIVE)
                    ? "primary"
                    : "none"
                }
              >
                {getLanguageLabel("buildInProgress")}
              </BoslerButton>
            </Col>
            <Col>
              <BoslerButton
                menuItems={items}
                onClickMenuItem={(e: MenuProps["onClick"] | any) => {
                  e.domEvent.stopPropagation();
                  e.domEvent.preventDefault();
                  console.log("CLICKED : ", e.key);
                  let isChecked = false;

                  filters.status.map((status: any) => {
                    if (status == e.key) isChecked = true;
                  });

                  if (!isChecked)
                    setFilters((filters: any) => {
                      return {
                        ...filters,
                        status: [...filters.status, e.key],
                      };
                    });
                  else
                    setFilters((filters: any) => {
                      const removedStatus = filters.status.filter(
                        (status: any) => status != e.key
                      );
                      return {
                        ...filters,
                        status: removedStatus,
                      };
                    });
                }}
                onClick={() => {
                  setFilters((filters: any) => {
                    return {
                      ...filters,
                      status: [FAILED, SUCCESS, ABORTED],
                    };
                  });
                }}
                intent={
                  filters.status.length <= 3 &&
                  filters.status.find(
                    (val) =>
                      val === SUCCESS || val === FAILED || val === ABORTED
                  )
                    ? "primary"
                    : "none"
                }
              >
                {getLanguageLabel("FINISHED")}
              </BoslerButton>
            </Col>
          </Row>
        </Col>
      </Row>
      <PanelGroup direction={"horizontal"}>
        <ResponsivePanel defaultSize={25} primaryPanelRef={primaryPanelRef}>
          <FilterPanel setFilters={setFilters} type={"BUILDS"}>
            <BuildsFilters filters={filters} setFilters={setFilters} />
          </FilterPanel>
        </ResponsivePanel>
        <PanelResizeHandle className="resizablePane-collapser">
          <CollapserHandler primaryPanelRef={primaryPanelRef} />
        </PanelResizeHandle>
        <Panel style={{ padding: "1rem" }}>
            <Skeleton loading={tableLoading} active avatar paragraph={{ rows: 30}}>
            <Table
              columns={getBuildTableColumns(
                abortBuild,
                onBuild,
                resourceDetailsMap,
                navigate
              )}
              size={"middle"}
              
              // sticky
              dataSource={buildsData}
              pagination={false}
              onRow={(record, rowIndex) => {
                return {
                  onClick: (event) => {
                    navigate(`/portal/builds/${record.id}`);
                  },
                  onDoubleClick: (event) => {},
                  onContextMenu: (event) => {},
                  onMouseEnter: (event) => {},
                  onMouseLeave: (event) => {},
                  // Conditionally add ref to the last row
                  ...(buildsData && rowIndex === buildsData.length - 1
                    ? { ref: lastElementRef }
                    : {}),
                };
              }}
              className={styles.buildsTable}
              {...(isLoading ? { footer: () => <Skeleton /> } : {})}
            />
            </Skeleton>
        </Panel>
      </PanelGroup>
    </div>
  );
};

export default Builds;
