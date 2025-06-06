import { Col, Form, Row, Select, Space } from "antd";
import React from "react";
import { getColorTheme, getLanguageLabel, notEmpty } from "utils/utilities";
import { KeplerConfig } from "../charts.config";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";

export const ThemeSelector = () => {
  const { customize } = useSelector((state: RootState) => state.kepler);
  const { config } = useSelector((state: RootState) => state.platformConfig);
  return (
    <>
      <Form.Item
        name="colorTheme"
        label={
          <div className="query_item__heading">
            {getLanguageLabel("colorScheme")}
          </div>
        }
        labelCol={{ span: 10 }}
        wrapperCol={{ span: 14 }}
      >
        <Select style={{ width: "100%" }}>
          {notEmpty(config.customTheme) && (
            <>
              <Select.OptGroup label={"Platform Themes"}>
                {config.customTheme.map((option: any) => (
                  <Select.Option value={option.name} label={option.name}>
                    <Row style={{ width: "100%" }} justify="start">
                      <Col span={8}>
                        <Space>{option.name}</Space>
                      </Col>
                      <Col span={8} offset={8}>
                        <Space style={{ overflow: "hidden" }}>
                          {option.color.map((c: string) => (
                            <div
                              style={{
                                height: "12px",
                                width: "12px",
                                display: "inline-block",
                                backgroundColor: c,
                              }}
                            ></div>
                          ))}
                        </Space>
                      </Col>
                    </Row>
                  </Select.Option>
                ))}
              </Select.OptGroup>
            </>
          )}
          <Select.OptGroup label={"Preset Themes"}>
            {KeplerConfig.chartThemeOptions.map((option: any) => (
              <Select.Option value={option.value} label={option.label}>
                <Row style={{ width: "100%" }} justify="start">
                  <Col span={8}>
                    <Space>{option.label}</Space>
                  </Col>
                  {option.value !== "custom" && (
                    <Col span={8} offset={8}>
                      <Space style={{ overflow: "hidden" }}>
                        {getColorTheme(
                          option.value as string,
                          customize
                        ).color.map((c: string) => (
                          <div
                            style={{
                              height: "12px",
                              width: "12px",
                              display: "inline-block",
                              backgroundColor: c,
                            }}
                          ></div>
                        ))}
                      </Space>
                    </Col>
                  )}
                </Row>
              </Select.Option>
            ))}
          </Select.OptGroup>
        </Select>
      </Form.Item>
    </>
  );
};
