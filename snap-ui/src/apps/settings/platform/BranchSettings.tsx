import { Col, Divider, Row, Typography } from "antd";
import React from "react";

import { Form } from "antd";
import MoveToDataLoader from "components/movetodataLoader";
import { useDispatch, useSelector } from "react-redux";
import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import MoveToDataInput from "components/InputComponent/MoveToDataInput";
import MoveToDataButton from "components/ButtonComponent/MoveToDataButton";

const { Title, Text } = Typography;

const BranchSettings = () => {
  const [form] = Form.useForm();

  const dispatch = useDispatch<ThunkAppDispatch>();

  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );

  return (
    <div className="settings-center-block">
      <p>
        {!loading ? (
          <Form
            form={form}
            layout="horizontal"
            initialValues={config}
            onFinish={(values: any) => {
              dispatch(
                updatePlatformConfig({
                  ...config,
                  defaultBranch: values.defaultBranch,
                })
              );
            }}
          >
            <Row>
              <Col>
                <Title level={3}>Default Branch</Title>
                <Text type="secondary">
                  Change the default branch for the platform from here.
                </Text>
              </Col>
            </Row>

            <Divider />
            <Row justify="space-between">
              <Col span={6}>
                <Text type="secondary"> Default Branch:</Text>
              </Col>

              <Col>
                <Form.Item name="defaultBranch">
                  <MoveToDataInput />
                </Form.Item>
              </Col>
            </Row>
            <Row justify="end">
              <Col>
                <Form.Item>
                  <MoveToDataButton intent="primary" htmlType="submit">
                    Update Branch
                  </MoveToDataButton>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        ) : (
          <MoveToDataLoader />
        )}
      </p>
    </div>
  );
};
export default BranchSettings;
