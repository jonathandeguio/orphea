import { Col, Divider, Row, Typography } from "antd";
import { ArrowLeftIcon } from "assets/icons/boslerNavigationIcon";
import DeploymentCard from "components/DeploymentCard/DeploymentCard";
import React from "react";
import { Panel } from "react-resizable-panels";
import { useNavigate } from "react-router";

function LeftPanel({
  primaryPanelRef,
  deploymentDetails,
  onDeploymentUpdated,
}: any) {
  const navigate = useNavigate();
  const { Title } = Typography;
  return (
    <Panel collapsible={true} defaultSize={20} ref={primaryPanelRef}>
      <div className="connect-container-left" style={{ padding: "35px" }}>
        <Row justify="space-between">
          <Col>
            <div onClick={() => navigate(-1)} style={{ cursor: "pointer" }}>
              <div className="text-and-icon-center">
                <ArrowLeftIcon size={20} /> {"DEPLOYMENTS"}
              </div>
            </div>
            <Title level={3}>Deployments Details</Title>
          </Col>
        </Row>
        <Divider />
        <DeploymentCard
          details={deploymentDetails}
          onDeploymentUpdated={onDeploymentUpdated}
        />
      </div>
    </Panel>
  );
}

export default LeftPanel;
