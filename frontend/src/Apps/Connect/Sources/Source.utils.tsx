import { ComponentIcon } from "assets/icons/boslerInterfaceIcons";
import { IBoslerBottomBarItem } from "common/components/BoslerLayout/type";
import BuildDetailsTable from "components/Builds/BuildDetailsTable.view";
import React from "react";
import { getLanguageLabel, isEmpty } from "utils/utilities";
import PreviewLink from "../Links/bottomBar/PreviewLink";

import { AutoModeIcon } from "assets/icons/boslerActionIcons";
import { isDefined } from "utils/utilities";
import { SourceAuthTypeEnum } from "../Enums/JDBCSourceAuthTypeEnum";
import { ISourceConfig } from "./Source";

export const isSourceConfigValid = (source: ISourceConfig) => {
  if (source.type == "jdbc") {
    if (source.authType == SourceAuthTypeEnum.KEYPAIR) {
      return (
        isDefined(source.name) &&
        isDefined(source.parent) &&
        isDefined(source.warehouse) &&
        isDefined(source.username) &&
        isDefined(source.privateKey) &&
        isDefined(source.server) &&
        isDefined(source.port) &&
        isDefined(source.database)
      );
    } else {
      return (
        isDefined(source.name) &&
        isDefined(source.parent) &&
        isDefined(source.username) &&
        isDefined(source.password) &&
        isDefined(source.server) &&
        isDefined(source.port) &&
        isDefined(source.database)
      );
    }
  } else if (source.type == "rest") {
    return (
      isDefined(source.name) &&
      !isEmpty(source.name) &&
      isDefined(source.parent) &&
      !isEmpty(source.parent) &&
      isDefined(source.domains) &&
      !isEmpty(source.domains)
    );
  } else if (source.type === "FOLDER") {
    return isDefined(source.path);
  } else if (source.type === "SHAREPOINT") {
    return (
      isDefined(source.url) &&
      source.url != "" &&
      isDefined(source.tenantId) &&
      source.tenantId != "" &&
      isDefined(source.clientId) &&
      source.clientId != "" &&
      isDefined(source.clientSecret) &&
      source.clientSecret != ""
    );
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
