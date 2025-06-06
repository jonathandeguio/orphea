import React from "react";

interface IProps {
  node: any;
}

const NodeSubText = ({ node }: IProps) => {
  if (node.originalDataType != undefined) {
    return (
      <div
        style={{
          background: "var(--SWITCH_SELECTED_BKG)",
          color: "var(--SWITCH_SELECTED_FONT)",
          padding: "0px 4px",
          fontSize: "12px",
          width: "100%",
        }}
      >
        {node.originalDataType}
      </div>
    );
  }

  return null;
};

export default NodeSubText;
