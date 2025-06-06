import {
  Col,
  Divider,
  Row,
  Select,
  Skeleton,
  Table,
  Typography,
  message,
} from "antd";
import axios from "axios";
const { Option } = Select;

import { useEffect, useState } from "react";
import { useSelector } from "react-redux";

// import { setTheme } from "../../App";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

import { GroupsIcon } from "assets/icons/boslerInterfaceIcons";
import classNames from "classnames";
import { useNavigate } from "react-router";
import styles from "./Users.module.scss";

const { Title, Text } = Typography;

const LoginActivity = () => {
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);

  const navigate = useNavigate();

  const [userGroups, setUserGroups] = useState();

  useEffect(() => {
    getUserGroups();
  }, []);

  const getUserGroups = async () => {
    try {
      const data = axios
        .get(`/passport/users/userGroups/${user?.id}`)
        .then((data) => {
          setUserGroups(data.data);
        });
    } catch (error) {
      message.error("Something went wrong");
    }
  };

  const userGroupColumns = [
    {
      title: (
        <Text
          type="secondary"
          strong
          className={classNames(styles.tableHeaderItem)}
        >
          {" "}
          {getLanguageLabel("groupName").toUpperCase()}{" "}
        </Text>
      ),
      dataIndex: "name",
      key: "name",
      render: (text: any, record: any) => {
        return (
          <>
            <GroupsIcon />
            <div
              className="pop-over-item"
              style={{ display: "inline" }}
              onClick={() =>
                navigate(`/portal/settings/groups/${record.id}/manageGroup`)
              }
            >
              {text}
            </div>
          </>
        );
      },
    },
  ];

  return !user ? (
    <Skeleton
      active
      avatar
      paragraph={{ rows: 20 }}
      className={styles.listItem}
    />
  ) : (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>{getLanguageLabel("groups")}</Title>
            <Text type="secondary">{getLanguageLabel("userGroupsList")}</Text>
          </Col>
        </Row>
        <Divider />
      </p>

      <Table
        dataSource={userGroups}
        columns={userGroupColumns}
        pagination={false}
        scroll={{ y: "60vh" }}
        size="middle"
      />
    </div>
  );
};

export default LoginActivity;
