import { Col, Divider, Form, Radio, Row, Typography } from "antd";
import TextArea from "antd/es/input/TextArea";

import BoslerLoader from "components/boslerLoader";
import React, { useEffect, useState } from "react";

import {
  decodeFromBase64,
  encodeToBase64,
  getLanguageLabel,
} from "utils/utilities";
import { BackingFsConfig } from "./BackingFsConfig";
import {
  getBackingFsConfigAPI,
  updateBackingFsConfigAPI,
} from "./BackingFsConfig.api";
import BoslerInput from "components/InputComponent/BoslerInput";
import BoslerButton from "components/ButtonComponent/BoslerButton";

const { Text, Title } = Typography;
export const BackingFsSettings = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(true);
  const [backingFsConfig, setBackingFsConfig] = useState<BackingFsConfig>();
  const [isUpdateButtonEnabled, setIsUpdateButtonEnabled] = useState(false);

  const getFsBasedEntries = (fsType: string) => {
    if (fsType == "localfs") {
      return (
        <>
          <Row justify="space-between">
            <Col span={8}>
              <Text>Local FS</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="localFs">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
        </>
      );
    } else if (fsType == "hdfs") {
      return (
        <>
          <Row justify="space-between">
            <Col span={8}>
              <Text>Hadoop Distributed File System</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="hdfs">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
        </>
      );
    } else if (fsType == "s3") {
      return (
        <>
          <Row justify="space-between">
            <Col span={8}>
              <Text>S3 Bucket</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="s3Bucket">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
          <Row justify="space-between">
            <Col span={8}>
              <Text>S3 Access Key</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="s3AccessKey">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
          <Row justify="space-between">
            <Col span={8}>
              <Text>S3 Secret Key</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="s3SecretKey">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
        </>
      );
    } else if (fsType == "gs") {
      return (
        <>
          <Row justify="space-between">
            <Col span={8}>
              <Text>GS Bucket</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="gsBucket">
                <BoslerInput />
              </Form.Item>
            </Col>
          </Row>
          <Row justify="space-between">
            <Col span={8}>
              <Text>GS Credentials</Text>
            </Col>
            <Col span={16}>
              <Text type="secondary"></Text>
              <Form.Item name="gsCredentials">
                <TextArea rows={10} />
              </Form.Item>
            </Col>
          </Row>
        </>
      );
    } else return <></>;
  };

  useEffect(() => {
    getBackingFsConfigAPI().then(({ data }) => {
      setBackingFsConfig(data);
      setLoading(false);
    });
  }, []);
  return (
    <div className="settings-center-block">
      {loading ? (
        <BoslerLoader />
      ) : (
        <>
          <Form
            form={form}
            layout="horizontal"
            initialValues={{
              ...backingFsConfig,
              gsCredentials: decodeFromBase64(
                backingFsConfig?.gsCredentials as string
              ),
            }}
            onFinish={(values: any) => {
              updateBackingFsConfigAPI(
                values?.fsType == "gs"
                  ? {
                      ...values,
                      gsCredentials: encodeToBase64(values?.gsCredentials),
                    }
                  : values
              );
              setIsUpdateButtonEnabled(false);
            }}
            onValuesChange={() => setIsUpdateButtonEnabled(true)}
          >
            <Row>
              <Col>
                <Title level={3}>BackingFS</Title>
                <Text type="secondary">
                  {getLanguageLabel("backingFsMessage")}
                </Text>
              </Col>
            </Row>
            <Divider />
            <Row justify="space-between">
              <Col span={8}>
                <Text>File System Type</Text>
              </Col>
              <Col span={16}>
                <Text type="secondary"></Text>
                <Form.Item name="fsType">
                  <Radio.Group
                    onChange={(e) => {
                      setBackingFsConfig((prev: any) => {
                        return { ...prev, fsType: e.target.value };
                      });
                    }}
                    name="fsType"
                  >
                    <Radio value={"localfs"} name="localFS">
                      localFS
                    </Radio>
                    <Radio value={"gs"} name="GS">
                      Google Cloud Storage
                    </Radio>
                    <Radio value={"hdfs"} name="HDFS">
                      Hadoop (HDFS)
                    </Radio>
                    <Radio value={"s3"} name="S3">
                      Amazon S3 / Minio
                    </Radio>
                    <Radio value={""} name="Azure" disabled>
                      Azure Blob Storage
                    </Radio>
                  </Radio.Group>
                </Form.Item>
              </Col>
            </Row>

            {getFsBasedEntries(backingFsConfig?.fsType as string)}

            <Row justify="space-between">
              <Col span={8}>
                <div
                  className="BoslerSubHeader1 text-and-icon-center"
                  style={{ marginRight: "0.5rem" }}
                >
                  Only platform administrators can view or edit this
                  configuration.
                </div>
              </Col>
              <Col span={16}>
                <Form.Item style={{ marginBottom: 0, marginLeft: "auto" }}>
                  <BoslerButton
                    htmlType="submit"
                    intent="primary"
                    disabled={!isUpdateButtonEnabled}
                  >
                    Update Configuration
                  </BoslerButton>
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </>
      )}
    </div>
  );
};
