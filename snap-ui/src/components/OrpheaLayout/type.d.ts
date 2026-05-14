import { ITabPane, TabState } from "../OrpheaComponents/OrpheaTabs/types";

export interface IOrpheaBottomBarItem {
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
export interface IOrpheaBottomBarItemBody extends IOrpheaBottomBarItem {
  collapseToggle: () => void;
  paneSize: number;
  primaryPanelRef: React.MutableRefObject<any>;
}

export interface IOrpheaBottomBar {
  primaryPanelRef: React.MutableRefObject<any>;
}
export interface BottomBarState {
  leftItems: IOrpheaBottomBarItem[];
  rightItems?: IOrpheaBottomBarItem[];
  bottomBarItems: { [id: string]: IOrpheaBottomBarItem };
  tabContext: { [id: string]: TabState };
  activeItem: string | null;
}
