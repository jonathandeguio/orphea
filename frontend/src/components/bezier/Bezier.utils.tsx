import DataHealthMonitoring from "Apps/Dataset/DataHealth/DataHealthMonitoring";
import BoslerTable from "Apps/Dataset/Table/BoslerTable";
import DatasetSql from "Apps/Dataset/bottomBar/DatasetSql";
import { Files } from "Apps/Dataset/bottomBar/Files";
import Schema from "Apps/Dataset/bottomBar/Schema/Schema";
import { Tag } from "antd";
import { DatabaseViewIcon, TreeIcon } from "assets/icons/boslerDataIcons";
import { CodeCellIcon } from "assets/icons/boslerEditorIcons";
import { SparkSQLIcon } from "assets/icons/boslerExternalIcons";
import { DocsIcon } from "assets/icons/boslerFileIcons";
import { PulseIcon } from "assets/icons/boslerMiscellaneousIcons";
import { IBoslerBottomBarItem } from "common/components/BoslerLayout/type";
import { ReadOnlyCodePanel } from "components/bottomBar/ReadOnlyCodePanel/ReadOnlyCodePanel.view";
import DatasetSync from "components/bottomBar/sync/Sync.view";
import React from "react";
import { NULL_UUID } from "utils/Common.constants";
import { getLanguageLabel, isDefined } from "utils/utilities";

export const getBezierBottomBarItems = (
  id: string,
  branch: string,
  type?: string,
  subType?: string,
  transactionId?: string
): IBoslerBottomBarItem[] => {
  console.log("subType", subType);
  return [
    {
      id: "bezierSqlEditor",
      icon: <SparkSQLIcon />,
      label: getLanguageLabel("query"),
      body: DatasetSql,
      type: "TAB",
      intent: type === "dataset" ? "PRIMARY" : "DISABLED",
      props: {
        id,
        branch,
        transactionId,
      },
    },
    {
      id: "bezierDatasetPanel",
      icon: <DocsIcon />,
      label: getLanguageLabel("dataset"),
      body: BoslerTable,
      intent: type === "dataset" ? "PRIMARY" : "DISABLED",
      type: "TAB",
      props: {
        id,
        branch,
        isTableFromBottomBar: true,
      },
    },
    {
      id: "bezierCodePanel",
      icon: <CodeCellIcon />,
      label: getLanguageLabel("code"),
      body: ReadOnlyCodePanel,
      /**
       * WE also need a check for subType of python & sql then only activate this.
       */
      intent: type === "dataset" ? "PRIMARY" : "DISABLED",
      type: "TAB",
      props: {
        id,
        branch,
        transactionId,
      },
    },
    {
      id: "bezierFilePanel",
      icon: <DocsIcon />,
      label: getLanguageLabel("files"),
      body: Files,
      intent: isDefined(type) && type === "dataset" ? "PRIMARY" : "DISABLED",
      type: "TAB",
      props: {
        id,
        branch,
        transactionId: NULL_UUID,
      },
    },
    {
      id: "bezierSyncPanel",
      icon: <DatabaseViewIcon />,
      label: getLanguageLabel("sync"),
      body: DatasetSync,
      intent: isDefined(type) && type === "dataset" ? "PRIMARY" : "DISABLED",
      type: "TAB",
      props: {
        datasetId: id,
        branch,
      },
    },
    {
      id: "bezierSchemaPanel",
      icon: <TreeIcon />,
      label: getLanguageLabel("schema"),
      body: Schema,
      intent: isDefined(type) && type === "dataset" ? "PRIMARY" : "DISABLED",
      type: "TAB",
      props: { id, branch, isBuildDataset: true, transactionId: NULL_UUID },
    },
    {
      id: "dataHealthPanel",
      icon: <PulseIcon />,
      intent: type === "dataset" ? "PRIMARY" : "DISABLED",
      label: (
        <>
          Data Health &nbsp;<Tag color="blue">Beta</Tag>
        </>
      ),
      body: DataHealthMonitoring,
      type: "TAB",
      // props: { id, branch, isBuildDataset },
    },
  ];
};
