import { Typography } from "antd";

import React from "react";

import { getLanguageLabel } from "utils/utilities";

const { Title, Text } = Typography;

const Learn = () => {
  return (
    <div className="settings-center-block">
      <Title level={3}>{getLanguageLabel("learn")}</Title>
    </div>
  );
};

export default Learn;
