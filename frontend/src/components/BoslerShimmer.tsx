import React from "react";

export const BoslerShimmer = ({
  loading,
  children,
}: {
  loading: boolean | undefined;
  children: JSX.Element | string;
}) => {
  return (
    <div className={`overlay-container ${!!loading ? "shimmer" : ""}`}>
      {children}
    </div>
  );
};
