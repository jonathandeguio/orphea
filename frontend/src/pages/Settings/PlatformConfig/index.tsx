import { Typography } from "antd";
import React from "react";

const { Title, Text } = Typography;

const PlatformSettings = () => {
  return (
    <div className="settings-center-block">
      <p>
        <>
          <Title level={4}>Platform Settings</Title>
          <Text>
            Welcome to the central hub for platform settings, where
            configurations wield influence over the entire platform. These
            settings bear significance solely for the utilization of platform
            administrators. Among these influential configurations are
            parameters governing aspects such as cache management, dataset
            history tracking, SMTP configuration, and more. Administering these
            settings demands a strategic understanding of their impact on the
            broader platform ecosystem.
          </Text>
        </>
      </p>
    </div>
  );
};
export default PlatformSettings;
