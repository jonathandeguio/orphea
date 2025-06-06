import { Button, Divider, Form, Input, Select } from "antd";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import React from "react";
import { isDefined } from "utils/utilities";
import { initialDomain } from "./RestAPIConnector.constants";
import styles from "./RestAPIConnector.module.scss";
import { IRestDomain, TRestAuthTypeEnum } from "./RestAPIConnector.types";

interface TProps {
  newSourceDetails: any;
  setNewSourceDetails: any;
}

const RestAPIConnector = ({
  newSourceDetails,
  setNewSourceDetails,
}: TProps) => {
  const [form] = Form.useForm();

  const fieldsToValidate = (authType: TRestAuthTypeEnum) => {
    const fields = ["domain", "port", "authType"];
    if (authType == TRestAuthTypeEnum.APIKEY) {
      fields.concat(["apiKeyName", "apiKeyValue"]);
    } else if (authType == TRestAuthTypeEnum.BEARERTOKEN) {
      fields.concat(["bearerToken"]);
    }
    return fields;
  };

  const validateDomain = (domains: IRestDomain[]) => {
    let isValid = true;
    domains.map((domain: IRestDomain) => {
      fieldsToValidate(domain.authType).forEach((field) => {
        if (
          !(field in domain) ||
          !isDefined((domain as any)[field]) ||
          (domain as any)[field] == ""
        ) {
          isValid = false;
        }
      });
    });

    return isValid;
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{
        domains: newSourceDetails?.domains
          ? newSourceDetails.domains
          : [initialDomain],
      }}
      onValuesChange={() => {
        console.log(form.getFieldsValue()["domains"]);
        if (validateDomain(form.getFieldsValue()["domains"])) {
          setNewSourceDetails({
            ...newSourceDetails,
            domains: form.getFieldsValue()["domains"],
          });
        } else {
          setNewSourceDetails({
            ...newSourceDetails,
            domains: null,
          });
        }
      }}
    >
      <Form.List name="domains">
        {(fields, { add, remove }) => (
          <>
            {fields.map((field, name, fieldKey, ...restField) => (
              <div key={field.key}>
                <div className={styles.connector_top}>
                  <BoslerButton intent="dangerous" onClick={() => remove(name)}>
                    Remove
                  </BoslerButton>
                </div>
                <Divider />
                <Form.Item label="Protocol" name={[field.name, "protocol"]}>
                  <Select>
                    <Select.Option value={"https"}>{"HTTPS"}</Select.Option>
                    <Select.Option value={"http"}>{"HTTP"}</Select.Option>
                  </Select>
                </Form.Item>
                <Form.Item
                  label="Base URL"
                  name={[field.name, "domain"]}
                  rules={[{ required: true, message: "Please input the URL!" }]}
                >
                  <Input />
                </Form.Item>

                <Form.Item
                  label="Authentication"
                  name={[field.name, "authType"]}
                  rules={[
                    { required: true, message: "Please select an auth type!" },
                  ]}
                >
                  <Select
                  // onChange={(value) =>
                  //   updateDomains(field.name, {
                  //     authType: value as IRestDomain["authType"],
                  //   })
                  // }
                  >
                    <Select.Option value={TRestAuthTypeEnum.NONE}>
                      {"None"}
                    </Select.Option>
                    <Select.Option value={TRestAuthTypeEnum.BEARERTOKEN}>
                      {"Bearer Token"}
                    </Select.Option>
                    <Select.Option value={TRestAuthTypeEnum.APIKEY}>
                      {"API Key"}
                    </Select.Option>
                  </Select>
                </Form.Item>

                {form.getFieldValue(["domains", field.name, "authType"]) ===
                  TRestAuthTypeEnum.BEARERTOKEN && (
                  <Form.Item
                    label="Bearer Token"
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
                  TRestAuthTypeEnum.APIKEY && (
                  <>
                    <Form.Item
                      label="API Key Name"
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
                      label="API Key Value"
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

                <Form.Item
                  label="Port"
                  name={[field.name, "port"]}
                  rules={[
                    {
                      required: true,
                    },
                  ]}
                >
                  <Input type="number" />
                </Form.Item>
                <Divider />
              </div>
            ))}
            <Form.Item>
              <Button type="dashed" onClick={() => add(initialDomain)}>
                Add new domain
              </Button>
            </Form.Item>
          </>
        )}
      </Form.List>
    </Form>
  );
};

export default RestAPIConnector;
