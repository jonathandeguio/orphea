import { Form, Select } from "antd";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

export const SankeyChartCustomizer = () => {
  return (
    <div className="customizer-subHeader">
      <BoslerCollapse
        key="sankeySettings"
        collapsible="HEADER"
        header={
          <div className="query_item__heading">
            {getLanguageLabel("additional") ?? "Additional"}
          </div>
        }
      >
        <>
          <Form.Item
            name="sankeyOrient"
            label={
              <div className="query_item__heading">
                {getLanguageLabel("orientation") ?? "Orientation"}
              </div>
            }
          >
            <Select
              options={[
                { label: "Horizontal", value: "horizontal" },
                { label: "Vertical", value: "vertical" },
              ]}
            />
          </Form.Item>
        </>
      </BoslerCollapse>
    </div>
  );
};
