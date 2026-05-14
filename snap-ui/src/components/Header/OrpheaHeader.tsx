import React from "react";
import styles from "./Header.module.scss";

interface TProps {
  icon?: any;
  heading: any;
  actionComponent?: any;
  description?: string;
  borderBottom?: boolean;
  muted?: boolean;
}
const OrpheaHeader = ({
  icon,
  heading,
  actionComponent,
  description,
  borderBottom = false,
  muted = false,
}: TProps) => {
  return (
    <div
      className={styles.wrapper}
      style={{
        borderBottom: borderBottom
          ? "1px solid var(--orphea-border-color-default)"
          : "",
        background: muted
          ? "var(--orphea-bkg-color-muted)"
          : "var(--orphea-bkg-color-default)",
      }}
    >
      {icon && <div className={styles.icon}>{icon}</div>}
      <div className={styles.container}>
        <div className={styles.headerTop}>
          <div className={styles.heading}>{heading}</div>
          {actionComponent && (
            <div className={styles.actionComponent}>{actionComponent}</div>
          )}
        </div>
        {description && (
          <div className={styles.headerDescription}>{description}</div>
        )}
      </div>
    </div>
  );
};

export default OrpheaHeader;
