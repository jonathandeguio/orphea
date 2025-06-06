import { Tag } from "antd";
import { CardIcon } from "assets/icons/boslerMiscellaneousIcons";
import BoslerHeader from "components/CommonUI/Header/BoslerHeader";
import React from "react";
import DataHealthAddChecks from "./DataHealthAddChecks";
import DataHealthConfiguredChecks from "./DataHealthConfiguredChecks";

const DataHealthMonitoring = () => {
  return (
    <div>
      <BoslerHeader
        icon={<CardIcon />}
        heading={<>Data Health &nbsp;<Tag color="blue">Beta</Tag></>}
        description="Data Health Monitoring"
        borderBottom
      />
      <div className="--p10">
        <DataHealthConfiguredChecks />
        <DataHealthAddChecks />
      </div>
    </div>
  );
};

export default DataHealthMonitoring;
