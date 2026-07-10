import { Typography } from "antd";
import React from "react";
import { BTH1 } from "components/MoveToDataTypography";

const { Title } = Typography;

const Typhography = () => {
  return (
    <>
      <Title>Typhography</Title>
      <Title level={3}>Heading one</Title>
      <BTH1 />
    </>
  );
};

export default Typhography;
