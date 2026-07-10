import {
  Avatar,
  Col,
  Divider,
  Input,
  Row,
  Select,
  Tooltip,
  Typography,
} from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getAllUserDetails } from "redux/actions/userActions";
import { ThunkAppDispatch } from "redux/types/store";

import { getLanguageLabel, openNotification } from "utils/utilities";
import { LockIcon } from "assets/icons/movetodataActionIcons";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";

const { Text, Title } = Typography;

const ChangePassword = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { allusers } = useSelector(
    (state) => (state as $TSFixMe).allUserDetails
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const { user: userAdmin } = useSelector((state) => (state as any).userAdmin);
  const [password, setPassword] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [selectedUser, setSelectedUser] = useState({
    id: user?.id,
    name: user?.name,
  });

  const [shakeEffect, setShakeEffect] = useState(false);

  const handleChangePassword = async () => {
    if (password.confirmPassword != password.newPassword) {
      openNotification("Password Change", "Password do not match!", "error");
      setShakeEffect(true);
      return;
    } else if (
      password.newPassword.length < 4 &&
      password.confirmPassword.length < 4
    ) {
      openNotification(
        "Password Change",
        "Minimum Length of Password should be 4 characters !",
        "error"
      );
      setShakeEffect(true);
      return;
    }
    try {
      const body = {
        currentPassword: password.currentPassword,
        userId: selectedUser.id,
        password: password.newPassword,
      };

      await axios.post(`/passport/users/changePassword`, JSON.stringify(body));
      // openNotification(
      //   "Password Change",
      //   "Password Changed successfully.",
      //   "success"
      // );
      setShakeEffect(false);
    } catch (error: any) {
      setShakeEffect(true);
      openNotification(
        "Failed to change password",
        (error.response as any)?.data,
        "error"
      );
    }
  };

  useEffect(() => {
    if (platformAdmin || userAdmin) dispatch(getAllUserDetails());
  }, []);
  return (
    <>
      <div className="settings-center-block">
        <p>
          <Row justify="space-between">
            <Col>
              <Title level={3}>{getLanguageLabel("changePassword")}</Title>
              <Text type="secondary">
                {getLanguageLabel("changeYourPasswordHere")}
              </Text>
            </Col>
            <Col span={12}>
              {(platformAdmin || userAdmin) && (
                <Tooltip title="Select the user to change password for:">
                  <Select
                    showSearch
                    allowClear
                    optionFilterProp="children"
                    defaultValue={user.id}
                    style={{
                      width: "100%",
                    }}
                    onChange={(value: string, options: any) => {
                      setSelectedUser({ id: value, name: options?.name });
                    }}
                    options={allusers?.map((i: $TSFixMe) => ({
                      label: (
                        <div className="text-and-icon-center">
                          <Avatar
                            src={i.profileImage != "" ? i.profileImage : null}
                            size="small"
                            style={{ verticalAlign: "middle" }}
                          >
                            {i.name ? i.name.charAt(0).toUpperCase() : "B"}
                          </Avatar>
                          &nbsp;
                          {i?.name}( {i?.username} )
                        </div>
                      ),
                      name: i.name,
                      id: i.id,
                      value: i.id,
                    }))}
                    filterOption={(input, option) => {
                      return (
                        option?.name
                          .toLowerCase()
                          .indexOf(input.toLowerCase()) >= 0
                      );
                    }}
                  />
                  <Text type="secondary">
                    {getLanguageLabel("adminsCanChangePasswordsForOtherUsers")}
                  </Text>
                </Tooltip>
              )}
            </Col>
          </Row>
          <Divider />
        </p>
        <Row justify="start">
          <Col span={8}>{getLanguageLabel("currentPassword")}:</Col>
          <Col span={6}>
            <Input.Password
              onChange={(e) =>
                setPassword({
                  ...password,
                  currentPassword: e.target.value,
                })
              }
              value={password.currentPassword}
              name="cpassword"
              placeholder={
                user.name == selectedUser.name
                  ? getLanguageLabel("currentPassword")
                  : user.name + "'s " + getLanguageLabel("currentPassword")
              }
              className={shakeEffect ? "error" : ""}
              required
            />
            {user.name != selectedUser.name && (
              <>
                <br />
                <Text type="secondary">
                  {user.name}
                  's
                </Text>
              </>
            )}
          </Col>
        </Row>
        <br />
        <Row justify="start" style={{ marginTop: "40px" }}>
          <Col span={8}>
            {getLanguageLabel("new")} {getLanguageLabel("password")}:
          </Col>
          <Col span={6}>
            <Input.Password
              onChange={(e) =>
                setPassword({
                  ...password,
                  newPassword: e.target.value,
                })
              }
              value={password.newPassword}
              name="npassword"
              placeholder={
                user.name == selectedUser.name
                  ? getLanguageLabel("newPassword")
                  : selectedUser.name + "'s " + getLanguageLabel("password")
              }
              className={shakeEffect ? "error" : ""}
              required
            />
            {user.name != selectedUser.name && (
              <>
                <br />
                <Text type="secondary">
                  {selectedUser.name}
                  's
                </Text>
              </>
            )}
          </Col>
        </Row>
        <br />

        <Row justify="start" style={{ marginTop: "10px" }}>
          <Col span={8}>{getLanguageLabel("confirmPassword")}:</Col>
          <Col span={6}>
            <Input.Password
              onChange={(e) =>
                setPassword({
                  ...password,
                  confirmPassword: e.target.value,
                })
              }
              value={password.confirmPassword}
              name="cpassword"
              placeholder={getLanguageLabel("confirmPassword")}
              className={shakeEffect ? "error" : ""}
              required
            />
            {user.name != selectedUser.name && (
              <>
                <br />
                <Text type="secondary">
                  {selectedUser.name}
                  's
                </Text>
              </>
            )}
          </Col>
        </Row>
        <br />
        <Row justify="start" style={{ marginTop: "20px" }}>
          <Col span={8}></Col>
          <Col span={6}>
            <MoveToDataButton
              icon={<LockIcon />}
              intent="action"
              onClick={handleChangePassword}
            >
              {" "}
              {getLanguageLabel("changePassword")}{" "}
            </MoveToDataButton>
          </Col>
        </Row>
      </div>
    </>
  );
};

export default ChangePassword;
