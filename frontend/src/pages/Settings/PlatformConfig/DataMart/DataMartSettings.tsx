import { Col, Divider, Form, Row, Switch, Typography } from "antd";
import { useForm } from "antd/es/form/Form";
import { AddIcon, SaveIcon } from "assets/icons/boslerActionIcons";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerLoader from "components/boslerLoader";
import { getDatamartConfig, updateDataMartConfig } from "pages/Settings/apis";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { isDefined } from "utils/utilities";
import { updatePlatformConfig } from "../../../../redux/actions/platformSettingsActions";
import { RootState, ThunkAppDispatch } from "../../../../redux/types/store";
import DataMartItem from "./DataMartItem";

const { Text, Title } = Typography;
const SAVE_BTN_ID = "DATA_MART_SAVE_BTN";

export const DataMartSettings = () => {
  const dispatch = useDispatch<ThunkAppDispatch>();
  const [form] = useForm();
  const [isLoading, setIsLoading] = useState(true);
  const [dataMartConfigs, setDataMartConfigs] = useState<IDMConfig>();
  const [noChange, setNoChange] = useState<boolean>(false);
  const { config } = useSelector(
    (state) => (state as RootState).platformConfig
  );

  const getDataMartConfig = () => {
    setIsLoading(true);
    getDatamartConfig()
      .then((data: IDMConfig) => {
        setDataMartConfigs(data);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    getDataMartConfig();
  }, []);

  if (isLoading) {
    return <BoslerLoader />;
  }

  return (
    <div className="settings-center-block">
      <div>
        <Row gutter={16}>
          <Col>
            <Title level={3}>Datamart Source</Title>
            <Text type="secondary">
              This space is designated for platform administrators and
              developers for Datamart related source configurations.
            </Text>
          </Col>
        </Row>
        <Divider />

        <Row gutter={16}>
          <Col span={16}>
            <Text type="secondary">Enable Datamart:</Text>
          </Col>
          <Col>
            <Switch
              checkedChildren="Yes"
              unCheckedChildren="No"
              loading={isLoading}
              defaultChecked={
                isDefined(config) ? config.dataMartEnabled : false
              }
              onChange={(checked) => {
                dispatch(
                  updatePlatformConfig({
                    ...config,
                    dataMartEnabled: checked,
                  })
                );
              }}
            />
          </Col>
        </Row>
        <br />
        {config.dataMartEnabled && (
          <Form
            form={form}
            onFinish={(values) => {
              updateDataMartConfig({
                ...values,
                config: "platform",
              })
                .then(({ data }) => {
                  form.setFieldsValue(data);
                  (window as any).makeButtonTemporarySuccess(SAVE_BTN_ID);
                })
                .catch((error) => {
                  (window as any).makeButtonTemporaryFailure(SAVE_BTN_ID);
                });
            }}
            onValuesChange={() => {
              setNoChange(false);
            }}
            initialValues={dataMartConfigs}
          >
            <Form.List name={"dataMartModels"}>
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name, ...restField }, index: any) => (
                    <DataMartItem
                      form={form}
                      outerName={name}
                      remove={remove}
                    />
                  ))}
                  <div className="--flex-row-space-between --mt20">
                    <BoslerButton
                      icon={<AddIcon />}
                      onClick={() => add()}
                      intent="action"
                    >
                      Add DataMart
                    </BoslerButton>
                    <BoslerButton
                      id={SAVE_BTN_ID}
                      onClick={() => {
                        updateDataMartConfig({
                          ...form.getFieldsValue(),
                          config: "platform",
                        })
                          .then(({ data }) => {
                            form.setFieldsValue(data);
                            (window as any).makeButtonTemporarySuccess(
                              SAVE_BTN_ID
                            );
                          })
                          .catch((error) => {
                            (window as any).makeButtonTemporaryFailure(
                              SAVE_BTN_ID
                            );
                          });
                      }}
                      htmlType={"submit"}
                      disabled={noChange}
                      intent={!noChange ? "action" : "none"}
                      icon={<SaveIcon />}
                    >
                      Save
                    </BoslerButton>
                  </div>
                </>
              )}
            </Form.List>
          </Form>
        )}
      </div>
    </div>
  );
};
