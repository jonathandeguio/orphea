import { Card, CardProps, Col, Row, Typography } from "antd";
import { MoreMenuIcon } from "assets/icons/boslerActionIcons";
import React from "react";

interface TBoslerCardProps extends CardProps {
  heading?: any;
  icon?: any;
  information?: any;
  disabled?: boolean;
}
const { Title } = Typography;
const { Meta } = Card;
const BoslerSelectableCard = ({
  heading,
  icon,
  information,
  disabled,
}: TBoslerCardProps) => {
  return (
    <Card
      hoverable
      className={
        disabled ? "Disabled-Card Selectable-Cards" : "Selectable-Cards"
      }
      style={{ cursor: "pointer", padding: "10px" }}
    >
      <Meta
        avatar={icon}
        title={
          <Row justify={"space-between"}>
            <Col>{heading}</Col>
            <Col>
              <MoreMenuIcon />
            </Col>
          </Row>
        }
        description={information}
      />
    </Card>
  );
};

export default BoslerSelectableCard;
