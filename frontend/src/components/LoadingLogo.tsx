import React from "react";
import { getLanguageLabel, isIpPlatform } from "utils/utilities";
import BoslerLoader from "./boslerLoader";

const LoadingLogo = () => {
  return (
    <div className="login-container login-container-logo">
      <div className="login-icon">
        {!isIpPlatform() && (
          <BoslerLoader content={getLanguageLabel("loading...")} />
        )}
      </div>
    </div>
  );
};

export default LoadingLogo;
