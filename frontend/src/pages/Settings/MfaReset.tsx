import {
  Avatar,
  Col,
  Divider,
  Row,
  Select,
  Spin,
  Tooltip,
  Typography,
} from "antd";
import axios from "axios";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import { getAllUserDetails } from "../../redux/actions/userActions";

import { ClearCacheRunIcon } from "assets/icons/boslerActionIcons";
import { KeyIcon } from "assets/icons/boslerInterfaceIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import { getLanguageLabel, openNotification } from "utils/utilities";
import { useNavigate } from "react-router";
import { Console } from "console";

const { Text, Title } = Typography;

const MfaReset = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { user } = useSelector((state: RootState) => state.userDetails);
  const { allusers } = useSelector((state: RootState) => state.allUserDetails);
  const [loading, setLoading] = useState<boolean>(false);
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const { user: userAdmin } = useSelector((state) => (state as any).userAdmin);

  const [selectedUser, setSelectedUser] = useState({
    id: user?.id,
    name: user?.name,
    username: user?.username,
  });

  const handleMfaDisable = async () => {
    try {
      setLoading(true);
      await axios.post(`/passport/mfa/disable/${selectedUser.username}`);
      setLoading(false);
    } catch (error: any) {
      setLoading(false);
      openNotification(
        "Failed to disable Mfa",
        (error.response as any)?.data,
        "error"
      );
    }
  };
  const navigate = useNavigate();
  useEffect(() => {
    if (!platformAdmin || !userAdmin) {
      navigate("/portal/home");
    }
    if (platformAdmin || userAdmin) dispatch(getAllUserDetails());
  }, []);

  return (
    <div className="settings-center-block" style={{ padding: "20px" }}>
      <Row justify="center">
        <Col span={16}>
          <Tooltip
            title={getLanguageLabel("resetMFAAdminsOnly")}
            placement="right"
          >
            <div className="text-and-icon-center">
              <Title level={3}>{getLanguageLabel("reset")}</Title>
            </div>
            <span style={{ marginLeft: "20px" }}>
              <KeyIcon />
            </span>
          </Tooltip>

          <Text
            type="secondary"
            style={{ marginBottom: "16px", display: "block" }}
          >
            {getLanguageLabel("mfaHeading")}
          </Text>
          <Divider />
        </Col>
      </Row>

      <Row justify="center" gutter={[16, 16]}>
        <Col span={16}>
          {(platformAdmin || userAdmin) && (
            <Tooltip title={getLanguageLabel("selectUserToDisableMFA")}>
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
                  setSelectedUser({
                    id: value,
                    name: options?.name,
                    username: options?.username,
                  });
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
                  username: i.username,
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
            </Tooltip>
          )}
        </Col>
      </Row>

      <Row justify="center" style={{ marginTop: "20px" }}>
        <Col span={16}>
          <Col style={{ display: "flex", gap: "20px", alignItems: "center" }}>
            <BoslerButton
              icon={<ClearCacheRunIcon />}
              intent="dangerous"
              onClick={handleMfaDisable}
              loading={loading}
            >
              {getLanguageLabel("reset")}
            </BoslerButton>
          </Col>
          <br />
          <Text style={{ fontSize: "0.8rem" }} type="secondary">
            {getLanguageLabel("resetMFAAdminsOnly")}
          </Text>
        </Col>
      </Row>
    </div>
  );
};

export default MfaReset;
