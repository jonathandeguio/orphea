import React from "react";

export const ColorBlock: React.FC<{
  color: string;
  dim?: string;
}> = ({ color, dim }) => {
  return (
    <div
      className="colorBlock"
      style={{
        backgroundColor: color,
        height: dim ?? "0.6rem",
        width: dim ?? "0.6rem",
      }}
    />
  );
};
