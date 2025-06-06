import { Col, Row, Typography } from "antd";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { ILink } from "./Link.types";
const { Text } = Typography;

interface IProps {
  setNewLinkDetails: any;
  newLinkDetails: ILink;
}

const LinkSourceFolder = ({ newLinkDetails, setNewLinkDetails }: IProps) => {
  return (
    <Row
      gutter={[16, 16]}
      style={{
        marginTop: "5px",
      }}
    >
      <Col span={8}>
        <Text>{getLanguageLabel("subFolder")}</Text>
      </Col>
      <Col span={16}>
        <BoslerInput
          onChange={(e) =>
            setNewLinkDetails({ ...newLinkDetails, subFolder: e.target.value })
          }
          value={newLinkDetails.subFolder}
          required
        />
      </Col>
    </Row>
  );
};

export default LinkSourceFolder;
