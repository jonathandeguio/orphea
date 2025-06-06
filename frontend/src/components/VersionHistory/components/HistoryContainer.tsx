import React from "react";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import EditRow from "./EditRow";
import SaveRow from "./SaveRow";

interface TProps {
  versions: any[];
  onlyVersions?: boolean;
  pageType: "dashboard" | "chart";
  resourceId: string;
}

const HistoryContainer = ({
  versions,
  onlyVersions = false,
  pageType,
  resourceId,
}: TProps) => {
  const { changedVersionChart, changedVersionDash } = useSelector(
    (state: RootState) => state.version
  );

  const isSelected = (currentVersionId: any) => {
    if (pageType == "chart") {
      return changedVersionChart == currentVersionId;
    } else if (pageType == "dashboard") {
      return changedVersionDash == currentVersionId;
    }
    return false;
  };

  if (onlyVersions) {
    return (
      <>
        {versions.map((version) => (
          <SaveRow
            version={version}
            pageType={pageType}
            resourceId={resourceId}
            isSelected={isSelected(version.versionId)}
          />
        ))}
      </>
    );
  } else {
    return (
      <>
        {versions.map((version) => (
          <EditRow
            version={version}
            pageType={pageType}
            resourceId={resourceId}
            isSelected={isSelected(version.versionId)}
          />
        ))}
      </>
    );
  }
};

export default HistoryContainer;
