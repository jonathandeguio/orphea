import {
  Card,
  Col,
  Form,
  FormInstance,
  Input,
  Row,
  Select,
  Switch,
  Tabs,
  Tag,
  Tooltip,
  Typography,
} from "antd";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import React, { useEffect, useState } from "react";
import { generateKey, getLanguageLabel, isDefined } from "utils/utilities";
import DataMartTreeViewer from "./DataMartTreeViewer";
import { useSelector } from "react-redux";
import { TestConnectionButton } from "Apps/Connect/Sources/TestConnection.view";
import { initialSourceDetails } from "Apps/Connect/Sources/Source.constants";
import { Buffer } from "buffer";
import SourceDetails from "Apps/Connect/Sources/Source.view";
import { TestConnectionDataMart } from "./TestConnectionDataMart";
const { Text } = Typography;

interface IProps {
  form: FormInstance;
  outerName: number;
  remove: any;
}

const DataMartItem = ({ form, outerName, remove }: IProps) => {
  const { TabPane } = Tabs;
  const { projects } = useSelector((state) => (state as any).projectList);
  const datamartId =
    form.getFieldValue(["dataMartModels", outerName, "id"]) || undefined;

  return (
    <div>
      <BoslerCollapse
        key={generateKey("dataMart")}
        collapsible={"ICON"}
        defaultCollpased={true}
        header={
          <div className="--flex-row-space-between">
            <div className="--flex-row-space-between">
              <Tag bordered={false} color="processing">
                {outerName + 1}
              </Tag>
              <Form.Item name={[outerName, "name"]}>
                <BoslerInput editText placeholder="Data Mart Name" />
              </Form.Item>
            </div>
            <div className="--flex-row-space-between">
              <Form.Item name={[outerName, "enabled"]} valuePropName="checked">
                <Switch size="small" />
              </Form.Item>
              <BoslerButton
                icon={<TrashIcon />}
                intent="dangerous"
                minimal
                icononly
                onClick={() => {
                  remove(outerName);
                }}
              />
            </div>
          </div>
        }
      >
        <>
          <Tabs defaultActiveKey="1" type="card">
            <TabPane tab="Info" key={"1"}>
              <div>
                <Row gutter={16}>
                  <Col span={16}>Host</Col>
                  <Col span={8}>
                    <Text type="secondary">Host Address like 192.168.1.1</Text>
                    <Form.Item
                      name={[outerName, "server"]}
                      rules={[
                        {
                          required: true,
                          message: "Please enter Host Address!",
                        },
                      ]}
                    >
                      <BoslerInput placeholder="Host" />
                    </Form.Item>
                  </Col>
                </Row>
                <br />
                <Row gutter={16}>
                  <Col span={16}>Port</Col>
                  <Col span={8}>
                    <Text type="secondary">Server Port like 8080,5432</Text>
                    <Form.Item
                      name={[outerName, "port"]}
                      rules={[
                        {
                          required: true,
                          message: "Please input Server Port!",
                        },
                      ]}
                    >
                      <BoslerInput placeholder="Port" />
                    </Form.Item>
                  </Col>
                </Row>
                <br />
                <Row gutter={16}>
                  <Col span={16}>Username</Col>
                  <Col span={8}>
                    <Form.Item
                      name={[outerName, "username"]}
                      rules={[
                        {
                          required: true,
                          message: "Please input your Username!",
                        },
                      ]}
                    >
                      <BoslerInput placeholder="Username" />
                    </Form.Item>
                  </Col>
                </Row>
                <br />
                <Row gutter={16}>
                  <Col span={16}>Password</Col>
                  <Col span={8}>
                    <Form.Item
                      name={[outerName, "password"]}
                      rules={[
                        {
                          required: true,
                          message: "Please input your Password!",
                        },
                      ]}
                    >
                      <Input.Password
                        className="input"
                        placeholder={getLanguageLabel("password")}
                        required
                      />
                    </Form.Item>
                  </Col>
                </Row>
                <br />
                <Row gutter={16}>
                  <Col span={16}>Database</Col>
                  <Col span={8}>
                    <Text type="secondary">DataMart Database Name</Text>
                    <Form.Item
                      name={[outerName, "database"]}
                      rules={[
                        {
                          required: true,
                          message: "Please input your DataMart Database Name!",
                        },
                      ]}
                    >
                      <BoslerInput placeholder="database" />
                    </Form.Item>
                  </Col>
                </Row>

                <Row gutter={16}>
                  <Col span={16}>Maximum Rows</Col>
                  <Col span={8}>
                    <Tooltip title="Select a number of rows in range">
                      <Text type="secondary">
                        Any Number or -1 for Unlimited
                      </Text>
                      <Form.Item
                        name={[outerName, "limitRows"]}
                        rules={[
                          {
                            required: true,
                            message: "Please input maximum number of rows!",
                          },
                        ]}
                      >
                        <BoslerInput />
                      </Form.Item>
                    </Tooltip>
                  </Col>
                </Row>
                <br />
                <Row gutter={16}>
                  <Col span={16}>Maximum Size</Col>
                  <Col span={8}>
                    <Tooltip title="Select maximum size in range">
                      <Text type="secondary">
                        Any Number or -1 for Unlimited
                      </Text>
                      <Form.Item
                        name={[outerName, "limitSize"]}
                        rules={[
                          {
                            required: true,
                            message: "Please input Maximum Size!",
                          },
                        ]}
                      >
                        <BoslerInput />
                      </Form.Item>
                    </Tooltip>

                    <br />
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={16}>Enabled Projects</Col>
                  <Col span={8}>
                    <Text type="secondary">
                      Projects enabled to use Datamart
                    </Text>
                    <Form.Item name={[outerName, "projects"]}>
                      <Select
                        mode="multiple"
                        placeholder="Select Project"
                        optionFilterProp="children"
                        style={{ width: "100%" }}
                      >
                        {isDefined(projects) &&
                          projects.map((proj: any) => {
                            return (
                              <Select.Option value={proj.id}>
                                {proj.name}
                              </Select.Option>
                            );
                          })}
                      </Select>
                    </Form.Item>
                    <br />
                  </Col>
                </Row>
                <Row gutter={16}>
                  <Col span={16}>
                    <TestConnectionDataMart form={form} outerName={outerName} />
                  </Col>
                </Row>
              </div>
            </TabPane>
            {datamartId && (
              <TabPane tab="Data Browser" key={"2"}>
                <DataMartTreeViewer dataMartId={datamartId} />
              </TabPane>
            )}
          </Tabs>
        </>
      </BoslerCollapse>
    </div>
  );
};

export default DataMartItem;
