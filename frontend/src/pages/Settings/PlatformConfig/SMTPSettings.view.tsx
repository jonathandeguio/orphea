import { Col, Divider, Input, Row, Typography } from "antd";
import React, { useEffect, useState } from "react";

import { Form } from "antd";
import BoslerButton from "../../../components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "../../../components/BoslerComponents/InputComponent/BoslerInput";

import BoslerLoader from "components/boslerLoader";
import { getSmtpConfig, updateSmtpConfig } from "../apis";

const { Title, Text } = Typography;

const SMTPSettings = () => {
  const [form] = Form.useForm();

  const [smtpConfig, setSmtpConfig] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getSmtpConfig().then((initialData: any) => {
      setSmtpConfig(initialData);
      setLoading(false);
    });
  }, []);

  return (
    <div className="settings-center-block">
      <p>
        {!loading ? (
          <Form
            form={form}
            layout="horizontal"
            initialValues={smtpConfig}
            onFinish={(values: any) => {
              updateSmtpConfig(values);
            }}
          >
            <Row>
              <Col>
                <Title level={3}>Mailer Configuration</Title>
                <Text type="secondary">
                  Change the SMTP configuration for the platform from here.
                </Text>
              </Col>
            </Row>

            <Divider />

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Host</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Text type="secondary">SMTP host name like smtp.gmail.com</Text>
                <Form.Item name="host">
                  <BoslerInput className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Port</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Text type="secondary">Port for the SMTP host like 587</Text>
                <Form.Item name="port">
                  <BoslerInput className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Authentication</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Form.Item name="auth">
                  <BoslerInput className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Transport TLS</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Form.Item name="ttls">
                  <BoslerInput className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Email</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Text type="secondary">Email for the mailer</Text>
                <Form.Item name="smtpEmail">
                  <BoslerInput className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <Text>Password</Text>
              </Col>
              <Col className="settings-mailer-col" span={8}>
                <Text type="secondary">Password for Mailer</Text>
                <Form.Item name="smtpPassword">
                  <Input.Password className="settings-mailer-input" />
                </Form.Item>
              </Col>
            </Row>

            <Row className="settings-mailer-row" justify="space-between">
              <Col span={16}>
                <div
                  className="BoslerSubHeader1 text-and-icon-center"
                  style={{ marginRight: "0.5rem" }}
                >
                  Only platform administrators can view or edit this
                  configuration.
                </div>
              </Col>
              <Col span={8}>
                <Form.Item style={{ marginBottom: 0, marginLeft: "auto" }}>
                  <BoslerButton htmlType="submit" intent="primary">
                    Update Configuration
                  </BoslerButton>
                </Form.Item>
              </Col>
            </Row>

            <br />
          </Form>
        ) : (
          <BoslerLoader />
        )}
      </p>
    </div>
  );
};
export default SMTPSettings;
