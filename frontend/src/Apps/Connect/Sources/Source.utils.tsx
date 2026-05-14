import { ComponentIcon } from "assets/icons/boslerInterfaceIcons";
import { IBoslerBottomBarItem } from "common/components/BoslerLayout/type";
import BuildDetailsTable from "components/Builds/BuildDetailsTable.view";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import PreviewLink from "../Links/bottomBar/PreviewLink";

import { AutoModeIcon } from "assets/icons/boslerActionIcons";
import { isDefined } from "utils/utilities";
import { SourceAuthTypeEnum } from "../Enums/SourceAuthTypeEnum";
import { ISourceConfig } from "./Source";

export const isSourceConfigValid = (source: ISourceConfig) => {
  if (source.type == "jdbc") {
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
