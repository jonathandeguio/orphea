import BoslerTable from "Apps/Dataset/Table/BoslerTable";
import React from "react";

const PipelineTable = ({ id, branch }: $TSFixMe) => {
  return (
    <div
      style={{
        height: "95%",
        width: "100%",
      }}
    >
      <BoslerTable
        onDataLoad={() => {}}
        isTableFromBottomBar={true}
        id={id}
        branch={branch}
      />
    </div>
  );
};

export default PipelineTable;
