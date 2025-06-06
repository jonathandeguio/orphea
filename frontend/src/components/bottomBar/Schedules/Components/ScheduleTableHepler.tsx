import { Resource } from "Apps/explorer/explorer";
import { Col, Dropdown, MenuProps, Row, Tooltip, Typography } from "antd";
import {
  MoreMenuIcon,
  RunIcon,
  StopIcon,
} from "assets/icons/boslerActionIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import classNames from "classnames";
import UserInfo from "common/components/UserInfo";
import BoslerLoader from "components/boslerLoader";
import cronParser from "cron-parser";
import cronstrue from "cronstrue";
import React from "react";
import { getLanguageLabel, getTimeDisplay, isDefined } from "utils/utilities";
import { JobStatusEnum } from "../SchedulesModal.constants";
import { TJobStatusEnum, TScheduleJobInfo } from "../SchedulesModal.types";
import styles from "./Schedules.module.scss";
const { Text } = Typography;

const moreDetailsItems = (row: TScheduleJobInfo, handleScheduleAction: any) => {
  const getStatusLabel = () => {
    return row.jobStatus === JobStatusEnum.PAUSED
      ? getLanguageLabel("resume")
      : getLanguageLabel("pause");
  };

  const getStatusIcon = () => {
    return row.jobStatus === JobStatusEnum.PAUSED ? <RunIcon /> : <StopIcon />;
  };

  return [
    {
      label: getStatusLabel(),
      key: "1",
      icon: getStatusIcon(),
      onClick: (e) => {
        e.domEvent.preventDefault();
        e.domEvent.stopPropagation();
        handleScheduleAction(
          row.jobId,
          row.jobStatus === JobStatusEnum.PAUSED
            ? JobStatusEnum.RUNNING
            : JobStatusEnum.PAUSED
        );
      },
    },
    {
      label: getLanguageLabel("delete"),
      key: "2",
      onClick: (e) => {
        e.domEvent.preventDefault();
        e.domEvent.stopPropagation();
        handleScheduleAction(row.jobId, JobStatusEnum.DELETED);
      },
      icon: <TrashIcon />,
    },
  ] as MenuProps["items"];
};

export const getScheduleTableColumns = (
  navigate: any,
  resourceList: any,
  handleScheduleAction: any
) => {
  return [
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("status").toUpperCase()} </Text>,
      dataIndex: "jobStatus",
      render: (e: TJobStatusEnum) =>
        e != JobStatusEnum.PAUSED && e != JobStatusEnum.DELETED ? (
          <Tooltip title={"The schedule is active."}>
            <div className="success-tick-circle text-and-icon-center">
              <TickIcon color="#ffffff" size={14}/>
            </div>
          </Tooltip>
        ) : (
          <Tooltip title={"The schedule has been paused."}>
            <StopIcon size={25} color="#FFA500" />
          </Tooltip>
        ),
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("jobID").toUpperCase()} </Text>,
      dataIndex: "jobId",
      render: (e: string) => <>{e.slice(-8)}</>,
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("cronExpression").toUpperCase()} </Text>,
      dataIndex: "triggers",
      render: (text: any, row: $TSFixMe) => {
        if (!text || text.length == 0) {
          return <div>{getLanguageLabel("notAvailable")}</div>;
        }
        let isCronValid = true;
        try {
          cronParser.parseExpression(text[0].triggerValue);
        } catch (e) {
          isCronValid = false;
        }
        // If valid, then pass into cRonstrue
        if (isCronValid) {
          return (
            <div style={{ display: "flex" }}>
              {cronstrue.toString(text[0].triggerValue)}
            </div>
          );
        }
        return <div style={{ display: "flex" }}>{text[0].triggerValue}</div>;
      },
    },

    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("triggerType").toUpperCase()} </Text>,
      dataIndex: "triggerType",
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("lastExecutionAt").toUpperCase()} </Text>,
      dataIndex: "lastExecution",
      render: (text: number, row: TScheduleJobInfo) => {
        return getTimeDisplay(text);
      },
    },
    // {
    //   title: getLanguageLabel("nextExecutionAt"),
    //   dataIndex: "lastExecution",
    //   render: (text: number, row: TScheduleJobInfo) => {
    //     if (
    //       row.triggerType == ScheduleTriggerType.CRON &&
    //       row.lastExecution
    //       // &&
    //       // row.jobStatus == JobStatusEnum.RUNNING
    //     ) {

    //     } else {
    //       return "-";
    //     }
    //   },
    // },

    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("resource").toUpperCase()} </Text>,
      dataIndex: "resourceId",
      render: (text: $TSFixMe, row: $TSFixMe) => {
        if (isDefined(resourceList) && isDefined((resourceList as any)[text])) {
          return (
            <div className="pop-over-item" onClick={() => navigate(text)}>
              {((resourceList as any)[text] as Resource).name}
            </div>
          );
        } else if (!text) {
          return <div>{getLanguageLabel("notAvailable")}</div>;
        } else return <BoslerLoader size="small" />;
      },
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("type").toUpperCase()} </Text>,
      dataIndex: "resourceType",
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("createdBy").toUpperCase()} </Text>,
      dataIndex: "createdBy",
      render: (e: $TSFixMe) => {
        return <UserInfo userId={e} />;
      },
    },
    {
      title: <Text type="secondary" strong className={classNames(styles.tableHeaderItem)} > {getLanguageLabel("createdAt").toUpperCase()} </Text>,
      dataIndex: "createdAt",
      render: (e: $TSFixMe, row: any) => {
        return getTimeDisplay(e);
        return (
          <Row justify={"space-between"} align="middle">
            <Col>{getTimeDisplay(e)}</Col>
            <Col>
              <Dropdown
                menu={{
                  items: [
                    {
                      label: (
                        <Row
                          align={"middle"}
                          onClick={() => {
                            e.stopPropagation();
                            e.preventDefault();
                            handleScheduleAction(
                              row.jobId,
                              row.jobStatus === JobStatusEnum.PAUSED
                                ? JobStatusEnum.RUNNING
                                : JobStatusEnum.PAUSED
                            );
                          }}
                        >
                          {row.jobStatus === JobStatusEnum.PAUSED ? (
                            <>
                              <RunIcon /> Resume
                            </>
                          ) : (
                            <>
                              <StopIcon /> Pause
                            </>
                          )}
                        </Row>
                      ),

                      key: 0,
                    },
                    {
                      label: (
                        <>
                          <div
                            onClick={() => {
                              e.stopPropagation();
                              e.preventDefault();
                              handleScheduleAction(
                                row.jobId,
                                JobStatusEnum.DELETED
                              );
                            }}
                            className="text-and-icon-center"
                            style={{
                              color: "var(--bosler-intent-danger)",
                            }}
                          >
                            <TrashIcon color={"var(--bosler-intent-danger)"} />
                            {getLanguageLabel("delete")}
                          </div>
                        </>
                      ),

                      key: 1,
                    },
                  ],
                }}
                trigger={["click"]}
              >
                <div
                  onClick={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                  }}
                  style={{ cursor: "pointer" }}
                >
                  <MoreMenuIcon />
                </div>
              </Dropdown>
            </Col>
          </Row>
        );
      },
    },
    {
      dataIndex: ["sparkApplicationId", "status"],
      width: 80,
      align: "center",
      render: (text: string, row: any) => {
        const items: MenuProps["items"] = moreDetailsItems(
          row,
          handleScheduleAction
        );
        return (
          <Dropdown menu={{ items }}>
            <div
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
              }}
              style={{ cursor: "pointer" }}
            >
              <MoreMenuIcon />
            </div>
          </Dropdown>
        );
      },
    },
  ];
};
