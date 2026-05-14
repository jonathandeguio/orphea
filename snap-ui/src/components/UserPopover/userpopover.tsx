import { Avatar, Badge, Col, Popover, Row, Spin, Typography } from "antd";
import { GitCommitIcon } from "assets/icons/orpheaExternalIcons";
import { EmailIcon } from "assets/icons/orpheaFileIcons";
import { GroupsIcon } from "assets/icons/orpheaInterfaceIcons";
import { SharedWorkspaceIcon } from "assets/icons/orpheaMiscellaneousIcons";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  getLanguageLabel,
  getTimeDisplay,
  isDefined,
  isEmpty,
  openNotification,
} from "utils/utilities";
import { IndexedUsersSlice } from "redux/usersSlice";
import { getUserApi } from "./userPopover.api";
import { User } from "global";
const { Title, Text } = Typography;

interface IUserPopover {
  id?: string;
  children?: React.ReactNode;
  record?: any; // FIXME
}

const UserPopOver = ({ children, record, id }: IUserPopover) => {
  const indexedUsers = useSelector((state: any) => state.indexedUsers);
  const dispatch = useDispatch();
  const [userData, setUserData] = useState<User | null>();

  useEffect(() => {
    if (record) {
      setUserData(record);
    } else if (isDefined(id)) {
      if (isDefined(indexedUsers[id])) {
        setUserData(indexedUsers[id]);
      } else {
        getUserApi(id)
          .then(({ data }) => {
            setUserData(data);
            dispatch(IndexedUsersSlice.actions.addUser(data));
          })
          .catch(({ response }) => {
            openNotification(
              response.data.error,
              response.data.description,
              "error"
            );
          });
      }
    }
  }, [record]);

  const content = (
    <>
      {userData ? (
        <>
          <div style={{ width: "17vw" }}>
            <Row>
              <Col style={{ padding: "8px" }}>
                <Badge
                  dot={true}
                  color="green"
                  title={getLanguageLabel("onlineNow")}
                >
                  <Avatar
                    shape="circle"
                    src={
                      userData?.profileImage != ""
                        ? userData?.profileImage
                        : null
                    }
                    size={50}
                  >
                    {userData.name
                      ? userData.name.charAt(0).toUpperCase()
                      : "B"}
                  </Avatar>
                </Badge>
              </Col>

              <Col>
                <Text type="secondary">
                  {userData.lastLoginAt && getTimeDisplay(userData.lastLoginAt)}
                </Text>
              </Col>
            </Row>
            <Row>
              <Col>
                <Row>
                  {/* <UserIcon /> */}
                  {userData.username && (
                    <>
                      <Text strong> {userData.username} &nbsp;&nbsp;</Text>
                    </>
                  )}
                  {userData.name && (
                    <Text type="secondary">{userData.name}</Text>
                  )}
                </Row>
                {userData.email && (
                  <Row>
                    <EmailIcon />
                    <Text type="secondary">{userData.email}</Text>
                  </Row>
                )}

                <Row>
                  {userData.location && (
                    <Row gutter={[6, 6]}>
                      <Col>
                        <SharedWorkspaceIcon />
                        <Text type="secondary">{userData.location}</Text>
                      </Col>
                    </Row>
                  )}
                </Row>
              </Col>
            </Row>
            <Row>
              <GitCommitIcon />
              <Text type="secondary">
                {getLanguageLabel("committedToTestRepository")}
              </Text>
            </Row>
            <Row>
              <GroupsIcon />
              <Text type="secondary">
                {getLanguageLabel("membersOfTestGroup")}
              </Text>
            </Row>
          </div>
        </>
      ) : (
        <Spin />
      )}
    </>
  );

  if (isDefined(userData)) {
    if (isEmpty(children)) {
      return <Popover content={content}>{userData.name}</Popover>;
    }

    return <Popover content={content}>{children}</Popover>;
  } else {
    return <></>;
  }
};

export default UserPopOver;
