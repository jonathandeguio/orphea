import { Card, CardProps, Col, Row, Typography } from "antd";
import { MoreMenuIcon } from "assets/icons/orpheaActionIcons";
import React from "react";

interface TOrpheaCardProps extends CardProps {
  heading?: any;
  icon?: any;
  information?: any;
  disabled?: boolean;
}
const { Title } = Typography;
const { Meta } = Card;
const OrpheaSelectableCard = ({
  heading,
  icon,
  information,
  disabled,
}: TOrpheaCardProps) => {
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

export default OrpheaSelectableCard;
