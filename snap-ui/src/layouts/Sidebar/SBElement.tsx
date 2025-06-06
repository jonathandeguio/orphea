import { Tooltip, Typography } from "antd";
import React, { ReactElement } from "react";
import { getLanguageLabel } from "utils/utilities";
import styles from "layouts/Sidebar/Sidebar.module.scss";

const { Text } = Typography;
interface TProps {
  icon?: ReactElement;
  tooltip?: ReactElement | string;
  text?: any;
  showText?: any;
  selected?: boolean;
  onClick?: any;
  ref?: any;
}
const SBElement = ({
  icon,
  tooltip,
  text,
  showText,
  selected,
  onClick,
  ref,
}: TProps) => {
  const elementClassName = selected
    ? `${styles.element} ${styles.element__selected}`
    : styles.element;

  return text && showText ? (
    <div className={elementClassName} onClick={onClick}>
      <div className={styles.element__icon}>{icon}</div>
      {text && showText && (
        <div className={styles.element__text}>{getLanguageLabel(text)}</div>
      )}
    </div>
  ) : (
    <Tooltip placement="right" title={tooltip}>
      <div className={elementClassName} onClick={onClick}>
        <div
          className={
            selected ? styles.element__icon_selected : styles.element__icon
          }
        >
          {icon}
        </div>
        {text && showText && (
          <div className={styles.element__text}>{getLanguageLabel(text)}</div>
        )}
      </div>
    </Tooltip>
  );
};

export default SBElement;
