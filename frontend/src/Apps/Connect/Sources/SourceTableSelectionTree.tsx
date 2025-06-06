import { ResourceSubTypeEnum } from "Apps/explorer/explorer.utils";
import React from "react";
import DatabaseTreeViewer from "../DatabaseTreeViewer";

interface IProps {
  sourceId: string;
  onTableClick: (node: any, parent: any) => void;
}
const SourceTableSelectionTree = ({ sourceId, onTableClick }: IProps) => {
  const onSingleClick = (node: any, parent: any) => {
    if (node.subType == ResourceSubTypeEnum.TABLE_CHART) {
      onTableClick(node, parent);
    }
  };

  return (
    <div
      style={{
        flex: 1,
        maxHeight: "40vh",
        overflow: "auto",
      }}
    >
      <DatabaseTreeViewer
        sourceId={sourceId}
        page="LINK_SOURCE_SELECTION"
        onSingleClick={onSingleClick}
      />
    </div>
  );
};

export default SourceTableSelectionTree;
