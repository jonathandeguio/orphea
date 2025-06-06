import { Card, Col, Divider, Row, Table, Typography } from "antd";

import { useSelector } from "react-redux";

import React from "react";
import { getLanguageLabel } from "utils/utilities";
import { useUserProjectsController } from "./utils/useUserProjectsController";

const { Title, Text } = Typography;

const UserProjects = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  const {
    projectsWithUserRole,
    loading,
    userProjectColumns,
    isCurrentUserAdmin,
  } = useUserProjectsController({
    userId: user.id,
  });

  return (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>{getLanguageLabel("projects")}</Title>
            <Text type="secondary">{getLanguageLabel("userProjectList")}</Text>
          </Col>
        </Row>
        <Divider />
      </p>

      {isCurrentUserAdmin ? (
        <Card>
          <Title level={3}>
            {getLanguageLabel("adminUserMyProjectsMessage")}
          </Title>
        </Card>
      ) : (
        <Table
          dataSource={projectsWithUserRole}
          columns={userProjectColumns}
          pagination={false}
          loading={loading}
          scroll={{
            y: "60vh",
          }}
        />
      )}
    </div>
  );
};

export default UserProjects;
