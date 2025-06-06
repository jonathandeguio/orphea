import { Popover } from "antd";
import { BuildIcon, StopIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { fetchBuildLogsAPI } from "components/Builds/Builds.api";
import React, { useEffect } from "react";
import { useHotkeys } from "react-hotkeys-hook";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";
import { getLanguageLabel, notEmpty, runPeriodically } from "utils/utilities";
import { isValidScript } from "../editor.api";
import { getFileExtension } from "../editor.utils";

interface TProps {
  trackingStatus: any;
  build: any;
  buildActive: boolean;
  setBuildActive: any;
  buildID: string;
  setBuildID: any;
  abortBuild: any;
  doesScriptHasDecorator: boolean;
}

const BuildBtn = ({
  trackingStatus,
  build,
  buildActive,
  setBuildActive,
  buildID,
  setBuildID,
  abortBuild,
  doesScriptHasDecorator,
}: TProps) => {
  const { editorPanes, activeId } = useSelector(
    (state: RootState) => state.repositoryEditor
  );
  const ON_VALID_SCRIPT =
    notEmpty(activeId) &&
    notEmpty(editorPanes[activeId]) &&
    isValidScript(editorPanes[activeId].path) &&
    (getFileExtension(editorPanes[activeId].path) == "py" ||
      getFileExtension(editorPanes[activeId].path) == "sql");

  const CONDITION =
    !ON_VALID_SCRIPT ||
    buildActive ||
    !trackingStatus.gitStatus.clean ||
    trackingStatus.ahead != 0;

  const getTitle = () => {
    if (buildID && buildActive) {
      return (
        <BoslerButton
          intent="dangerous"
          onClick={() => abortBuild(buildID)}
          icon={<StopIcon />}
          fill
        >
          {getLanguageLabel("abort")}
        </BoslerButton>
      );
    } else if (!CONDITION) {
      return "Build : " + editorPanes[activeId!].path;
    } else {
      return getLanguageLabel("selectCorrectScript");
    }
  };

  const getIconColor = () => {
    return CONDITION ? "grey" : "#fff";
  };

  const getIntent = () => {
    return CONDITION ? "none" : "action";
  };

  const getButtonState = () => {
    return CONDITION ? true : false;
  };

  const handleBuildLog = (buildID: string) => {
    fetchBuildLogsAPI(buildID).then(({ data: updatedBuildLog }) => {
      if (updatedBuildLog.status == "ACTIVE") {
        // TODO : Uncomment after, log comming for specific dataset & branch
        setBuildActive(true);
        setBuildID(updatedBuildLog.id);
      } else {
        setBuildActive(false);
        setBuildID(undefined);
      }
    });
  };

  useEffect(() => {
    let stopPeriodicFunction: (() => void) | undefined;

    if (buildID) {
      stopPeriodicFunction = runPeriodically(() => handleBuildLog(buildID));
    }

    return () => {
      if (stopPeriodicFunction) {
        stopPeriodicFunction();
      }
    };
  }, [buildID]);

  useHotkeys("ctrl+B,meta+B", (event: any) => {
    event.preventDefault();
    build();
  });

  return ON_VALID_SCRIPT ? (
    <Popover title={getTitle()} placement="bottom">
      <BoslerButton
        onClick={() => build()}
        intent={getIntent()}
        // disabled={getButtonState()}
        loading={buildActive}
        icon={<BuildIcon color={getIconColor()} />}
      >
        {getLanguageLabel("build")}
      </BoslerButton>
    </Popover>
  ) : (
    <></>
  );
};

export default BuildBtn;
