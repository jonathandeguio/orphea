import { Col, Divider, Row, Select, Typography } from "antd";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { ThunkAppDispatch } from "redux/types/store";
import { getLanguageLabel } from "utils/utilities";
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
              <Text type="secondary">Platform TimeZone</Text>
            </Col>
            <Col>
              <Select
                showSearch
                placeholder="Search for timezones..."
                optionLabelProp="label"
                defaultValue="Europe/Paris (CEST)"
                style={{ minWidth: "30vw" }}
                // filterOption={(input, option) =>
                //   option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
                // }
              >
                {timezones.slice(0, 10).map((timezone) => (
                  <Option key={timezone.value} value={timezone.value}>
                    <div>
                      {timezone.value}

                      {timezone.label}
                    </div>
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
