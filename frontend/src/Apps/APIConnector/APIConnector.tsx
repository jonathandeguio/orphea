import {
  IRestDomain,
  TRestAuthTypeEnum,
} from "Apps/Connect/Sources/RestAPIConnector/RestAPIConnector.types";
import { ISourceConfig } from "Apps/Connect/Sources/Source";
import { RestAPIBodyTypeEnum } from "Apps/Connect/Webhook/Webhook.types";
import {
  Col,
  Descriptions,
  Form,
  FormInstance,
  Input,
  Popconfirm,
  Row,
  Select,
  Tabs,
  Tag,
  Tooltip
} from "antd";
import TextArea from "antd/es/input/TextArea";
import { SearchEmptyState } from "assets/Illustrations/EmptyState";
import {
  CrossIcon,
  RemoveIcon
} from "assets/icons/boslerActionIcons";
import { CopyIcon } from "assets/icons/boslerEditorIcons";
import { TrashIcon } from "assets/icons/boslerMiscellaneousIcons";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import NoData from "components/CommonUI/NoData";
import React, { useState } from "react";
import { getLanguageLabel } from "utils/utilities";
import styles from "./APIConnector.module.scss";
import CoypAsCurl from "./CopyAsCurl";
import { MappingTypeOverlay } from "./MappingTypeOverlay";
interface IProps {
  form: FormInstance;
  apiIndex: number;
  source: ISourceConfig;
  outerName: number;
  removeRequest: (request: number) => void;
}

interface IAddParamBtnProps {
  fields: any[];
  add: () => void;
  text: string;
}

const AddParamBtn = ({ fields, add, text }: IAddParamBtnProps) => {
  return (
    <>
      {!fields || fields.length == 0 ? (
        <NoData
          subHeading={<BoslerButton onClick={() => add()}>{text}</BoslerButton>}
          icon={<SearchEmptyState />}
        />
      ) : (
        <BoslerButton onClick={() => add()}>{text}</BoslerButton>
      )}
    </>
  );
};

const APIConnector = ({
  apiIndex,
  source,
  outerName,
  removeRequest,
  form,
}: IProps) => {
  const { Option } = Select;
  const { TabPane } = Tabs;
  const [isHovered, setIsHovered] = useState<boolean>(false);
  const [isOverlayVisible, setOverlayVisible] = useState(false);
  const [overlayFieldValue, setOverlayFieldValue] = useState<any>();
  const [overlayAtPos, setOverlayAtPos] = useState();
  const [hoveredRowIndex, setHoveredRowIndex] = useState(null);

  const [overlayPosition, setOverlayPosition] = useState({ top: 0, left: 0 });

  const handleMouseEnter = (e: any) => {
    setIsHovered(true);
  };

  const handleMouseLeave = (e: any) => {
    setIsHovered(false);
  };

  const removeAuthEntry = (
    index: number,
    remove: {
      (index: number | number[]): void;
      (index: number | number[]): void;
      (index: number | number[]): void;
      (arg0: any): void;
    }
  ) => {
    remove(index);
  };

  const bodyType = Form.useWatch(["requests", outerName, "bodyType"], form);
  const domain = source.domains?.filter(
    (domain) =>
      domain.id == form.getFieldValue(["requests", outerName, "domainId"])
  );
  const selectedDomain: IRestDomain | undefined =
    domain && domain.length > 0 ? domain[0] : undefined;

  const handleKeyDown = (event: any, currentValue: any) => {
    if (event.key === "@") {
      const { top, left } = event.currentTarget.getBoundingClientRect();
      const selectionStart = event.currentTarget.selectionStart ?? 0;
      setOverlayPosition({
        top: top + window.scrollY - 50,
        left: left + window.scrollX + selectionStart * 8 - 15,
      });

      setOverlayVisible(true);
      setOverlayFieldValue(currentValue);
      setOverlayAtPos(selectionStart);
    }
  };
  const handleKeyDownTextArea2 = (event: any, currentValue: any, pos: any) => {
    if (event.nativeEvent.data === "@") {
      const { top, left } = event.nativeEvent.target.getBoundingClientRect();
      setOverlayPosition({
        top: top + window.scrollY - 50,
        left: left + window.scrollX + pos * 8 - 15,
      });

      setOverlayVisible(true);
      setOverlayFieldValue(currentValue);
      setOverlayAtPos(pos);
    }
  };

  const handleKeyDownTextArea = (event: any, currentValue: any) => {
    const selection = window.getSelection();
    const range = selection?.getRangeAt(0);
    console.log("event", event, range?.startOffset);

    if (event.key === "@") {
      const { top, left } = event.currentTarget.getBoundingClientRect();
      const selectionStart: any = range?.startOffset ?? 0;
      setOverlayPosition({
        top: top + window.scrollY - 50,
        left: left + window.scrollX + selectionStart * 8 - 15,
      });

      setOverlayVisible(true);
      setOverlayFieldValue(currentValue);
      setOverlayAtPos(selectionStart);
    }
  };
  const [confirmDelete, setConfirmDelete] = useState(false);
  return (
    <>
      <div
        className={styles.requestContainer}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        <BoslerCollapse
          key={apiIndex.toString()}
          collapsible={"ICON"}
          defaultCollpased={false}
          header={
            <>
              <Row justify={"space-between"}>
                <Col span={22}>
                  <div
                    // className="--flex-row-items-center"
                    style={{ display: "flex", alignItems: "center" }}
                  >
                    <Tag bordered={false} color="processing">
                      {apiIndex + 1}
                    </Tag>
                    <Tooltip title={getLanguageLabel("clickToRename")}>
                      <Form.Item name={[outerName, "apiTitle"]}>
                        <BoslerInput
                          className={`${styles.apiTitle} editText`}
                          placeholder={getLanguageLabel("addApiTitle")}
                          variant={"borderless"}
                          // dynamicWidth
                          editText
                          // className="editText"
                          // debounceInterval={5000}
                        />
                      </Form.Item>
                    </Tooltip>
                  </div>
                </Col>
                <Col>
                  {isHovered && (
                    <div style={{ display: "flex" }}>
                      <Tooltip title="Copy as cURL">
                        <BoslerButton
                          icononly
                          size="middle"
                          icon={<CopyIcon />}
                          onClick={(event: { stopPropagation: () => void }) => {
                            event.stopPropagation();
                            CoypAsCurl({ form, outerName, source });
                          }}
                          minimal
                          trimicononlypadding
                        />
                      </Tooltip>
                      <Tooltip title="Remove panel">
                        <Popconfirm
                          placement="left"
                          title={
                            <div
                            // style={{ paddingTop: "20px" }}
                            >
                              <div>
                                {getLanguageLabel("areYouSureYouWantToDelete")}{" "}
                                <span style={{ fontWeight: 800 }}>
                                  {form.getFieldValue([
                                    "requests",
                                    outerName,
                                    "apiTitle",
                                  ])}
                                </span>{" "}
                                ?
                              </div>
                              <div
                                className="--flex"
                                style={{
                                  display: "flex",
                                  marginTop: "10px",
                                  justifyContent: "space-evenly",
                                }}
                              >
                                <BoslerButton
                                  icon={<TrashIcon />}
                                  intent="dangerous"
                                  onClick={() => {
                                    setConfirmDelete(false);
                                    removeRequest(outerName);
                                  }}
                                  key="submit"
                                  textTransform="capitalize"
                                >
                                  {getLanguageLabel("delete")}
                                </BoslerButton>
                                <BoslerButton
                                  icon={<CrossIcon />}
                                  intent="none"
                                  onClick={() => {
                                    setConfirmDelete(false);
                                  }}
                                >
                                  {getLanguageLabel("cancel")}
                                </BoslerButton>
                              </div>
                            </div>
                          }
                          open={confirmDelete}
                          onOpenChange={(visible) => setConfirmDelete(visible)}
                          okButtonProps={{ style: { display: "none" } }}
                          cancelButtonProps={{ style: { display: "none" } }}
                        >
                          <BoslerButton
                            icon={<TrashIcon />}
                            size="middle"
                            icononly
                            intent="dangerous"
                            minimal
                            onClick={() => {
                              setConfirmDelete(true); // Show the Popconfirm
                            }}
                            trimicononlypadding
                          />
                        </Popconfirm>
                      </Tooltip>
                    </div>
                  )}
                </Col>
              </Row>
            </>
          }
        >
          <>
            <div className={styles.requestUrl}>
              <Form.Item name={[outerName, "method"]}>
                <Select className={styles.requestURLMethods}>
                  <Option value="GET">
                    <span
                      style={{
                        color: "var(--SUCCESS_COLOR)",
                      }}
                    >
                      GET
                    </span>
                  </Option>
                  <Option value="POST">
                    <span
                      style={{
                        color: "var(--WARNING_COLOR)",
                      }}
                    >
                      POST
                    </span>
                  </Option>
                  {/* <Option value="PUT">
                    <span
                      style={{
                        color: "var(--PRIMARY_COLOR)",
                      }}
                    >
                      PUT
                    </span>
                  </Option>
                  <Option value="DELETE">
                    <span
                      style={{
                        color: "var(--DANGEROUS_COLOR)",
                      }}
                    >
                      DELETE
                    </span>
                  </Option> */}
                </Select>
              </Form.Item>
              <Form.Item name={[outerName, "domainId"]}>
                <Select style={{ width: "200px", height: "30px" }}>
                  {source?.domains?.map((domain, index) => (
                    <Option key={index} value={domain.id}>
                      {domain.domain}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
              <Form.Item name={[outerName, "path"]} style={{ flex: 1 }}>
                <BoslerInput placeholder={getLanguageLabel("inputApiPath")} />
              </Form.Item>
            </div>

            <Tabs defaultActiveKey="1" type="card" style={{ margin: "6px" }}>
              <TabPane tab={getLanguageLabel("queryParams")} key="1">
                <Form.List name={[outerName, "queryParams"]}>
                  {(fields, { add, remove }) => (
                    <>
                      {fields.map(({ key, name, ...restField }, index: any) => (
                        <Row
                          gutter={4}
                          key={key}
                          justify={"space-between"}
                          onMouseEnter={() => setHoveredRowIndex(index)}
                          onMouseLeave={() => setHoveredRowIndex(null)}
                        >
                          <Col span={8}>
                            <Form.Item
                              {...restField}
                              name={[name, "key"]}
                              rules={[
                                {
                                  required: true,
                                  message: getLanguageLabel("keyRequired"),
                                },
                              ]}
                            >
                              <BoslerInput
                                placeholder={getLanguageLabel("key")}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={14}>
                            <Form.Item
                              {...restField}
                              name={[name, "value"]}
                              rules={[
                                {
                                  required: true,
                                  message: getLanguageLabel("valueRequired"),
                                },
                              ]}
                              // valuePropName="value"
                              // trigger="onChange"
                            >
                              <BoslerInput
                                // value={value}
                                // readOnlyDisplayValue={readOnlyDisplayValue} // this is different for all the enteries
                                onKeyDown={(e) =>
                                  handleKeyDown(e, [
                                    "requests",
                                    outerName,
                                    "queryParams",
                                    name,
                                    "value",
                                  ])
                                }
                                // onChange={handleChange}
                                placeholder={getLanguageLabel(
                                  "useAtToReferenceParameters"
                                )}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={2}>
                            {isHovered && hoveredRowIndex === index && (
                              <>
                                <BoslerButton
                                  icononly
                                  icon={<RemoveIcon />}
                                  onClick={() => removeAuthEntry(index, remove)}
                                  minimal
                                  trimicononlypadding
                                />
                              </>
                            )}
                          </Col>
                        </Row>
                      ))}
                      <AddParamBtn
                        fields={fields}
                        add={add}
                        text={getLanguageLabel("addQueryParams")}
                      />
                    </>
                  )}
                </Form.List>
              </TabPane>

              <TabPane tab={getLanguageLabel("headers")} key="2">
                <Form.List name={[outerName, "headers"]}>
                  {(fields, { add, remove }) => (
                    <>
                      {fields.map(({ key, name, ...restField }, index: any) => (
                        <Row
                          gutter={4}
                          key={key}
                          justify={"space-between"}
                          onMouseEnter={() => setHoveredRowIndex(index)}
                        >
                          <Col span={8}>
                            <Form.Item
                              {...restField}
                              name={[name, "key"]}
                              rules={[
                                {
                                  required: true,
                                  message: getLanguageLabel("keyRequired"),
                                },
                              ]}
                            >
                              <BoslerInput
                                placeholder={`${getLanguageLabel(
                                  "header"
                                )} ${getLanguageLabel("key")}`}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={14}>
                            <Form.Item
                              {...restField}
                              name={[name, "value"]}
                              rules={[
                                {
                                  required: true,
                                  message: getLanguageLabel("valueRequired"),
                                },
                              ]}
                            >
                              <BoslerInput
                                // value={value}
                                // readOnlyDisplayValue={readOnlyDisplayValue}
                                onKeyDown={(e) =>
                                  handleKeyDown(e, [
                                    "requests",
                                    outerName,
                                    "headers",
                                    name,
                                    "value",
                                  ])
                                }
                                // onChange={handleChange}
                                placeholder={getLanguageLabel(
                                  "useAtToReferenceParameters"
                                )}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={2}>
                            {isHovered && hoveredRowIndex === index && (
                              <BoslerButton
                                minimal
                                icononly
                                icon={<RemoveIcon />}
                                onClick={() => removeAuthEntry(index, remove)}
                                trimicononlypadding
                              />
                            )}
                          </Col>
                        </Row>
                      ))}
                      <AddParamBtn
                        fields={fields}
                        add={add}
                        text={getLanguageLabel("addHeader")}
                      />
                    </>
                  )}
                </Form.List>
              </TabPane>

              <TabPane tab={getLanguageLabel("body")} key="3">
                <Form.Item name={[outerName, "bodyType"]}>
                  <Select
                    onChange={(value) => {
                      form.setFieldsValue({ [outerName]: { bodyType: value } });
                    }}
                  >
                    <Option value={RestAPIBodyTypeEnum.NONE}>
                      {getLanguageLabel("none")}
                    </Option>
                    <Option value={RestAPIBodyTypeEnum.FORMDATA}>
                      {getLanguageLabel("formData")}
                    </Option>
                    <Option value={RestAPIBodyTypeEnum.RAW}>
                      {getLanguageLabel("raw")}
                    </Option>
                    <Option value={RestAPIBodyTypeEnum.JSON}>{"JSON"}</Option>
                  </Select>
                </Form.Item>
                {bodyType === RestAPIBodyTypeEnum.FORMDATA && (
                  <Form.List name={[outerName, "formData"]}>
                    {(fields, { add, remove }) => (
                      <>
                        {fields.map(
                          ({ key, name, ...restField }, index: any) => (
                            <Row
                              gutter={4}
                              key={key}
                              justify={"space-between"}
                              onMouseEnter={() => setHoveredRowIndex(index)}
                            >
                              <Col span={8}>
                                <Form.Item
                                  {...restField}
                                  name={[name, "key"]}
                                  rules={[
                                    {
                                      required: true,
                                      message: getLanguageLabel("keyRequired"),
                                    },
                                  ]}
                                >
                                  <BoslerInput
                                    placeholder={getLanguageLabel("key")}
                                  />
                                </Form.Item>
                              </Col>
                              <Col span={14}>
                                <Form.Item
                                  {...restField}
                                  name={[name, "value"]}
                                  rules={[
                                    {
                                      required: true,
                                      message:
                                        getLanguageLabel("valueRequired"),
                                    },
                                  ]}
                                >
                                  <BoslerInput
                                    placeholder={getLanguageLabel(
                                      "useAtToReferenceParameters"
                                    )}
                                    onKeyDown={(e) =>
                                      handleKeyDown(e, [
                                        "requests",
                                        outerName,
                                        "formData",
                                        name,
                                        "value",
                                      ])
                                    }
                                  />
                                </Form.Item>
                              </Col>
                              <Col span={2}>
                                {isHovered && hoveredRowIndex === index && (
                                  <BoslerButton
                                    icononly
                                    icon={<RemoveIcon />}
                                    onClick={() =>
                                      removeAuthEntry(index, remove)
                                    }
                                    minimal
                                    trimicononlypadding
                                  />
                                )}
                              </Col>
                            </Row>
                          )
                        )}
                        <AddParamBtn
                          fields={fields}
                          add={add}
                          text={getLanguageLabel("addFormData")}
                        />
                      </>
                    )}
                  </Form.List>
                )}

                {(bodyType === RestAPIBodyTypeEnum.RAW ||
                  bodyType === RestAPIBodyTypeEnum.JSON) && (
                  <Form.Item name={[outerName, "rawBody"]}>
                    <TextArea
                      placeholder={getLanguageLabel("enterRawBodyContent")}
                      rows={4}
                      onKeyDown={(e) =>
                        handleKeyDownTextArea(e, [
                          "requests",
                          outerName,
                          "rawBody",
                        ])
                      }
                    />
                  </Form.Item>
                )}
              </TabPane>
              {selectedDomain && (
                <TabPane tab={getLanguageLabel("authorization")} key="4">
                  <Descriptions bordered>
                    <Descriptions.Item
                      label={getLanguageLabel("authType")}
                      span={2}
                    >
                      {selectedDomain.authType}
                    </Descriptions.Item>
                    {selectedDomain.authType ==
                      TRestAuthTypeEnum.BEARERTOKEN && (
                      <Descriptions.Item label={"Bearer Token"} span={2}>
                        <Input.Password
                          value={selectedDomain.bearerToken}
                          readOnly
                        ></Input.Password>
                      </Descriptions.Item>
                    )}
                    {selectedDomain.authType == TRestAuthTypeEnum.APIKEY && (
                      <>
                        <Descriptions.Item label={"API Key"} span={2}>
                          {selectedDomain.apiKeyName}
                        </Descriptions.Item>
                        <Descriptions.Item label={"API Key Value"} span={2}>
                          {selectedDomain.apiKeyValue}
                        </Descriptions.Item>
                      </>
                    )}
                  </Descriptions>
                </TabPane>
              )}
            </Tabs>

            {isOverlayVisible && (
              <MappingTypeOverlay
                form={form}
                position={overlayPosition}
                overlayFieldValue={overlayFieldValue}
                overlayAtPos={overlayAtPos}
                setOverlayVisible={setOverlayVisible}
              />
            )}
          </>
        </BoslerCollapse>
      </div>
    </>
  );
};

export default APIConnector;
