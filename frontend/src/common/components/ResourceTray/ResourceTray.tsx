import React, { ReactNode } from "react";

interface IProps {
  children: ReactNode;
  size?: number;
}

const ResourceTray = ({ children, size = 100 }: IProps) => {
  return <div>{children}</div>;
};

export default ResourceTray;
