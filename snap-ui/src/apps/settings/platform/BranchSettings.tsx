import { Col, Divider, Row, Typography } from "antd";
import React from "react";

import { Form } from "antd";
import OrpheaLoader from "components/orpheaLoader";
import { useDispatch, useSelector } from "react-redux";
import { updatePlatformConfig } from "redux/actions/platformSettingsActions";
import { ThunkAppDispatch } from "redux/types/store";
import OrpheaInput from "components/InputComponent/OrpheaInput";
import OrpheaButton from "components/ButtonComponent/OrpheaButton";

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
                  <OrpheaInput />
                </Form.Item>
              </Col>
            </Row>
            <Row justify="end">
              <Col>
                <Form.Item>
                  <OrpheaButton intent="primary" htmlType="submit">
                    Update Branch
                  </OrpheaButton>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        ) : (
          <OrpheaLoader />
        )}
      </p>
    </div>
  );
};
export default BranchSettings;
