// General Menu Item type

import { MenuItemType, SubMenuType } from "antd/es/menu/interface";

interface ExtendedMenuItemType extends MenuItemType {
  customType?: "menu" | "divider";
  tooltip?: string;
  children?: TGeneralMenuItem[];
}

interface ExtendedSubMenuType extends SubMenuType {
  customType?: "menu" | "divider";
  tooltip?: string;
}

export type TGeneralMenuItem = ExtendedMenuItemType | ExtendedSubMenuType;
