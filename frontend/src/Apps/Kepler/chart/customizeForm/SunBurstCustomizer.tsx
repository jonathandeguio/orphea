import { Form, Switch } from "antd";
import React from "react";
import { LabelCustomizer } from "./LabelCustomizer";
import { SliceLabelCustomizer } from "./SliceLabelCustomizer";
import { useSelector } from "react-redux";
import { RootState } from "redux/types/store";

export const SunBurstCustomizer = () => {
  const chartCustomize = useSelector(
    (state: RootState) => state.kepler.customize
  );
  return (
    <>
      {/* <Form.Item
        name="sunBurstTreeMap"
        valuePropName="checked"
        label={<div className="query_item__heading">{"sunBurstToggle"}</div>}
      >
        <Switch size={"small"} />
      </Form.Item> */}
      {/* <SliceLabelCustomizer chartCustomize={chartCustomize} /> */}

      <LabelCustomizer />
    </>
  );
};
