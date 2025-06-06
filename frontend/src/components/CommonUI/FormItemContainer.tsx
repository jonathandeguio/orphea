import { Col, Row, Tooltip } from "antd";
import { InfoIcon } from "assets/icons/boslerMiscellaneousIcons";
import React, { ReactNode, useState } from "react";
import { BoslerTypography } from "./BoslerTypography";

interface IProps {
  children?: ReactNode;
  label?: string;
  info?: ReactNode;
}

const FormItemContainer = ({ children, info, label }: IProps) => {
  const [isHovered, setIsHovered] = useState<boolean>(false);
  const handleMouseEnter = () => {
    setIsHovered(true);
  };
  const handleMouseLeave = () => {
    setIsHovered(false);
  };

  return (
    <Row
      justify={"space-between"}
      align="middle"
      gutter={[16, 16]}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      style={{ margin: 0 }}
    >
      <Col span={8} style={{ paddingBottom: "16px" }}>
        {label && <BoslerTypography>{label}</BoslerTypography>}
        {info && isHovered && (
          <Tooltip title={info}>
            <InfoIcon />
          </Tooltip>
        )}
      </Col>
      <Col span={16}>{children}</Col>
    </Row>
  );
};

export default FormItemContainer;
