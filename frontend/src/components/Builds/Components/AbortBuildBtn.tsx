import { StopIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { abortBuildAPI } from "../Builds.api";

interface IProps {
  buildId: string;
}

const AbortBuildBtn = ({ buildId }: IProps) => {
  const onAbort = (buildId: string) => {
    abortBuildAPI({
      buildId: buildId,
    })
      .then(() => {
        (window as any).makeButtonTemporarySuccess("abort-btn", 5000);
      })
      .catch(() => {
        (window as any).makeButtonTemporaryFailure("abort-btn", 5000);
      });
  };
  return (
    <BoslerButton
      intent="dangerous"
      onClick={() => onAbort(buildId)}
      icon={<StopIcon />}
      fill
      id="abort-btn"
    >
      {getLanguageLabel("abort")}
    </BoslerButton>
  );
};

export default AbortBuildBtn;
