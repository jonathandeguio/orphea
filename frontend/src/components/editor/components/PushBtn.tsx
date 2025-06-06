import { ArrowTopRightIcon } from "assets/icons/boslerNavigationIcon";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { useParams } from "react-router";
import { getLanguageLabel, isDefined } from "utils/utilities";

interface TProps {
  trackingStatus: any;
  pushCode: any;
}

const PushBtn = ({ trackingStatus, pushCode }: TProps) => {
  const { detached } = useParams();

  return (
    <BoslerButton
      icononly
      icon={<ArrowTopRightIcon />}
      intent={
        (trackingStatus.isRemote && trackingStatus.ahead == 0) ||
        isDefined(detached)
          ? "none"
          : "action"
      }
      disabled={
        (trackingStatus.isRemote && trackingStatus.ahead == 0) ||
        isDefined(detached)
      }
      onClick={() => pushCode()}
    >
      <span className="icon-text">{getLanguageLabel("push")}</span>
    </BoslerButton>
  );
};

export default PushBtn;
