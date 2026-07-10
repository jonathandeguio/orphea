import { Col, Divider, Row, Select, Typography } from "antd";
import React from "react";
import { useDispatch, useSelector } from "react-redux";

import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import UploadLogoButton from "../UploadLogoButton.view";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";

const { Text, Title } = Typography;
const { Option } = Select;

export const ThemeSettings = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  return (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>Platform customization</Title>
            <Text type="secondary">
              User can select the platform icon and theme palette
            </Text>
          </Col>
        </Row>
        <Divider />
        <Row justify="space-between">
          <Col>
            <Text>Custom Platform Name</Text>
            <br />
            <Text type="secondary">
              This Name will replace the Platform Name at all the places.
            </Text>
          </Col>
          <Col>
            <MoveToDataInput
              value={config.platformName}
              debounceInterval={2000}
              onChange={(e) => {
                dispatch(
                  updatePlatformConfig({
                    ...config,
                    platformName: e.target.value,
                  })
                );
              }}
            />
          </Col>
        </Row>
        <br />
        <Row justify="space-between">
          <Col>
            <Text>Custom logo</Text>
            <br />
            <Text type="secondary">
              This logo will replace the MoveToData logo at all the places.
            </Text>
          </Col>
          <Col>
            <UploadLogoButton />
          </Col>
        </Row>

        <br />
        <Row justify="space-between">
          <Col>
            <Text>Theme palette</Text>
          </Col>
          <Col span={8}>
            <Select style={{ width: "100%" }} />
          </Col>
        </Row>
        <Row>
          <Col>
            <Text type="secondary">
              Select the active theme palette used in MoveToData charts in your
              projects, or create your own.
            </Text>
          </Col>
        </Row>
      </p>
    </div>
  );
};
