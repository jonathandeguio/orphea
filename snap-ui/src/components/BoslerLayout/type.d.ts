import { ITabPane, TabState } from "../BoslerComponents/BoslerTabs/types";

export interface IBoslerBottomBarItem {
  id: string;
  type: "TAB" | "BUTTON";
  label: JSX.Element | string;
  icon: JSX.Element;
  body: React.FC<any>;
  props?: { [id: string]: any };
  tabs?: ITabPane[];
  onOpen?: () => void;
  intent?: "PRIMARY" | "DISABLED" | "WARNING" | "ERROR";
}
export interface IBoslerBottomBarItemBody extends IBoslerBottomBarItem {
  collapseToggle: () => void;
  paneSize: number;
  primaryPanelRef: React.MutableRefObject<any>;
}

export interface IBoslerBottomBar {
  primaryPanelRef: React.MutableRefObject<any>;
}
export interface BottomBarState {
  leftItems: IBoslerBottomBarItem[];
  rightItems?: IBoslerBottomBarItem[];
  bottomBarItems: { [id: string]: IBoslerBottomBarItem };
  tabContext: { [id: string]: TabState };
  activeItem: string | null;
}
