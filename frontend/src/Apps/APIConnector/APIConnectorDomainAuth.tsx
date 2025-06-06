import { Button, Divider, Form, Input, Select } from "antd";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useEffect, useState } from "react";
import styles from "./APIConnector.module.scss";
import { getLanguage } from "redux/actions/languageActions";
import { getLanguageLabel } from "utils/utilities";

interface Domain {
  id?: string | undefined;
  protocol: "https" | "http";
  url: string;
  authType: "None" | "API Key" | "Bearer Token";
  bearerToken?: string;
  apiKeyName?: string;
  apiKeyValue?: string;
  port: number;
}

const initialDomain: Domain = {
  protocol: "https",
  url: "",
  authType: "None",
  port: 443,
};

const APIConnectorDomainAuth: React.FC = () => {
  const [form] = Form.useForm();
  const [isFormValid, setIsFormValid] = useState(false);

  useEffect(() => {
    form
      .validateFields()
      .then(() => setIsFormValid(true))
      .catch(() => setIsFormValid(false));
  }, [form]);

  const updateDomains = (index: number, updatedData: Partial<Domain>) => {
    const domains = form.getFieldValue("domains") || [];
    const updatedDomains = domains.map((domain: Domain, i: number) =>
      i === index ? { ...domain, ...updatedData } : domain
    );
    form.setFieldsValue({ domains: updatedDomains });
  };

  const handleAddDomain = () => {
    const domains = form.getFieldValue("domains") || [];
    form.setFieldsValue({ domains: [...domains, initialDomain] });
  };

  const handleRemoveDomain = (index: number) => {
    const domains = form.getFieldValue("domains") || [];
    form.setFieldsValue({
      domains: domains.filter((_: any, i: number) => i !== index),
    });
  };

  const handleSaveAndContinue = () => {
    const domains = form.getFieldValue("domains").map((domain: Domain) => {
      const {
        protocol,
        url,
        port,
        authType,
        bearerToken,
        apiKeyName,
        apiKeyValue,
      } = domain;
      const filteredDomain: Domain = { protocol, url, port, authType };

      if (authType === "Bearer Token") {
        filteredDomain.bearerToken = bearerToken;
      }
      if (authType === "API Key") {
        filteredDomain.apiKeyName = apiKeyName;
        filteredDomain.apiKeyValue = apiKeyValue;
      }

      return filteredDomain;
    });
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{ domains: [initialDomain] }}
      onValuesChange={() =>
        form
          .validateFields()
          .then(() => setIsFormValid(true))
          .catch(() => setIsFormValid(false))
      }
    >
      <Form.List name="domains">
        {(fields) =>
          fields.map((field) => (
            <div key={field.key}>
              <div className={styles.connector_top}>
                <div>
                  <span>
                    {form.getFieldValue(["domains", field.name, "url"])}
                  </span>
                  <span className={styles.connector_top_authtype}>
                    {form.getFieldValue(["domains", field.name, "authType"])}
                  </span>
                </div>
                <BoslerButton
                  intent="dangerous"
                  onClick={() => handleRemoveDomain(field.name)}
                >
                  {getLanguageLabel("remove")}
                </BoslerButton>
              </div>
              <Divider />

              <Form.Item name={[field.name, "protocol"]}>
                <Select>
                  <Select.Option value="https">HTTPS</Select.Option>
                  <Select.Option value="http">HTTP</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item
                label="Domain base URL"
                name={[field.name, "url"]}
                rules={[{ required: true, message: "Please input the URL!" }]}
              >
                <BoslerInput />
              </Form.Item>

              <Form.Item
                label="Authentication"
                name={[field.name, "authType"]}
                rules={[
                  { required: true, message: "Please select an auth type!" },
                ]}
              >
                <Select
                  onChange={(value) =>
                    updateDomains(field.name, {
                      authType: value as Domain["authType"],
                    })
                  }
                >
                  <Select.Option value="None">None</Select.Option>
                  <Select.Option value="API Key">API Key</Select.Option>
                  <Select.Option value="Bearer Token">
                    Bearer Token
                  </Select.Option>
                </Select>
              </Form.Item>

              {form.getFieldValue(["domains", field.name, "authType"]) ===
                "Bearer Token" && (
                <Form.Item
                  label="Bearer Token (required)"
                  name={[field.name, "bearerToken"]}
                  rules={[
                    {
                      required: true,
                      message: "Please input your Bearer Token!",
                    },
                  ]}
                >
                  <Input.Password />
                </Form.Item>
              )}

              {form.getFieldValue(["domains", field.name, "authType"]) ===
                "API Key" && (
                <>
                  <Form.Item
                    label="API Key Name (required)"
                    name={[field.name, "apiKeyName"]}
                    rules={[
                      {
                        required: true,
                        message: "Please input your API Key Name!",
                      },
                    ]}
                  >
                    <Input placeholder="Enter API Key Name" />
                  </Form.Item>

                  <Form.Item
                    label="API Key Value (required)"
                    name={[field.name, "apiKeyValue"]}
                    rules={[
                      {
                        required: true,
                        message: "Please input your API Key Value!",
                      },
                    ]}
                  >
                    <Input.Password placeholder="Enter API Key Value" />
                  </Form.Item>
                </>
              )}

              <Form.Item label="Port (optional)" name={[field.name, "port"]}>
                <Input type="number" />
              </Form.Item>
              <Divider />
            </div>
          ))
        }
      </Form.List>

      <Form.Item>
        <Button type="dashed" onClick={handleAddDomain}>
          {getLanguageLabel("addNewDomain")}
        </Button>
      </Form.Item>

      <Form.Item>
        <Button
          type="primary"
          onClick={handleSaveAndContinue}
          disabled={!isFormValid}
        >
          {getLanguageLabel("saveAndContinue")}
        </Button>
      </Form.Item>
    </Form>
  );
};

export default APIConnectorDomainAuth;
