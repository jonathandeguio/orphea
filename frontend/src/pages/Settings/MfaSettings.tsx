import { Col, Divider, Row, Switch, Typography } from "antd";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { updatePlatformConfig } from "../../redux/actions/platformSettingsActions";

const { Text, Title } = Typography;
export const MfaSetting = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  // Initialize the state with the value from config.mfaEnabled
  const [ismfaEnabled, setMfaEnabled] = useState<boolean>(
    config?.mfaEnabled || false
  );
  const [ismfaEnforced, setMfaEnforced] = useState<boolean>(
    config?.mfaEnabled || false
  );

  useEffect(() => {
    if (isDefined(config)) {
      console.log(config);
      // Update the state to reflect the mfaEnabled value from config
      setMfaEnabled(config.mfaEnabled);
      setMfaEnforced(config.mfaEnforced);
    }
  }, [config]);
  return (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>{getLanguageLabel("enforce")}</Title>
            <Text type="secondary">
              {getLanguageLabel("enforceMFAPlatform")}
            </Text>
          </Col>
        </Row>
        <Divider />
      </p>
      <Row style={{ flexDirection: "column", gap: "20px" }}>
        <Row gutter={16}>
          <Col span={10}>
            <Text type="secondary"> {getLanguageLabel("activate")}</Text>
          </Col>
          <Col>
            <Switch
              checkedChildren="On"
              unCheckedChildren="Off"
              loading={loading}
              defaultChecked={
                isDefined(config) ? config.mfaEnabled : false || null
              }
              onChange={(checked) => {
                dispatch(
                  updatePlatformConfig({
                    ...config,
                    mfaEnabled: checked,
                    mfaEnforced: false,
                  })
                );
              }}
            />
          </Col>
        </Row>
        {config.mfaEnabled === true ? (
          <Row gutter={16}>
            <Col span={10}>
              <Text type="secondary"> {getLanguageLabel("enforce")}</Text>
            </Col>
            <Col>
              <Switch
                checkedChildren="On"
                unCheckedChildren="Off"
                loading={loading}
                defaultChecked={
                  isDefined(config) ? config.mfaEnforced : false || null
                }
                onChange={(checked) => {
                  dispatch(
                    updatePlatformConfig({ ...config, mfaEnforced: checked })
                  );
                }}
              />
            </Col>
          </Row>
        ) : (
          ""
        )}
      </Row>
    </div>
  );
};
