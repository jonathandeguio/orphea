import { Col, Divider, Row, Select, Typography } from "antd";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel, isDefined } from "utils/utilities";
import { updatePlatformConfig } from "../../../redux/actions/platformSettingsActions";
import { timezones } from "./PlatformConfig.utils";

const { Text, Title } = Typography;
const { Option } = Select;
export const TimezoneSettings = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  return (
    <div className="settings-center-block">
      <p>
        <Row>
          <Col>
            <Title level={3}>{getLanguageLabel("timezone")}</Title>
            <Text type="secondary">
              {getLanguageLabel("selectThePlatformTimezone")}
            </Text>
          </Col>
        </Row>
        <Divider />
      </p>

      <Row>
        <Col span={24}>
          <Text type="secondary">{getLanguageLabel("timezoneMsg")}</Text>
          <br />
          <br />
          <Row>
            <Col span={6}>
              <Text type="secondary">Platform Timezone</Text>
            </Col>
            <Col>
              <Select
                showSearch
                placeholder="Search for timezones..."
                optionLabelProp="label"
                defaultValue={config.timezone}
                style={{ minWidth: "30vw" }}
                onChange={(timezone) => {
                  dispatch(
                    updatePlatformConfig({ ...config, timezone: timezone })
                  );
                }}
              >
                {timezones.map((timezone) => (
                  <Option key={timezone.value} value={timezone.value}>
                    <Row justify={"space-between"} align={"middle"}>
                      <Col>
                        {timezone.value}{" "}
                        {isDefined(timezone.shortForm) &&
                          `(${timezone.shortForm})`}
                      </Col>
                      <Col>{timezone.label}</Col>
                    </Row>
                  </Option>
                ))}
              </Select>
              <br />
              <br />
            </Col>
          </Row>
        </Col>
      </Row>
    </div>
  );
};
