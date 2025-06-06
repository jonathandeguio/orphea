import { Col, Divider, Row, Select, Table, Typography } from "antd";
import axios from "axios";

import { useEffect, useState } from "react";
import { useSelector } from "react-redux";

// import { setTheme } from "../../app";
import React from "react";
import { getLanguageLabel, openNotification } from "utils/utilities";

import BoslerLoader from "components/boslerLoader";

const { Title, Text } = Typography;

const LoginActivity = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  const [last10Login, setLast10Login] = useState();

  useEffect(() => {
    getLoginActivity();
  }, []);

  const getLoginActivity = async () => {
    try {
      const data = axios
        .get(`/passport/users/${user?.id}/last10Login`)
        .then((data) => {
          setLast10Login(data.data);
        });
    } catch (error) {
      openNotification("Something went wrong", " ", "error");
    }
  };

  const columns = [
    {
      title: "Agent",
      dataIndex: "agent",
      key: "agent",
    },
    {
      title: "Remote Address",
      dataIndex: "remoteAddr",
      key: "remoteAddr",
    },
    {
      title: "Last Login",
      dataIndex: "lastLoginAt",
      key: "lastLoginAt",
      render: (text: number) => new Date(text).toLocaleString(),
    },
    {
      title: "Logged Out",
      dataIndex: "lastLogoutAt",
      key: "lastLogoutAt",
      render: (text: number) =>
        text ? new Date(text).toLocaleString() : getLanguageLabel("noStatus"),
    },
  ];
  return !user ? (
    <BoslerLoader />
  ) : (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>{getLanguageLabel("loginActivity")}</Title>
            <Text type="secondary">{getLanguageLabel("loginActivity")}</Text>
          </Col>
        </Row>
        <Divider />
      </p>

      <Table
        dataSource={last10Login}
        columns={columns}
        pagination={false}
        // scroll={{ y: "60vh" }}
      />
    </div>
  );
};

export default LoginActivity;
