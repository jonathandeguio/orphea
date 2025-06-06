import { Popover } from "antd";
import { CONNECT, SYNCHRO } from "components/Builds/Builds.constants";
import { TBuildLog, TBuildTrigger } from "components/Builds/Builds.types";
import React from "react";
import { useNavigate } from "react-router";
import { getLanguageLabel } from "utils/utilities";

interface IProps {
  text: string;
  row: TBuildLog;
  resourceDetailsMap: Map<string, string>;
}

const BuildTableBuilder = ({ text, row, resourceDetailsMap }: IProps) => {
  const navigate = useNavigate();
  const getTitle = (trigger: TBuildTrigger) => {
    if (trigger == CONNECT) {
      return getLanguageLabel("LinkName");
    } else if (trigger == SYNCHRO) {
      return "Open source database";
    } else {
      return getLanguageLabel("openCodeRepository");
    }
  };

  const handleClick = (e: any, row: TBuildLog) => {
    e.preventDefault();
    e.stopPropagation();
    let link = undefined;
    if (row.trigger == CONNECT) {
      link = `/portal/connect/link/${text}`;
    } else if (row.trigger == SYNCHRO) {
      link = `/portal/connect/source/${text}`;
    } else {
      link = `/portal/kitab/repository/${text}/${row.branch}/?f=${row.scriptPath}`;
    }
    window.open(link, "_blank");
  };

  if (!text) {
    return <div>{getLanguageLabel("noStatus")}</div>;
  }

  if (!resourceDetailsMap || !resourceDetailsMap.get(text)) {
    return getLanguageLabel("notAvailable");
  }

  return (
    <Popover
      title={() => getTitle(row.trigger)}
      content={<div>{(resourceDetailsMap.get(text) as any).name}</div>}
    >
      <a onClick={(e) => handleClick(e, row)}>
        <div
          className="pop-over-item"
          style={{
            display: "inline",
            overflow: "hidden",
            textOverflow: "ellipsis",
          }}
        >
          {(resourceDetailsMap.get(text) as any).name}
        </div>
      </a>
    </Popover>
  );
};

export default BuildTableBuilder;
