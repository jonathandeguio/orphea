import { Col, Divider, InputNumber, Row, Switch, Typography } from "antd";
import { SaveIcon } from "assets/icons/movetodataActionIcons";
import MoveToDataLoader from "components/movetodataLoader";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { getLanguageLabel, isDefined, openNotification } from "utils/utilities";
import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import { defaultExpiration } from "./PlatformConfig.constants";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";

const { Text, Title } = Typography;
export const CacheSettings = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  const [selectedLimits, setSelectedLimits] = useState({
    cacheExpiration: defaultExpiration,
  });

  useEffect(() => {
    if (isDefined(config))
      setSelectedLimits({
        cacheExpiration: isDefined(config)
          ? config.cacheExpiration / 86400
          : defaultExpiration,
      });
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
                <Title level={3}>{getLanguageLabel("caching")}</Title>
                <Text type="secondary">{getLanguageLabel("cachingMsg2")}</Text>
              </Col>
            </Row>
            <Divider />
          </p>

          <Row gutter={16}>
            <Col span={6}>
              <Text type="secondary"> Allow Caching:</Text>
            </Col>

            <Col>
              <Switch
                checkedChildren="Yes"
                unCheckedChildren="No"
                loading={loading}
                defaultChecked={isDefined(config) ? config.cache : false}
                onChange={(checked) => {
                  dispatch(updatePlatformConfig({ ...config, cache: checked }));
                }}
              />
            </Col>
          </Row>
          {config.cache && (
            <>
              <br />
              <Row gutter={16} align="middle" style={{ marginTop: "10px" }}>
                <Col span={6}>
                  <Text type="secondary">Cache Expiration</Text>
                  &nbsp;
                  <Text strong>( [1,90] ):</Text>
                </Col>

                <Col>
                  <InputNumber
                    min={1}
                    max={90}
                    controls={false}
                    addonAfter="Days"
                    value={selectedLimits.cacheExpiration}
                    onChange={(val: any) => {
                      setSelectedLimits({
                        ...selectedLimits,
                        cacheExpiration: val,
                      });
                    }}
                  />
                </Col>
              </Row>
              <br />
              <br />
              <Row style={{ marginTop: "10px" }}>
                <Col span={6}></Col>
                <Col>
                  <MoveToDataButton
                    icon={<SaveIcon />}
                    intent="primary"
                    onClick={() => {
                      if (
                        selectedLimits.cacheExpiration < 1 ||
                        selectedLimits.cacheExpiration > 90
                      ) {
                        openNotification(
                          "select a value in between 1 to 90 days",
                          "",
                          "info"
                        );
                        return;
                      }
                      dispatch(
                        updatePlatformConfig({
                          ...config,
                          cacheExpiration:
                            selectedLimits.cacheExpiration * 86400,
                        })
                      );
                    }}
                    textTransform="none"
                  >
                    {" "}
                    {getLanguageLabel("update")}{" "}
                  </MoveToDataButton>
                </Col>
              </Row>{" "}
            </>
          )}
        </>
      )}
    </div>
  );
};
