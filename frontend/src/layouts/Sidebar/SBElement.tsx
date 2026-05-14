import { Tooltip, Typography } from "antd";
import React, { ReactElement } from "react";
import { getLanguageLabel } from "utils/utilities";
import styles from "../Sidebar/Sidebar.module.scss";

const { Text } = Typography;

interface TProps {
  icon?: ReactElement;
  tooltip?: ReactElement | string;
  text?: any;
  showText?: any;
  selected?: boolean;
  onClick?: any;
  disabled?: boolean;
  ref?: any;
}

const SBElement = ({
  icon,
  tooltip,
  text,
  showText,
  selected,
  onClick,
  disabled,
  ref,
}: TProps) => {
  const elementClassName = `${styles.element} ${
    selected ? styles.element__selected : ""
  } ${disabled ? styles.element__disabled : ""}`;

  const handleClick = (e: React.MouseEvent) => {
    if (!disabled && onClick) {
      onClick(e);
    }
  };

  return (
    <Tooltip
      placement="right"
      title={disabled ? "Feature is not available." : tooltip}
    >
      <div
        className={elementClassName}
        onClick={handleClick}
        style={{ cursor: disabled ? "not-allowed" : "pointer" }}
      >
        <div className={styles.element__icon}>{icon}</div>
        {text && showText && (
          <div className={styles.element__text}>{getLanguageLabel(text)}</div>
        )}
      </div>
    </Tooltip>
  );
};

export default SBElement;