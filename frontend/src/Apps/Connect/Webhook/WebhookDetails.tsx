import { useNavigateHelper } from "Apps/explorer/explorer.hooks";
import { Col, Row, Typography } from "antd";
import { EditIcon } from "assets/icons/boslerEditorIcons";
import { FolderIcon } from "assets/icons/boslerFileIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import RestAPISourcePage from "../Sources/RestAPIConnector/RestAPISourcePage";
import { ISourceConfig } from "../Sources/Source";
import { IWebhook } from "./Webhook.types";

interface IProps {
  source: ISourceConfig;
  webhook: IWebhook;
}

const WebhookDetails = ({ source, webhook }: IProps) => {
  const { Text } = Typography;
  const navigator = useNavigateHelper();
  return (
    <div className="--flex-col-center">
      <div
        className="--p20"
        style={{
          flex: 1,
          height: "100%",

          width: "100%",
        }}
      >
        <div className="connect-container-left" style={{ padding: "35px" }}>
          <Row justify={"space-between"} align="middle">
            <Col>
              <Text type="secondary" strong>
                {getLanguageLabel("general")}
              </Text>
            </Col>
            <Col>
              <BoslerButton
                icon={<EditIcon />}
                intent="primary"
                // onClick={() => setIsUpdateSourceModalOpen(true)}
              >
                {getLanguageLabel("edit")}
              </BoslerButton>
            </Col>
          </Row>

          <Row
            justify={"space-between"}
            align="middle"
            style={{ marginTop: "10px" }}
            gutter={[16, 16]}
          >
            <Col span={8}>
              <Text>{getLanguageLabel("name")}</Text>
            </Col>
            <Col span={16}>
              <Text>{(source as any).name}</Text>
            </Col>
          </Row>
          {source.description && (
            <>
              <Row
                justify={"space-between"}
                align="top"
                style={{ marginTop: "10px" }}
                gutter={[16, 16]}
              >
                <Col span={8}>
                  <Text>{getLanguageLabel("description")}</Text>
                </Col>
                <Col span={16}>
                  <Text>{(source as any).description}</Text>
                </Col>
              </Row>
            </>
          )}

          <Row
            justify={"space-between"}
            align="top"
            style={{ marginTop: "10px" }}
            gutter={[16, 16]}
          >
            <Col span={8}>
              <Text>{getLanguageLabel("parentFolder")}</Text>
            </Col>
            <Col span={16}>
              <BoslerButton
                intent="none"
                onClick={() => navigator(source.parent)}
                icon={<FolderIcon />}
                minimal
              >
                {source.parent}
              </BoslerButton>
            </Col>
          </Row>
          {source.type === "rest" && <RestAPISourcePage source={source} />}
        </div>
      </div>
    </div>
  );
};

export default WebhookDetails;
