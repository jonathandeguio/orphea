import React from "react";
import styles from "./NoData.module.scss";

export const NoData = ({
  heading,
  subHeading,
  icon,
  actionArea,
}: {
  heading: string;
  subHeading?: string;
  icon?: any;
  actionArea?: any;
}) => {
  return (
    <div className={styles.wrapper}>
      {icon && icon}
      <div className={styles.heading}>{heading}</div>
      {subHeading && <p className={styles.subHeading}>{subHeading}</p>}
      {actionArea && <div className={styles.actionArea}>{actionArea}</div>}
    </div>
  );
};

export default NoData;
