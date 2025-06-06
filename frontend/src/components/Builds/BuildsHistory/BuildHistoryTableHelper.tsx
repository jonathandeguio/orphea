import {
  Dropdown,
  MenuProps,
  Popover,
  TableProps,
  Tooltip,
  Typography,
} from "antd";
import {
  BuildIcon,
  MoreMenuIcon,
  StopIcon,
  SyncIcon,
  WarningIcon,
} from "assets/icons/boslerActionIcons";
import { SparkSQLIcon } from "assets/icons/boslerExternalIcons";
import { TickIcon } from "assets/icons/boslerNavigationIcon";
import classNames from "classnames";
import UserInfo from "common/components/UserInfo";
import React from "react";
import { Link } from "react-router-dom";
import {
  formatDuration,
  getLanguageLabel,
  getTimeDisplay,
  isDefined,
  timeConverter,
} from "utils/utilities";
import { ABORTED, ACTIVE, CONNECT, SUCCESS } from "../Builds.constants";
import { TBuildLog, TBuildStatus } from "../Builds.types";
import styles from "./Builds.module.scss";
import BuildTableBuilder from "./HistoryTable/BuildHistoryTableBuilder";
const { Text } = Typography;

const moreDetailsItems = (
  row: TBuildLog,
  abortBuild: any,
  onBuild: any,
  navigate: any
) => {
  const SPARK_INDEX = "sparkApplicationId";
  const STATUS_INDEX = "status";

  const getStatusLabel = () => {
    return row[STATUS_INDEX] == ACTIVE
      ? getLanguageLabel("abort")
      : getLanguageLabel("rebuild");
  };

  const getStatusIcon = () => {
    return row[STATUS_INDEX] == ACTIVE ? <StopIcon /> : <BuildIcon />;
  };

  const statusOnClick = () => {
    if (row[STATUS_INDEX] == ACTIVE) {
      abortBuild(row.id);
    } else {
      onBuild(row.trigger, row.trigger == CONNECT ? row.builder : row.id);
    }
  };

  return [
    {
      label: getLanguageLabel("spark"),
      key: "1",
      icon: <SparkSQLIcon />,
      disabled: !row[SPARK_INDEX],
      onClick: (e) => {
        e.domEvent.preventDefault();
        e.domEvent.stopPropagation();
        navigate(`/builds/${row[SPARK_INDEX].substr(-8)}`);
      },
    },
    {
      label: getStatusLabel(),
      key: "2",
      onClick: (e: any) => {
        e.domEvent.preventDefault();
        e.domEvent.stopPropagation();
        statusOnClick();
      },
      icon: getStatusIcon(),
    },
  ] as MenuProps["items"];
};

export const getBuildTableColumns = (
  abortBuild: any,
  onBuild: any,
  resourceDetailsMap: Map<string, string>,
  navigate: any
) => {
  return [
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("status").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "status",
      // width: 80,
      align: "center",
      onCell: undefined,
      render: (e: TBuildStatus, row: TBuildLog) => {
        const getStatusTooltip = () => {
          if (e === ACTIVE) {
            return "Currently build is active";
          } else if (e == SUCCESS) {
            return "Build completed successfully";
          } else if (e == ABORTED) {
            return "Build aborted";
          } else {
            return e.toString();
          }
        };
        return (
          <Tooltip title={getStatusTooltip()}>
            {e === ACTIVE ? (
              <SyncIcon size={22} color={"#08c"} spin />
            ) : e === SUCCESS ? (
              <div className="success-tick-circle text-and-icon-center">
                <TickIcon color="#ffffff" />
              </div>
            ) : e === ABORTED ? (
              <div className="text-and-icon-center">
                <StopIcon color="#FF0000" />
              </div>
            ) : (
              <div className="text-and-icon-center">
                <WarningIcon size={26} color="#FFA500" />
              </div>
            )}
          </Tooltip>
        );
      },
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("build").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "id",
      width: 120,
      align: "center",
      onCell: undefined,
      render: (e: string) => (
        <Link to={`/portal/builds/${e}`}>
          <Popover
            placement="right"
            content={<div>{getLanguageLabel("displayBuildLog")}</div>}
          >
            <div className="pop-over-item" style={{ display: "inline" }}>
              {isDefined(e) && e?.substr(-8).toLocaleUpperCase()}
            </div>
          </Popover>
        </Link>
      ),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("builder").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "builder",
      width: 180,
      align: "center",
      onCell: undefined,
      render: (text: string, row: TBuildLog) => {
        return (
          <BuildTableBuilder
            text={text}
            row={row}
            resourceDetailsMap={resourceDetailsMap}
          />
        );
      },
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("branch").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "branch",
      width: 140,
      align: "center",
      onCell: undefined,
      render: (text: string, row: TBuildLog) => {
        return <>{text}</>;
      },
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("triggerType").toUpperCase()}{" "}
        </Text>
      ),
      sorter: (a: $TSFixMe, b: $TSFixMe) => a.trigger.localeCompare(b.trigger),
      dataIndex: "trigger",
      // width: 80,
      onCell: undefined,
      align: "center",
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("startedBy").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "startedBy",
      width: 200,
      align: "center",
      onCell: undefined,
      render: (e: string, record: TBuildLog) => {
        return <UserInfo userId={e} />;
      },
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("startedAt").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "startedAt",
      // width: 80,
      align: "center",
      onCell: undefined,
      render: (text: number) => (
        <div className="BoslerSpan">{timeConverter(text)}</div>
      ),
    },
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("duration").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "duration",
      // width: 80,
      align: "center",
      onCell: undefined,
      render: (text: $TSFixMe, row: $TSFixMe) => {
        if (!row.finishedAt) {
          return (
            <div className="BoslerSpan">
              <Tooltip title={timeConverter(row.startedAt)}>
                {getTimeDisplay(row.startedAt)}
              </Tooltip>
            </div>
          );
        }
        return (
          <div className="BoslerSpan">
            {formatDuration(row.finishedAt - row.startedAt)}
          </div>
        );
      },
    },
    {
      // title: "more",
      dataIndex: ["sparkApplicationId", "status"],
      width: 80,
      align: "center",
      render: (text: string, row: any) => {
        const items: MenuProps["items"] = moreDetailsItems(
          row,
          abortBuild,
          onBuild,
          navigate
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
  ] as TableProps<TBuildLog>["columns"];
};
