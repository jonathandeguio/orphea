import { Dropdown, MenuProps, Tooltip } from "antd";
import { Divider } from "antd/lib";
import { TGeneralMenuItem } from "common/types";
import React from "react";
import styles from "./StripMenu.module.scss";

interface IProps {
  items: TGeneralMenuItem[];
}

interface IStripItem {
  item: TGeneralMenuItem;
  onMouseEnter?: any;
  onMouseLeave?: any;
}

const StripItem = ({ item, onMouseEnter, onMouseLeave }: IStripItem) => {
  return (
    <div
      className={styles.item}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
      <div className={styles.itemIcon}>{item.icon}</div>
      <div className={styles.itemText}>{item.label}</div>
    </div>
  );
};

const DropDownStripItem = ({
  item,
  onMouseEnter,
  onMouseLeave,
}: IStripItem) => {
  if (item.children) {
    const menu: MenuProps = { items: item.children };
    return (
      <Dropdown menu={menu}>
        <StripItem
          item={item}
          onMouseEnter={onMouseEnter}
          onMouseLeave={onMouseLeave}
        />
      </Dropdown>
    );
  }
  return (
    <StripItem
      item={item}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    />
  );
};

const StripMenu = ({ items }: IProps) => {
  return (
    <div className={styles.wrapper}>
      <div className={styles.container}>
        {items.map((item: TGeneralMenuItem) => {
          if (item.customType == "divider") {
            return (
              <div className={styles.item}>
                <Divider
                  type="vertical"
                  style={{ margin: 0, borderWidth: "2px", fontSize: "28px" }}
                />
              </div>
            );
          } else if (item.tooltip) {
            return (
              <Tooltip title={item.tooltip}>
                <DropDownStripItem item={item} />
              </Tooltip>
            );
          } else {
            return <DropDownStripItem item={item} />;
          }
        })}
      </div>
    </div>
  );
};

export default StripMenu;
