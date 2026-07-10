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

const DisableMfa = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { user } = useSelector((state) => (state as $TSFixMe).userDetails);
  const { allusers } = useSelector(
    (state) => (state as $TSFixMe).allUserDetails
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const { user: userAdmin } = useSelector((state) => (state as any).userAdmin);
  const [selectedUser, setSelectedUser] = useState({
    id: user?.id,
    name: user?.name,
  });

  const handleMfaDisable = async () => {
    try {
      await axios.post(`/passport/users/disableMfa/${selectedUser.id}`);
    } catch (error: any) {
      openNotification(
        "Failed to disable Mfa",
        (error.response as any)?.data,
        "error"
      );
    }
  };

  useEffect(() => {
    if (platformAdmin || userAdmin) dispatch(getAllUserDetails());
  }, []);

  return (
    <div className="settings-center-block" style={{ padding: "20px" }}>
      <Row justify="center">
        <Col span={16}>
          <Title level={3}>Disable Mfa</Title>
          <Text type="secondary" style={{ marginBottom: "16px", display: "block" }}>
            Disable Mfa here for the selected user.
          </Text>
          <Divider />
        </Col>
      </Row>

      <Row justify="center" gutter={[16, 16]}>
        <Col span={16}>
          {(platformAdmin || userAdmin) && (
            <Tooltip title="Select the user to disable MFA for">
              <Select
                showSearch
                allowClear
                optionFilterProp="children"
                defaultValue={user?.id}
                style={{
                  width: "100%",
                  marginBottom: "20px",
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
                      {i?.name} ({i?.username})
                    </div>
                  ),
                  name: i.name,
                  id: i.id,
                  value: i.id,
                }))}
                filterOption={(input, option) => {
                  return (
                    option?.name.toLowerCase().indexOf(input.toLowerCase()) >= 0
                  );
                }}
              />
              <Text type="secondary">
                Admins can disable Mfa for other users.
              </Text>
            </Tooltip>
          )}
        </Col>
      </Row>

      <Row justify="center" style={{ marginTop: "20px" }}>
        <Col span={6}>
          <MoveToDataButton
            icon={<LockIcon />}
            intent="action"
            onClick={handleMfaDisable}
          >
            Disable Mfa
          </MoveToDataButton>
        </Col>
      </Row>
    </div>
  );
};

export default DisableMfa;
