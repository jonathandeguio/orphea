import { Form, Switch } from "antd";
import { BoslerCollapse } from "components/BoslerComponents/BoslerCollapse/BoslerCollapse";
import React from "react";
import { getLanguageLabel } from "utils/utilities";

export const WaterFallChartCustomizer = () => {
  return (
    <BoslerCollapse key={"additional"} collapsible={"HEADER"} header={<div className="query_item__heading">{getLanguageLabel("additional")}</div>} >
      <>
      <Form.Item
        label={
          <div className="query_item__heading">
            {getLanguageLabel("showTotalSum")}
          </div>
        }
        name="showTotalSum"
      >
        <Switch size="small" />
      </Form.Item>
      </>
      </BoslerCollapse>
  );
};
