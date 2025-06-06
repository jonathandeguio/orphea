import { Dropdown, Menu } from "antd";
import React from "react";

const clickHandle = (e: $TSFixMe) => {};

const ContextMenu = ({ children }: $TSFixMe) => {
  const menu = (
    <Menu>
      <Menu.Item onClick={clickHandle}>hello</Menu.Item>
    </Menu>
  );

  return (
    <Dropdown overlay={menu} visible={true}>
      {children}
    </Dropdown>
  );
};

export default ContextMenu;
