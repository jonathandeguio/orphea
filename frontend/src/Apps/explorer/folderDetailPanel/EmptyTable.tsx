import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import NoData from "components/CommonUI/NoData";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

interface Props {
  onContextMenu?: (e: React.MouseEvent<HTMLDivElement>, node: any) => void;
}

export const EmptyTable: React.FC<Props> = ({ onContextMenu }) => {
  return (
    <div
      style={{ height: "calc(100% - 3rem)", width: "100%" }}
      onContextMenu={(e: React.MouseEvent<HTMLDivElement>) => {
        e.preventDefault();
        onContextMenu?.(e, "");
      }}
    >
      <NoData
        heading={getLanguageLabel("folderIsEmpty")}
        subHeading={getLanguageLabel("addNewResourceNote")}
        icon={<SearchEmptyState />}
      />
    </div>
  );
};
