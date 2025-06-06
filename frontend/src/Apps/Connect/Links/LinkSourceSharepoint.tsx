import { Col, Row, Typography } from "antd";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React from "react";
import { ILink } from "./Link.types";
const { Text } = Typography;

interface IProps {
  setNewLinkDetails: any;
  newLinkDetails: ILink;
}

const LinkSourceSharepoint = ({
  newLinkDetails,
  setNewLinkDetails,
}: IProps) => {
  return (
    <Row
      gutter={[16, 16]}
      style={{
        marginTop: "5px",
      }}
    >
      <Col span={8}>
        <Text>{"File ID"}</Text>
      </Col>
      <Col span={16}>
        <BoslerInput
          onChange={(e) =>
            setNewLinkDetails({ ...newLinkDetails, fileId: e.target.value })
          }
          value={newLinkDetails.fileId}
          required
        />
      </Col>
      <Col span={8}>
        <Text>{"Sheet Name"}</Text>
      </Col>
      <Col span={16}>
        <BoslerInput
          onChange={(e) =>
            setNewLinkDetails({ ...newLinkDetails, sheetName: e.target.value })
          }
          value={newLinkDetails.sheetName}
          required
        />
      </Col>
    </Row>
  );
};

export default LinkSourceSharepoint;
