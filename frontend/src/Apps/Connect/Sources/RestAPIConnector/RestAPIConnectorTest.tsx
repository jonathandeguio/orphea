import { Collapse, Tag } from "antd";
import JsonFormatter from "common/utils/JsonFormatter";
import React from "react";
import { getLanguageLabel } from "utils/utilities";
import styles from "./RestAPIConnector.module.scss";
import { IWebhookCallData } from "./RestAPIConnector.types";

interface IProps {
  result: IWebhookCallData[];
}

const getColorByMethod = (method: string) => {
  switch (method) {
    case "GET":
      return "var(--SUCCESS_COLOR)";
    case "POST":
      return "var(--WARNING_COLOR)";
    case "PUT":
      return "var(--PRIMARY_COLOR)";
    case "DELETE":
      return "var(--DANGEROUS_COLOR)";
    default:
      return "processing";
  }
};
const renderCustomCollapse = (items: IWebhookCallData[]) => {
  const collapseItems = items.map((item) => ({
    key: item.id,
    label: (
      <div className="collapse-header">
        <div className="--flex-row-space-between">
          <div>
            <Tag bordered={false} color="processing">
              <span style={{ color: getColorByMethod(item.method) }}>
                {item.method}
              </span>
            </Tag>
            {item.apiTitle}
          </div>
          {item.status && item.status[0] == "2" ? (
            <>
              <div className="--flex-row-space-between">
                <Tag bordered={false} color={"var(--SUCCESS_COLOR)"}>
                  {item.status}
                </Tag>
                {/* <div className="success-tick-circle text-and-icon-center">
                <TickIcon color={"var(--background-color)"} />
              </div> */}
              </div>
            </>
          ) : (
            <div className="--flex-row-space-between">
              <Tag bordered={false} color={"var(--bosler-intent-danger)"}>
                {item.status}
              </Tag>
              {/* <div className="failed-cross-circle text-and-icon-center">
                <CrossIcon color="var(--background-color)" />
              </div> */}
            </div>
          )}
        </div>
      </div>
    ),
    children: (
      <div className="collapse-content">
        <JsonFormatter
          heading={getLanguageLabel("url")}
          jsonString={item.fullUrl}
        />
        <JsonFormatter
          heading={getLanguageLabel("status")}
          jsonString={item.status}
        />
        {item.extraErrors != "" && (
          <JsonFormatter
            heading="Processing Errors"
            jsonString={item.extraErrors}
          />
        )}
        <JsonFormatter
          heading={"Request Headers"}
          jsonString={item.requestHeaders}
        />
        <JsonFormatter heading={"Request Body"} jsonString={item.requestBody} />
        <JsonFormatter
          heading={getLanguageLabel("responseHeaders")}
          jsonString={item.responseHeaders}
        />
        <JsonFormatter
          heading={getLanguageLabel("response")}
          jsonString={item.responseBody}
        />
      </div>
    ),
  }));

  return <Collapse accordion items={collapseItems} defaultActiveKey={["1"]} />;
};

const RestAPIConnectorTest = ({ result }: IProps) => {
  return (
    <div className={styles.api_test_container}>
      {renderCustomCollapse(result)}
    </div>
  );
};

export default RestAPIConnectorTest;
