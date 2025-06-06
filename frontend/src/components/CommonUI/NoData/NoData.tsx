import React from "react";
import styles from "./NoData.module.scss";

export const NoData = ({
  heading,
  subHeading,
  icon,
  actionArea,
  style,
}: {
  heading?: string;
  subHeading?: string | JSX.Element;
  icon?: any;
  actionArea?: any;
  style?: any;
}) => {
  return (
    <div className={styles.wrapper} style={style}>
      {icon && icon}
      {heading && <div className={styles.heading}>{heading}</div>}
      {subHeading && <p className={styles.subHeading}>{subHeading}</p>}
      {actionArea && <div className={styles.actionArea}>{actionArea}</div>}
    </div>
  );
};

export default NoData;
