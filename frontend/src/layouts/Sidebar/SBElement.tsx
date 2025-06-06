import { Tooltip, Typography } from "antd";
import React, { ReactElement } from "react";
import { getLanguageLabel } from "utils/utilities";
import styles from "../Sidebar/Sidebar.module.scss";
import { Link } from "react-router-dom";

const { Text } = Typography;
interface TProps {
  icon?: ReactElement;
  tooltip?: ReactElement | string;
  text?: any;
  showText?: any;
  selected?: boolean;
  onClick?: any;
  ref?: any;
  navigateLink?: any;
}
const SBElement = ({
  icon,
  tooltip,
  text,
  showText,
  selected,
  navigateLink,
  onClick,
  ref,
}: TProps) => {
  const elementClassName = selected
    ? `${styles.element} ${styles.element__selected}`
    : styles.element;

  return text && showText ? (
    <Link onClick={onClick} to={navigateLink}>
    <div className={elementClassName}>
      <div className={styles.element__icon}>{icon}</div>
      {text && showText && (
        <div className={styles.element__text}>{getLanguageLabel(text)}</div>
      )}
     </div>
      </Link>
  ) : (
    <Tooltip placement="right" title={tooltip}>
    <Link onClick={onClick} to={navigateLink}>
      <div className={elementClassName}>
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
        </Link>
    </Tooltip>
  );
};

export default SBElement;