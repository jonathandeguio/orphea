import { ComponentIcon } from "assets/icons/boslerInterfaceIcons";
import { IBoslerBottomBarItem } from "common/components/BoslerLayout/type";
import BuildDetailsTable from "components/Builds/BuildDetailsTable.view";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import PreviewLink from "../Links/bottomBar/PreviewLink";

import { AutoModeIcon } from "assets/icons/boslerActionIcons";
import { isDefined } from "utils/utilities";
import { SourceAuthTypeEnum } from "../Enums/SourceAuthTypeEnum";
import { SourceTypeEnum } from "../Enums/SourceTypeEnum";
import { ISourceConfig } from "./Source";

export const isSourceConfigValid = (source: ISourceConfig) => {
  if (source.type == "jdbc") {
    // ODBC Bridge: requires name, parent, and at minimum a DSN name (server field)
    // or a full connection string (database field). Credentials are optional for some ODBC sources.
    if (source.dbmsType === SourceTypeEnum.ODBC) {
      return (
        source.name &&
        source.parent &&
        (source.server || source.database)
      );
    }
    // Databricks: host + httpPath (stored in schema) + PAT (stored in password). No port or username required.
    if (source.dbmsType === SourceTypeEnum.DATABRICKS) {
      return (
        source.name &&
        source.parent &&
        source.server &&
        source.schema &&
        source.password
      );
    }
    // MongoDB: host + port + database are mandatory. Username/password are optional (some instances use no auth).
    if (source.dbmsType === SourceTypeEnum.MONGODB) {
      return (
        source.name &&
        source.parent &&
        source.server &&
        source.port &&
        source.database
      );
    }
    if (source.authType == SourceAuthTypeEnum.KEYPAIR) {
      return (
        source.name &&
        source.parent &&
        source.warehouse &&
        source.username &&
        source.privateKey &&
        source.server &&
        source.port &&
        source.database
      );
    } else {
      return (
        source.name &&
        source.parent &&
        source.username &&
        source.password &&
        source.server &&
        source.port &&
        source.database
      );
    }
  } else if (source.type === "FOLDER") {
    return isDefined(source.path);
  }
};

export const getSourceBottombarItems = (
  source: any
): IBoslerBottomBarItem[] => {
  return [
    {
      id: "datasetBuildLogPanel",
      icon: <ComponentIcon />,
      label: getLanguageLabel("buildLog"),
      body: BuildDetailsTable,
      type: "TAB",
      props: {
        showHeader: false,
        page: "SOURCE",
        showEmpty: true,
        data: [],
      },
    },
    ...(source.type == "jdbc" && source.directLoad
      ? [
          {
            id: "datasetPreviewPanel",
            icon: <AutoModeIcon />,
            label: getLanguageLabel("preview"),
            body: PreviewLink,
            type: "TAB",
            props: {
              id: source.id,
              showHeader: false,
            },
          },
        ]
      : ([] as any)),
  ];
};
