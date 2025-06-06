import { Tag } from "antd";
import React from "react";
import { isDefined } from "utils/utilities";
import sm from "./Tag.module.scss";

interface BoslerTagProps {
  onClick?: any;
  icon?: JSX.Element;
  color?: string;
  children: any;
  onMouseEnter?: any;
  onMouseLeave?: any;
}

export const BoslerTag = ({
  onClick,
  icon,
  color,
  children,
  onMouseEnter,
  onMouseLeave,
}: BoslerTagProps) => {
  return (
    <Tag
      className={sm.boslertag}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      onClick={() => (isDefined(onClick) ? onClick() : null)}
      icon={icon}
      color={color}
    >
      {children}
    </Tag>
  );
};
