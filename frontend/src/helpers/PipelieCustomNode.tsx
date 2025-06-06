import { Button } from "antd";
import React from "react";

export const pipelineCustomNode = (dataset: $TSFixMe) => {
  return (
    <div className={`flex-container dataset-node`}>
      <Button
        // @ts-expect-error TS(2322): Type '{ children: any; class: string; style: { bor... Remove this comment to see the full error message
        class="name"
        style={{ borderRadius: "6px", height: "10vh", width: "20vw" }}
      >
        {dataset.name}
      </Button>
    </div>
  );
};

export default pipelineCustomNode;
