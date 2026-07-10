import { Col, Divider, InputNumber, Row, Switch, Typography } from "antd";
import { SaveIcon } from "assets/icons/movetodataActionIcons";
import MoveToDataLoader from "components/movetodataLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";

const { Text, Title } = Typography;
export const MfaSetting = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  // Initialize the state with the value from config.mfaEnabled
  const [ismfaElabed, setMfaEnabled] = useState<boolean>(
    config?.mfaEnabled || false
  );
  const [ismfaEnforced, setMfaEnforced] = useState<boolean>(
    config?.mfaEnabled || false
  );
  useEffect(() => {
    if (isDefined(config)) {
      // Update the state to reflect the mfaEnabled value from config
      setMfaEnabled(config.mfaEnabled);
      setMfaEnforced(config.mfaEnforced);
    }
  }, [config]);
  return (
    <div className="settings-center-block">
      {loading ? (
        <MoveToDataLoader />
      ) : (
        <>
          <p>
            <Row>
              <Col>
                <Title level={3}>{"Multi Factor Authentication"}</Title>
                <Text type="secondary">
                  {
                    "Here you can select whether you want enable Multi Factor Authentication for User's or Not."
                  }
                </Text>
              </Col>
            </Row>
            <Divider />
          </p>

          <Row gutter={16}>
            <Col span={6}>
              <Text type="secondary"> Allow Multi Factor Authentication:</Text>
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
                <Text type="secondary">
                  {" "}
                  here you can enforce the mfa on whole platform
                </Text>
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
        </>
      )}
    </div>
  );
};
