import { Col, Form, Row, Typography } from "antd";
import TextArea from "antd/es/input/TextArea";
import { LinkIcon } from "assets/icons/boslerActionIcons";
import { CodeCellIcon, CopyIcon } from "assets/icons/boslerEditorIcons";
import { KeyIcon } from "assets/icons/boslerInterfaceIcons";
import {
  LibraryIcon,
  LightBulbIcon,
} from "assets/icons/boslerMiscellaneousIcons";
import { ArrowRightIcon } from "assets/icons/boslerNavigationIcon";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import BoslerButton from "components/BoslerComponents/ButtonComponent/BoslerButton";
import BoslerModal from "components/CommonUI/BoslerModalContainer";
import React from "react";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { RootState } from "redux/types/store";
import {
  copyToClipboard,
  decodeFromBase64,
  encodeToBase64,
  getLanguageLabel,
  openNotification,
} from "utils/utilities";
const { Text } = Typography;
interface TProps {
  openEmbedModal: boolean;
  setOpenEmbedModal: React.Dispatch<React.SetStateAction<boolean>>;
}
const EmbedModal = ({ openEmbedModal, setOpenEmbedModal }: TProps) => {
  const PUBLIC_URL = window.location.origin;
  const [form] = Form.useForm();
  const { query } = useSelector((state) => (state as RootState)?.kepler);

  const exampleFilter1 = [
    {
      columnName: "ORDER_STATUS",
      columnType: "string",
      filters: [
        {
          key: "Condition_1715072295401",
          operator: "equal",
          value: "completed",
        },
        {
          key: "Condition_1715072330108",
          operator: "equal",
          value: "cancelled",
        },
      ],
      key: "filter_1715072328437",
      logicalOperator: "OR",
    },
  ];

  const exampleFilter2 = [
    {
      columnName: "ORDER_STATUS",
      columnType: "string",
      filters: [
        {
          key: "Condition_1715072295401",
          operator: "equal",
          value: "completed",
        },
        {
          key: "Condition_1715072330108",
          operator: "equal",
          value: "cancelled",
        },
      ],
      key: "filter_1715072328437",
      logicalOperator: "OR",
    },
    {
      columnName: "CATEGORY",
      columnType: "string",
      filters: [
        {
          key: "Condition_1715072509438",
          operator: "equal",
          value: "Dumplings",
        },
      ],
      key: "filter_1715072506847",
      logicalOperator: "AND",
    },
  ];

  const copyMenuItems = (isLink: boolean) => {
    return [
      {
        label: (
          <>
            <Row justify="space-between" align="middle">
              <Col>
                <div className="text-and-icon-center">Without Filters</div>
              </Col>
            </Row>
          </>
        ),
        key: "withoutFilters",

        onClick: (e: any) => {
          e.domEvent.stopPropagation();
          handleCopy(isLink, false);
        },
      },
      {
        label: (
          <>
            <Row justify="space-between" align="middle">
              <Col>
                <div className="text-and-icon-center">With Filters</div>
              </Col>
            </Row>
          </>
        ),
        key: "withFilters",

        onClick: (e: any) => {
          e.domEvent.stopPropagation();
          handleCopy(isLink, true);
        },
      },
    ];
  };

  const handleCopy = (isLink: boolean, withFilters = false) => {
    let link = window.location.href + "?embedded=true";

    if (withFilters)
      link += `&filter=${encodeToBase64(JSON.stringify(query.filter))}`;

    let text = link;
    if (!isLink)
      text = `
<iframe width="800" height="600" frameborder="0" src="${link}"></iframe>
    `;

    copyToClipboard(text);

    setOpenEmbedModal(false);
    openNotification(
      getLanguageLabel(isLink ? "linkCopied" : "codeCopied"),
      "",
      "info"
    );
  };

  return (
    <>
      {openEmbedModal && (
        <Form
          form={form}
          onFinish={() => {
            form.setFieldValue(
              "encodedFilters",
              encodeToBase64(form.getFieldValue("filters"))
            );
          }}
        >
          <BoslerModal
            open={openEmbedModal}
            onCancel={() => {
              setOpenEmbedModal(false);
              form.resetFields();
            }}
            headingIcon={<LinkIcon />}
            heading={getLanguageLabel("embedResourceExternal")}
            width={"70vw"}
            information={
              <div>
                <div style={{ padding: "20px" }}>
                  <div className="text-and-icon-align">
                    <LightBulbIcon />
                    <Text strong>{getLanguageLabel("filterUsage")}</Text>
                  </div>
                  <div style={{ paddingTop: "5px", paddingLeft: "20px" }}>
                    {getLanguageLabel("filterUsageExplanation")}
                    <br />
                    <BoslerCollapse
                      key={`embed_url`}
                      collapsible="HEADER"
                      header={getLanguageLabel("url")}
                    >
                      <>
                        URL to use : {PUBLIC_URL}
                        /portal/kepler/CHART/ac276595-a61d-4290-ab7f-7558f4692a52?embedded=true&filter=W3siZmllbGQiOnsibmFtZSI6Ik9SREVSX0lURU1fSUQiLCJ0eXBlIjoic3RyaW5nIiwidmFsdWUiOiJPUkRFUl9JVEVNX0lEIiwiZGF0YXNldElkIjoiMTI0OGYyZjAtYzliYS00YzIzLWIzODctYTExMzFjMzFkM2E1In0sImNvbmRpdGlvbkNhc2UiOlt7ImtleSI6ImNvbmRpdGlvbl8xNzA4MjgxMTQzNzc5Iiwib3BlcmF0b3IiOiJlcXVhbCIsInZhbHVlIjoiODcyIn1dLCJrZXkiOiJmaWx0ZXJfMTcwODI4MTE0Mzc3OSIsImxvZ2ljYWxPcGVyYXRvciI6IkFORCJ9XQ==
                      </>
                    </BoslerCollapse>
                    <BoslerCollapse
                      key={`example_filter`}
                      collapsible="HEADER"
                      header={getLanguageLabel("example")}
                    >
                      <>
                        <BoslerCollapse
                          key={`example_filter1`}
                          collapsible="HEADER"
                          header={getLanguageLabel("filter") + " 1"}
                        >
                          <>
                            <pre style={{ fontSize: "0.5rem" }}>
                              {JSON.stringify(exampleFilter1, null, 2)}
                            </pre>
                          </>
                        </BoslerCollapse>

                        <BoslerCollapse
                          key={`example_filter2`}
                          collapsible="HEADER"
                          header={getLanguageLabel("filter") + " 2"}
                        >
                          <>
                            <pre style={{ fontSize: "0.5rem" }}>
                              {JSON.stringify(exampleFilter2, null, 2)}
                            </pre>
                          </>
                        </BoslerCollapse>
                      </>
                    </BoslerCollapse>
                  </div>
                </div>
                <div style={{ padding: "20px" }}>
                  <div className="text-and-icon-align">
                    <LibraryIcon />
                    <Text strong>{getLanguageLabel("learn")}</Text>
                  </div>
                  <div style={{ paddingTop: "10px", paddingLeft: "20px" }}>
                    <Link to="/learn/">DataConnection Guidelines</Link>
                    <br />
                    <Link to="/learn/">Best Practices</Link>
                    <br />
                    <Link to="/learn/">Data Transfer Security</Link>
                    <br />
                    <Link to="/learn/">Governance Policy and Guidelines</Link>
                  </div>
                </div>
              </div>
            }
          >
            <div
              style={{
                display: "flex",
                flexDirection: "row",
                gap: "20px",
                justifyContent: "center",
                alignItems: "center",
              }}
            >
              <div
                style={{
                  flex: 1,
                  display: "flex",
                  justifyContent: "center",
                  alignItems: "center",
                  flexDirection: "column",
                  background: "var(--background-color)",
                  padding: "20px",
                  border: "1px solid var(--bosler-border-color-default)",
                  height: "200px",
                  width: "250px",
                  gap: "10px",
                }}
              >
                <div className="BoslerHeader1">
                  {getLanguageLabel("embeddedLink")}
                </div>
                <div
                  style={{
                    marginBottom: "10px",
                  }}
                >
                  {getLanguageLabel("pasteLinkIntoSupportingApplication")}
                </div>
                <BoslerButton
                  icon={<LinkIcon />}
                  onClick={() => handleCopy(true, false)}
                  intent="primary"
                  menuItems={copyMenuItems(true)}
                >
                  {getLanguageLabel("copyLink")}
                </BoslerButton>
              </div>
              <div
                style={{
                  flex: 1,
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "center",
                  alignItems: "center",
                  background: "var(--background-color)",
                  padding: "20px",
                  border: "1px solid var(--bosler-border-color-default)",
                  height: "200px",
                  width: "250px",
                  gap: "10px",
                }}
              >
                <div className="BoslerHeader1">
                  {getLanguageLabel("embeddedCode")}
                </div>
                <div
                  style={{
                    marginBottom: "10px",
                  }}
                >
                  {getLanguageLabel("pasteHTMLIframeIntoWebpage")}
                </div>
                <BoslerButton
                  icon={<CodeCellIcon />}
                  onClick={() => handleCopy(false, false)}
                  intent="primary"
                  menuItems={copyMenuItems(false)}
                >
                  Copy Code
                </BoslerButton>
              </div>
            </div>
            <BoslerCollapse
              key={`enconde`}
              collapsible="HEADER"
              header="Encode / Decode Filters"
            >
              <>
                <Row
                  justify={"space-between"}
                  style={{ marginTop: "10px" }}
                  align={"middle"}
                  gutter={[16, 16]}
                >
                  <Col>
                    <Form.Item name="filters">
                      <TextArea
                        rows={10}
                        style={{ minWidth: "250px" }}
                        placeholder="Filters Here... (to encode or decode)"
                      />
                    </Form.Item>
                  </Col>

                  <Col>
                    {" "}
                    <Form.Item>
                      <BoslerButton
                        intent="primary"
                        icon={<KeyIcon />}
                        onClick={() => {
                          form.setFieldValue(
                            "encodedFilters",
                            encodeToBase64(form.getFieldValue("filters"))
                          );
                        }}
                        htmlType="submit"
                        actionIcon={<ArrowRightIcon />}
                      >
                        Encode
                      </BoslerButton>
                    </Form.Item>
                    <Form.Item>
                      <BoslerButton
                        intent="primary"
                        icon={<KeyIcon />}
                        onClick={() => {
                          form.setFieldValue(
                            "encodedFilters",
                            decodeFromBase64(form.getFieldValue("filters"))
                          );
                        }}
                        htmlType="submit"
                        actionIcon={<ArrowRightIcon />}
                      >
                        Decode
                      </BoslerButton>
                    </Form.Item>
                  </Col>
                  <Col>
                    <Form.Item name="encodedFilters">
                      <TextArea
                        placeholder={getLanguageLabel("result")}
                        readOnly
                        rows={10}
                        style={{ minWidth: "250px" }}
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <Row justify={"center"}>
                  <BoslerButton
                    icon={<CopyIcon />}
                    intent="action"
                    onClick={() =>
                      copyToClipboard(form.getFieldValue("encodedFilters"))
                    }
                    disabled={form.getFieldValue("encodedFilters") == ""}
                  >
                    {getLanguageLabel("copy")} Encoded String
                  </BoslerButton>
                </Row>
              </>
            </BoslerCollapse>
          </BoslerModal>
        </Form>
      )}
    </>
  );
};

export default EmbedModal;
